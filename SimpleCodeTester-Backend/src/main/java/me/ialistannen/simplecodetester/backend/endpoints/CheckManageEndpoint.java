package me.ialistannen.simplecodetester.backend.endpoints;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.exception.InvalidCheckException;
import me.ialistannen.simplecodetester.backend.exception.WebStatusCodeException;
import me.ialistannen.simplecodetester.backend.security.AuthenticatedJwtUser;
import me.ialistannen.simplecodetester.backend.services.checks.CodeCheckService;
import me.ialistannen.simplecodetester.backend.services.user.UserService;
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

  private CodeCheckService checkService;
  private UserService userService;

  public CheckManageEndpoint(CodeCheckService checkService, UserService userService) {
    this.checkService = checkService;
    this.userService = userService;
  }

  @GetMapping("/checks/get-all")
  public List<CodeCheck> getAll() {
    return checkService.getAll();
  }

  /**
   * Adds a new check.
   *
   * @param text the text of the check
   * @return true if the check was added
   */
  @PostMapping("/checks/add")
  public ResponseEntity<Object> addNew(@RequestBody @NotEmpty String text) {
    AuthenticatedJwtUser user = (AuthenticatedJwtUser) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    Optional<User> userOptional = userService.getUser(user.getUsername());

    if (userOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    CodeCheck codeCheck = new CodeCheck(text, userOptional.get());

    try {
      return ResponseEntity.ok(checkService.addCheck(codeCheck));
    } catch (InvalidCheckException e) {
      return ResponseEntity.badRequest().body(
          Map.of("message", e.getMessage(), "output", e.getOutput())
      );
    }
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
  public ResponseEntity<Void> remove(@PathVariable long id) {
    Optional<CodeCheck> check = checkService.getCheck(id);

    if (check.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    assertHasPermission(authentication, check.get());

    checkService.removeCheck(id);

    return ResponseEntity.ok().build();
  }

  private void assertHasPermission(Authentication authentication, CodeCheck check) {
    // deleting own is fine
    if (check.getCreator().getId().equals(authentication.getName())) {
      return;
    }

    if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
      throw new WebStatusCodeException("Forbidden", HttpStatus.FORBIDDEN);
    }
  }
}
