package me.ialistannen.simplecodetester.backend.endpoints;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.services.user.UserService;
import me.ialistannen.simplecodetester.backend.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class UserManageEndpoint {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;

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
  public ResponseEntity<Object> deleteUser(@PathVariable("id") @NotEmpty String userId) {
    if (!userService.removeUser(userId)) {
      return ResponseEntity.notFound().build();
    }
    log.info("{} deleted user '{}'",
        SecurityContextHolder.getContext().getAuthentication().getName(), userId
    );
    return ResponseEntity.ok(Map.of());
  }

  /**
   * Deletes a user by its id.
   *
   * @param addUserBase the user to add
   * @return ok if the user was deleted, 404 if not found
   */
  @PostMapping("/admin/add-user")
  public ResponseEntity<Object> addUser(
      @RequestBody @Valid @NotNull UserManageEndpoint.AddUserBase addUserBase) {

    String userId = addUserBase.id.trim();
    if (userService.containsUser(userId)) {
      return ResponseUtil.error(HttpStatus.CONFLICT, "User already exists.");
    }

    userService.addUser(
        new User(
            userId,
            addUserBase.displayName.trim(),
            passwordEncoder.encode(addUserBase.password),
            true,
            addUserBase.roles
        )
    );

    log.info("{} added the user '{}'",
        SecurityContextHolder.getContext().getAuthentication().getName(),
        userId
    );

    return ResponseEntity.ok(Map.of());
  }

  /**
   * Sets the roies for a given user.
   *
   * @param userId the id of the user
   * @param roles the roles the user should have
   * @return ok if the roles were set, 404 if the user was not found
   */
  @PostMapping("/admin/set-roles")
  public ResponseEntity<Object> setRoles(@RequestParam @NotEmpty String userId,
      @RequestBody List<String> roles) {
    if (!userService.updateUser(userId, user -> user.setAuthorities(roles))) {
      return ResponseEntity.notFound().build();
    }
    log.info("{} set roles for {} to {}",
        SecurityContextHolder.getContext().getAuthentication().getName(),
        userId,
        String.join(", ", roles)
    );
    return ResponseEntity.ok(Map.of());
  }

  /**
   * Modifies a given user.
   *
   * @param user the user to modify
   */
  @PostMapping("/admin/update-user")
  public ResponseEntity<Object> updateUser(@RequestBody JsonObject user) {
    String id = user.get("id").getAsString();
    boolean enabled = user.get("enabled").getAsBoolean();
    String name = user.get("displayName").getAsString();
    List<String> roles = new ArrayList<>();

    if (user.has("roles")) {
      JsonArray nodes = user.get("roles").getAsJsonArray();
      for (JsonElement node : nodes) {
        roles.add(node.getAsString());
      }
    }

    if (!userService.containsUser(id)) {
      return ResponseUtil.error(HttpStatus.NOT_FOUND, "User not found");
    }

    userService.updateUser(id, editable -> {
      editable.setEnabled(enabled);
      editable.setName(name);
      editable.setAuthorities(roles);

      if (user.has("password") && !user.get("password").getAsString().isBlank()) {
        editable.setPasswordHash(passwordEncoder.encode(user.get("password").getAsString()));
      }
    });

    log.info("User {} updated user '{}'",
        SecurityContextHolder.getContext().getAuthentication().getName(),
        id
    );

    return ResponseEntity.ok(Map.of());
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

    log.info("User {} enabled user '{}': {}",
        SecurityContextHolder.getContext().getAuthentication().getName(),
        userId,
        enabled
    );

    return ResponseEntity.ok(enabled);
  }

  /**
   * Sets an account's password.
   *
   * @param userId the id of the account
   * @param newPassword the new password
   */
  @PostMapping("/admin/set-password")
  public ResponseEntity<String> setPassword(@RequestParam @NotEmpty String userId,
      @RequestParam @NotEmpty String newPassword) {

    if (!userService.containsUser(userId)) {
      return ResponseEntity.notFound().build();
    }
    userService
        .updateUser(userId, user -> user.setPasswordHash(passwordEncoder.encode(newPassword)));

    log.info("User {} changed the password of {}",
        SecurityContextHolder.getContext().getAuthentication().getName(),
        userId
    );

    return ResponseEntity.ok("{}");
  }

  /**
   * Sets your own password.
   *
   * @param newPassword the new password
   */
  @PostMapping("/set-own-password")
  public ResponseEntity<String> setOwnPassword(@RequestParam @NotEmpty String newPassword,
      @NotNull Principal principal) {

    userService.updateUser(
        principal.getName(),
        user -> user.setPasswordHash(passwordEncoder.encode(newPassword))
    );

    log.info("User {} / {} updated his password",
        SecurityContextHolder.getContext().getAuthentication().getName(),
        principal.getName()
    );

    return ResponseEntity.ok("{}");
  }

  @Data
  private static class AddUserBase {

    @NotEmpty
    private String displayName;
    @NotEmpty
    private String password;
    @NotEmpty
    private String id;
    private List<String> roles;
  }
}
