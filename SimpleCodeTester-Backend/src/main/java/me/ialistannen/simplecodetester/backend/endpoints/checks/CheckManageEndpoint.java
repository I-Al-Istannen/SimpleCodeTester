package me.ialistannen.simplecodetester.backend.endpoints.checks;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import me.ialistannen.simplecodetester.backend.db.entities.CheckCategory;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.endpoints.checks.parsers.CheckParsers;
import me.ialistannen.simplecodetester.backend.exception.CheckParseException;
import me.ialistannen.simplecodetester.backend.exception.InvalidCheckException;
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

  private CheckCategoryService checkCategoryService;
  private CodeCheckService checkService;
  private UserService userService;
  private CheckSerializer checkSerializer;
  private CheckParsers checkParsers;

  public CheckManageEndpoint(CheckCategoryService checkCategoryService,
      CodeCheckService checkService, UserService userService,
      ConfigurationService configurationService) {
    this.checkCategoryService = checkCategoryService;
    this.checkService = checkService;
    this.userService = userService;

    Gson gson = ConfiguredGson.createGson();
    this.checkSerializer = new CheckSerializer(gson);
    this.checkParsers = new CheckParsers(gson, configurationService.getParsingConfig());
  }

  @GetMapping("/checks/get-all")
  public List<JsonNode> getAll(ObjectMapper objectMapper) {
    return checkService.getAll().stream()
        .map(codeCheck -> {
          ObjectNode object = objectMapper.createObjectNode()
              .put("id", codeCheck.getId())
              .put("name", codeCheck.getName())
              .put("creator", codeCheck.getCreator().getName())
              .put("checkType", codeCheck.getCheckType().name())
              .put("approved", codeCheck.isApproved());

          ObjectNode categoryNode = objectMapper.createObjectNode();
          categoryNode.put("id", codeCheck.getCategory().getId());
          categoryNode.put("name", codeCheck.getCategory().getName());

          object.set("category", categoryNode);
          return object;
        })
        .collect(toList());
  }

  @GetMapping("/checks/get-content")
  public ResponseEntity<Object> getCheckContent(@RequestParam long id) {
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
            "name", checkInstance.name()
        );
      } else if (checkInstance instanceof StaticInputOutputCheck) {
        data = Map.of(
            "class", checkInstance.getClass().getSimpleName(),
            "input", ((StaticInputOutputCheck) checkInstance).getInput(),
            "output", ((StaticInputOutputCheck) checkInstance).getExpectedOutput(),
            "name", checkInstance.name()
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
  public ResponseEntity<Object> addNew(@PathVariable("categoryId") long categoryId,
      @RequestBody @Valid AddCheckRequest addRequest) {
    AuthenticatedJwtUser user = (AuthenticatedJwtUser) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    Optional<User> userOptional = userService.getUser(user.getUsername());

    if (userOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    CheckCategory checkCategory = checkCategoryService.getById(categoryId)
        .orElseThrow(() -> new WebStatusCodeException("Category not found", HttpStatus.NOT_FOUND));

    try {
      Check check = parseCheckFromJsonBlob(addRequest.getPayload());
      check.setFiles(addRequest.getFiles());

      ResponseEntity<Object> responseEntity = ResponseEntity
          .ok(checkService.addCheck(check, userOptional.get(), checkCategory));

      log.info("User {} added a new check with the name '{}'", user.getUsername(), check.name());

      return responseEntity;
    } catch (CheckParseException e) {
      return ResponseUtil.error(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (InvalidCheckException e) {
      Map<String, Object> data = new HashMap<>();

      data.put("error", e.getMessage());

      if (e.getOutput() != null) {
        data.put("output", e.getOutput());
      }

      return ResponseEntity.badRequest().body(data);
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

  /**
   * Adds a new check.
   *
   * @param payload the new check payload
   * @return true if the check was added
   */
  @PostMapping("/checks/update/{checkId}")
  public ResponseEntity<Object> updateCheck(@PathVariable("checkId") long id,
      @RequestBody @NotEmpty String payload) {
    Authentication user = SecurityContextHolder.getContext().getAuthentication();

    Optional<CodeCheck> storedCheck = checkService.getCheck(id);

    if (storedCheck.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    assertHasPermission(user, storedCheck.get());

    try {
      checkService.updateCheck(id, parseCheckFromJsonBlob(payload));
      log.info("User {} updated a check with the name '{}'",
          user.getName(), storedCheck.get().getName()
      );
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
  public ResponseEntity<Object> remove(@PathVariable long id) {
    Optional<CodeCheck> check = checkService.getCheck(id);

    if (check.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    assertHasPermission(authentication, check.get());

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
  public ResponseEntity<Boolean> approve(long id, boolean approved) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
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

  private void assertHasPermission(Authentication authentication, CodeCheck check) {
    // deleting own is fine
    if (check.getCreator().getId().equals(authentication.getName())) {
      return;
    }

    if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
      throw new WebStatusCodeException("Forbidden", HttpStatus.FORBIDDEN);
    }
  }

  @ToString
  @Getter(onMethod_ = {@JsonProperty})
  private static class AddCheckRequest {

    @NotEmpty
    private String payload;
    @NotNull
    private List<CheckFile> files;

    @JsonCreator
    public AddCheckRequest(@JsonProperty("payload") String payload,
        @JsonProperty("files") List<CheckFile> files) {
      this.payload = payload;
      this.files = files;
    }
  }
}
