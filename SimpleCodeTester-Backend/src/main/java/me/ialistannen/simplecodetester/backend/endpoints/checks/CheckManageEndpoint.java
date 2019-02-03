package me.ialistannen.simplecodetester.backend.endpoints.checks;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
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
import me.ialistannen.simplecodetester.backend.services.user.UserService;
import me.ialistannen.simplecodetester.backend.util.ResponseUtil;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.checks.CheckType;
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
public class CheckManageEndpoint {

  private CheckCategoryService checkCategoryService;
  private CodeCheckService checkService;
  private UserService userService;
  private CheckSerializer checkSerializer;
  private CheckParsers checkParsers;

  public CheckManageEndpoint(CheckCategoryService checkCategoryService,
      CodeCheckService checkService, UserService userService) {
    this.checkCategoryService = checkCategoryService;
    this.checkService = checkService;
    this.userService = userService;

    Gson gson = ConfiguredGson.createGson();
    this.checkSerializer = new CheckSerializer(gson);
    this.checkParsers = new CheckParsers(gson);
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

    Object data = "N/A";

    if (codeCheck.getCheckType() == CheckType.UNKNOWN) {
      Check checkInstance = checkSerializer.fromJson(codeCheck.getText());
      if (checkInstance instanceof InterleavedStaticIOCheck) {
        data = Map.of(
            "class", checkInstance.getClass().getSimpleName(),
            "text", checkInstance.toString(),
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
   * @param payload the json payload
   * @return true if the check was added
   */
  @PostMapping("/checks/add/{categoryId}")
  public ResponseEntity<Object> addNew(@PathVariable("categoryId") long categoryId,
      @RequestBody @NotEmpty String payload) {
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
      Check check = parseCheckFromJsonBlob(payload);

      return ResponseEntity.ok(checkService.addCheck(check, userOptional.get(), checkCategory));
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
  public ResponseEntity<CodeCheck> updateCheck(@PathVariable("checkId") long id,
      @RequestBody @NotEmpty String payload) {
    Authentication user = SecurityContextHolder.getContext().getAuthentication();

    Optional<CodeCheck> storedCheck = checkService.getCheck(id);

    if (storedCheck.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    assertHasPermission(user, storedCheck.get());

    try {
      checkService.updateCheck(id, parseCheckFromJsonBlob(payload));
    } catch (CheckParseException e) {
      return ResponseUtil.error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    //noinspection OptionalGetWithoutIsPresent
    return ResponseEntity.ok(checkService.getCheck(id).get());
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

    return ResponseEntity.ok("{}");
  }

  /**
   * Sets the approve status for a given check.
   *
   * @param id the if of the check
   * @param approved whether the check should be approved
   */
  @PostMapping("/checks/approve")
  public ResponseEntity<Object> approve(long id, boolean approved) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
      return ResponseUtil.error(HttpStatus.FORBIDDEN, "You are not allowed to do this.");
    }

    if (checkService.approveCheck(id, approved)) {
      return ResponseEntity.ok(Map.of());
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
}
