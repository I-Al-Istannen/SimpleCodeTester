package me.ialistannen.simplecodetester.backend.endpoints.checks;

import static java.util.stream.Collectors.toList;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import me.ialistannen.simplecodetester.backend.db.entities.CheckCategory;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.endpoints.checks.parsers.CheckParsers;
import me.ialistannen.simplecodetester.backend.exception.CheckParseException;
import me.ialistannen.simplecodetester.backend.exception.WebStatusCodeException;
import me.ialistannen.simplecodetester.backend.security.AuthenticatedJwtUser;
import me.ialistannen.simplecodetester.backend.services.checks.CheckCategoryService;
import me.ialistannen.simplecodetester.backend.services.checks.CodeCheckService;
import me.ialistannen.simplecodetester.backend.services.config.ConfigurationService;
import me.ialistannen.simplecodetester.backend.services.user.UserService;
import me.ialistannen.simplecodetester.backend.util.ResponseUtil;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.checks.Check.CheckFile;
import me.ialistannen.simplecodetester.checks.defaults.StaticInputOutputCheck;
import me.ialistannen.simplecodetester.checks.defaults.io.InterleavedStaticIOCheck;
import me.ialistannen.simplecodetester.checks.storage.CheckSerializer;
import me.ialistannen.simplecodetester.util.ConfiguredGson;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class CheckManageEndpoint {

  private final CheckCategoryService checkCategoryService;
  private final CodeCheckService checkService;
  private final UserService userService;
  private final CheckSerializer checkSerializer;
  private final CheckParsers checkParsers;
  private final Counter editedChecksCounter;

  public CheckManageEndpoint(CheckCategoryService checkCategoryService,
      CodeCheckService checkService, UserService userService,
      ConfigurationService configurationService) {
    this.checkCategoryService = checkCategoryService;
    this.checkService = checkService;
    this.userService = userService;

    Gson gson = ConfiguredGson.createGson();
    this.checkSerializer = new CheckSerializer(gson);
    this.checkParsers = new CheckParsers(gson, configurationService.getParsingConfig());
    this.editedChecksCounter = Counter
        .builder("checks_edited")
        .description("How many edit actions were recorded")
        .register(Metrics.globalRegistry);

    Gauge.builder("total_check_count", checkService::getCount)
        .description("Total number of checks")
        .register(Metrics.globalRegistry);
  }

  @GetMapping("/checks/get-all")
  public List<JsonElement> getAll() {
    Authentication user = SecurityContextHolder.getContext().getAuthentication();
    boolean sadFace = user.getName().equals("I Al Istannen");
    return checkService.getAll().stream()
        .map(codeCheck -> {
          JsonObject object = new JsonObject();
          object.addProperty("id", codeCheck.getId());
          object.addProperty("name", codeCheck.getName());
          if (sadFace || user.getName().equals(codeCheck.getCreator().getName())) {
            object.addProperty("creator", codeCheck.getCreator().getName());
          }
          object.addProperty("checkType", codeCheck.getCheckType().name());
          object.addProperty("approved", codeCheck.isApproved());
          object.addProperty("creationTime", codeCheck.getCreationTime().toEpochMilli());

          codeCheck.getUpdateTime()
              .ifPresent(it -> object.addProperty("updateTime", it.toEpochMilli()));

          JsonObject categoryObject = new JsonObject();
          categoryObject.addProperty("id", codeCheck.getCategory().getId());
          categoryObject.addProperty("name", codeCheck.getCategory().getName());

          object.add("category", categoryObject);
          return object;
        })
        .collect(toList());
  }

  @GetMapping("/checks/get-content")
  public ResponseEntity<Object> getCheckContent(@RequestParam int id) {
    Optional<CodeCheck> check = checkService.getCheck(id);

    if (check.isEmpty()) {
      return ResponseUtil.error(HttpStatus.NOT_FOUND, "Check not found!");
    }

    CodeCheck codeCheck = check.get();

    return serializeCheckContent(codeCheck);
  }

  private ResponseEntity<Object> serializeCheckContent(CodeCheck codeCheck) {
    Object data = "N/A";

    JsonObject checkJson = checkSerializer.toJson(codeCheck.getText());

    if (checkJson.has("class")) {
      Check checkInstance = checkSerializer.fromJson(codeCheck.getText());

      // Yes, this could be solved with yet another abstraction layer
      // No, probably not worth it for two tests
      if (checkInstance instanceof InterleavedStaticIOCheck) {
        data = Map.of(
            "class", checkInstance.getClass().getSimpleName(),
            "text", checkInstance.toString(),
            "name", checkInstance.name(),
            "files", checkInstance.getFiles()
        );
      } else if (checkInstance instanceof StaticInputOutputCheck) {
        data = Map.of(
            "class", checkInstance.getClass().getSimpleName(),
            "input", ((StaticInputOutputCheck) checkInstance).getInput(),
            "output", ((StaticInputOutputCheck) checkInstance).getExpectedOutput(),
            "name", checkInstance.name(),
            "files", checkInstance.getFiles()
        );
      }
    } else {
      data = codeCheck.getText();
    }

    return ResponseEntity.ok(Map.of("content", data));
  }

  /**
   * Adds a new check.
   *
   * @param categoryId the id of the category
   * @param addRequest the add request
   * @return true if the check was added
   */
  @PostMapping("/checks/add/{categoryId}")
  public ResponseEntity<Object> addNew(@PathVariable("categoryId") int categoryId,
      @RequestBody @Valid AddOrUpdateCheckRequest addRequest) {
    AuthenticatedJwtUser user = (AuthenticatedJwtUser) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    Optional<User> userOptional = userService.getUser(user.getUsername());

    if (userOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    CheckCategory checkCategory = checkCategoryService.getById(categoryId)
        .orElseThrow(() -> new WebStatusCodeException("Category not found", HttpStatus.NOT_FOUND));

    long distinctNameSize = addRequest.files.stream().map(CheckFile::getName).distinct().count();
    if (distinctNameSize != addRequest.files.size()) {
      return ResponseUtil
          .error(HttpStatus.BAD_REQUEST, "Mehrere Dateien haben den gleichen Namen!");
    }
    if (addRequest.files.size() > 25) {
      return ResponseUtil.error(HttpStatus.BAD_REQUEST, "Zu viele Dateien");
    }

    try {
      Check check = parseCheckFromJsonBlob(addRequest.getPayload());
      check.setFiles(addRequest.getFiles());

      ResponseEntity<Object> responseEntity = ResponseEntity
          .ok(checkService.addCheck(check, userOptional.get(), checkCategory));

      log.info("User {} added a new check with the name '{}'", user.getUsername(), check.name());

      return responseEntity;
    } catch (CheckParseException e) {
      return ResponseUtil.error(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  private Check parseCheckFromJsonBlob(String payload) {
    JsonObject jsonObject = checkSerializer.toJson(payload);
    String checkJson = jsonObject.getAsJsonPrimitive("value").getAsString();
    String keyword = jsonObject.getAsJsonPrimitive("class").getAsString();
    Check check = checkParsers.parsePayload(checkJson, keyword);

    if (check == null) {
      throw new CheckParseException("Could not successfully parse the check :/");
    }
    return check;
  }

  @PostMapping("/checks/update/{checkId}")
  public ResponseEntity<Object> updateCheck(@PathVariable("checkId") int id,
      @RequestBody @NotEmpty AddOrUpdateCheckRequest changeRequest) {
    Authentication user = SecurityContextHolder.getContext().getAuthentication();

    Optional<CodeCheck> storedCheck = checkService.getCheck(id);

    if (storedCheck.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    // updating own is fine
    if (!storedCheck.get().getCreator().getId().equals(user.getName())) {
      // Or if you are an editor
      if (!isAdmin(user) && !isEditor(user)) {
        throw new WebStatusCodeException("Forbidden", HttpStatus.FORBIDDEN);
      }
    }

    long distinctNameSize = changeRequest.files.stream().map(CheckFile::getName).distinct().count();
    if (distinctNameSize != changeRequest.files.size()) {
      return ResponseUtil
          .error(HttpStatus.BAD_REQUEST, "Mehrere Dateien haben den gleichen Namen!");
    }

    try {
      Check newCheck = parseCheckFromJsonBlob(changeRequest.payload);
      newCheck.setFiles(changeRequest.files);
      checkService.updateCheck(id, newCheck);
      log.info("User {} updated a check with the name '{}'",
          user.getName(), storedCheck.get().getName()
      );
      editedChecksCounter.increment();
    } catch (CheckParseException e) {
      return ResponseUtil.error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    //noinspection OptionalGetWithoutIsPresent
    return serializeCheckContent(checkService.getCheck(id).get());
  }

  /**
   * Deletes a given check.
   *
   * @param id the if of the check
   * @return true if the check was removed
   */
  @DeleteMapping("/checks/remove/{id}")
  public ResponseEntity<Object> remove(@PathVariable int id) {
    Optional<CodeCheck> check = checkService.getCheck(id);

    if (check.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // deleting only works as admin if it is not your own
    if (!check.get().getCreator().getId().equals(authentication.getName())) {
      if (!isAdmin(authentication)) {
        throw new WebStatusCodeException("Forbidden", HttpStatus.FORBIDDEN);
      }
    }

    checkService.removeCheck(id);
    log.info("User {} deleted a check with the name '{}'",
        authentication.getName(), check.get().getName()
    );

    return ResponseEntity.ok("{}");
  }

  /**
   * Sets the approve status for a given check.
   *
   * @param id the if of the check
   * @param approved whether the check should be approved
   * @return the new approve status
   */
  @PostMapping("/checks/approve")
  public ResponseEntity<Boolean> approve(int id, boolean approved) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (!isAdmin(authentication) && !isEditor(authentication)) {
      return ResponseUtil.error(HttpStatus.FORBIDDEN, "You are not allowed to do this.");
    }

    if (checkService.approveCheck(id, approved)) {
      log.info("User {} set the check approval status of {} to {}",
          authentication.getName(),
          id,
          approved
      );

      return ResponseEntity.ok(approved);
    }

    return ResponseUtil.error(HttpStatus.NOT_FOUND, "The check could not be found.");
  }

  private boolean isAdmin(Authentication authentication) {
    return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
  }

  private boolean isEditor(Authentication authentication) {
    return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EDITOR"));
  }

  @ToString
  @Getter
  private static class AddOrUpdateCheckRequest {

    @NotEmpty
    private final String payload;
    @NotNull
    private final List<CheckFile> files;

    public AddOrUpdateCheckRequest(String payload, List<CheckFile> files) {
      this.payload = payload;
      this.files = files;
    }
  }
}
