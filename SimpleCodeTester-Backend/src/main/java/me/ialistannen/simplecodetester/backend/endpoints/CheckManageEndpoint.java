package me.ialistannen.simplecodetester.backend.endpoints;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import me.ialistannen.simplecodetester.backend.db.entities.CheckCategory;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.exception.InvalidCheckException;
import me.ialistannen.simplecodetester.backend.exception.WebStatusCodeException;
import me.ialistannen.simplecodetester.backend.security.AuthenticatedJwtUser;
import me.ialistannen.simplecodetester.backend.services.checks.CheckCategoryService;
import me.ialistannen.simplecodetester.backend.services.checks.CodeCheckService;
import me.ialistannen.simplecodetester.backend.services.user.UserService;
import me.ialistannen.simplecodetester.backend.util.ResponseUtil;
import me.ialistannen.simplecodetester.checks.CheckType;
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

  public CheckManageEndpoint(CheckCategoryService checkCategoryService,
      CodeCheckService checkService, UserService userService) {
    this.checkCategoryService = checkCategoryService;
    this.checkService = checkService;
    this.userService = userService;
  }

  @GetMapping("/checks/get-all")
  public List<JsonNode> getAll(ObjectMapper objectMapper) {
    return checkService.getAll().stream()
        .map(codeCheck -> {
          ObjectNode object = objectMapper.createObjectNode()
              .put("id", codeCheck.getId())
              .put("name", codeCheck.getName())
              .put("creator", codeCheck.getCreator().getName())
              .put("approved", codeCheck.isApproved())
              .put("checkType", codeCheck.getCheckType().name());

          ObjectNode categoryNode = objectMapper.createObjectNode();
          categoryNode.put("id", codeCheck.getCategory().getId());
          categoryNode.put("name", codeCheck.getCategory().getName());

          object.set("category", categoryNode);
          return object;
        })
        .collect(toList());
  }

  @GetMapping("/checks/get")
  public ResponseEntity<CodeCheck> getCheck(@RequestParam long id) {
    Optional<CodeCheck> check = checkService.getCheck(id);

    if (check.isEmpty()) {
      return ResponseUtil.error(HttpStatus.NOT_FOUND, "Check not found!");
    }

    return ResponseEntity.ok(check.get());
  }

  /**
   * Adds a new check.
   *
   * @param categoryId the id of the category
   * @param text the text of the check
   * @return true if the check was added
   */
  @PostMapping("/checks/add/{categoryId}")
  public ResponseEntity<Object> addNew(@PathVariable("categoryId") long categoryId,
      @RequestBody @NotEmpty String text) {
    AuthenticatedJwtUser user = (AuthenticatedJwtUser) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    Optional<User> userOptional = userService.getUser(user.getUsername());

    if (userOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    CheckCategory checkCategory = checkCategoryService.getById(categoryId)
        .orElseThrow(() -> new WebStatusCodeException("Category not found", HttpStatus.NOT_FOUND));

    CodeCheck codeCheck = new CodeCheck(
        text,
        CheckType.SOURCE_CODE,
        userOptional.get(),
        checkCategory
    );

    try {
      return ResponseEntity.ok(checkService.addCheck(codeCheck));
    } catch (InvalidCheckException e) {
      Map<String, Object> data = new HashMap<>();

      data.put("error", e.getMessage());

      if (e.getOutput() != null) {
        data.put("output", e.getOutput());
      }

      return ResponseEntity.badRequest().body(data);
    }
  }

  @PostMapping("/checks/add-io")
  public ResponseEntity<Object> addInputOutputCheck(@RequestParam @NotNull String input,
      @RequestParam @NotNull String output, @RequestParam @NotEmpty String name,
      @RequestParam long categoryId, Principal principal) {

    CheckCategory checkCategory = checkCategoryService.getById(categoryId)
        .orElseThrow(() -> new WebStatusCodeException("Category not found", HttpStatus.NOT_FOUND));

    String slightlySanerInput = sanitizeIOInput(input);
    String slightlySanerOutput = sanitizeIOInput(output) + "\n";

    // User logged in, so they very likely still exist
    User user = userService.getUser(principal.getName()).orElseThrow();

    return ResponseEntity.ok(checkService.addIOCheck(
        Arrays.asList(slightlySanerInput.split("\n")),
        slightlySanerOutput,
        name,
        user,
        checkCategory
    ));
  }

  private String sanitizeIOInput(String input) {
    return input.replace("\r\n", "\n").trim();
  }

  @PostMapping("/checks/update-io")
  public ResponseEntity<Object> updateInputOutputCheck(@RequestParam @NotNull String input,
      @RequestParam @NotNull String output, @RequestParam @NotEmpty String name,
      @RequestParam long checkId) {

    String slightlySanerInput = sanitizeIOInput(input);
    String slightlySanerOutput = sanitizeIOInput(output) + "\n";

    Optional<CodeCheck> check = checkService.getCheck(checkId);
    if (check.isEmpty()) {
      return ResponseUtil.error(HttpStatus.NOT_FOUND, "Check not found");
    }
    if (check.get().getCheckType() != CheckType.IO) {
      return ResponseUtil.error(HttpStatus.BAD_REQUEST, "Check is no IO check!");
    }

    boolean successfullyUpdated = checkService.updateIOCheck(
        checkId,
        Arrays.asList(slightlySanerInput.split("\n")),
        slightlySanerOutput,
        name
    );

    if (successfullyUpdated) {
      return ResponseEntity.ok("{}");
    }
    return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error");
  }

  /**
   * Adds a new check.
   *
   * @param text the text of the check
   * @return true if the check was added
   */
  @PostMapping("/checks/update")
  public ResponseEntity<CodeCheck> updateCheck(@RequestParam long id,
      @RequestBody @NotEmpty String text) {
    Authentication user = SecurityContextHolder.getContext().getAuthentication();

    Optional<CodeCheck> storedCheck = checkService.getCheck(id);

    if (storedCheck.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    assertHasPermission(user, storedCheck.get());

    checkService.updateCheck(id, check -> check.setText(text));

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
  @RolesAllowed("ROLE_ADMIN")
  public ResponseEntity<Object> approve(long id, boolean approved) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
      return ResponseUtil.error(HttpStatus.FORBIDDEN, "You are not allowed to do this.");
    }

    if (checkService.approveCheck(id, approved)) {
      return ResponseEntity.ok("{}");
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
