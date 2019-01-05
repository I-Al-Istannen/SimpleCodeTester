package me.ialistannen.simplecodetester.backend.endpoints;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.services.user.UserService;
import me.ialistannen.simplecodetester.backend.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserManageEndpoint {

  private UserService userService;
  private PasswordEncoder passwordEncoder;

  UserManageEndpoint(UserService userService, PasswordEncoder passwordEncoder) {
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Returns all users.
   *
   * @return all users
   */
  @GetMapping("/admin/get-users")
  public List<User> getAllUsers() {
    return StreamSupport.stream(userService.getAllUsers().spliterator(), false)
        .sorted(Comparator.comparing(User::getName))
        .collect(Collectors.toList());
  }

  /**
   * Deletes a user by its id.
   *
   * @param userId the id of the user
   * @return ok if the user was deleted, 404 if not found
   */
  @DeleteMapping("/admin/delete-user/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable("id") @NotEmpty String userId) {
    if (!userService.removeUser(userId)) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok().build();
  }

  /**
   * Deletes a user by its id.
   *
   * @param userBase the user to add
   * @return ok if the user was deleted, 404 if not found
   */
  @PostMapping("/admin/add-user")
  public ResponseEntity<Void> addUser(@RequestBody @Valid @NotNull UserBase userBase) {

    if (userService.containsUser(userBase.id)) {
      return ResponseUtil.error(HttpStatus.CONFLICT, "User already exists.");
    }

    userService.addUser(
        new User(
            userBase.id,
            userBase.displayName,
            passwordEncoder.encode(userBase.password),
            true,
            userBase.roles
        )
    );

    return ResponseEntity.ok().build();
  }

  /**
   * Sets the roies for a given user.
   *
   * @param userId the id of the user
   * @param roles the roles the user should have
   * @return ok if the roles were set, 404 if the user was not found
   */
  @PostMapping("/admin/set-roles")
  public ResponseEntity<Void> setRoles(@RequestParam @NotEmpty String userId,
      @RequestBody List<String> roles) {
    if (!userService.updateUser(userId, user -> user.setAuthorities(roles))) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok().build();
  }

  /**
   * Sets whether the account is enabled.
   *
   * @param userId the id of the account
   * @param enabled true if the count should be enabled
   * @return the current enabled status of the account
   */
  @PostMapping("/admin/set-enabled")
  public ResponseEntity<Boolean> setEnabled(@RequestParam @NotEmpty String userId,
      @RequestParam boolean enabled) {

    if (!userService.containsUser(userId)) {
      return ResponseEntity.notFound().build();
    }
    userService.updateUser(userId, user -> user.setEnabled(enabled));
    return ResponseEntity.ok(enabled);
  }

  @Data
  private static class UserBase {

    @NotEmpty
    private String displayName;
    @NotEmpty
    private String password;
    @NotEmpty
    private String id;
    @NotEmpty
    private List<String> roles;
  }
}
