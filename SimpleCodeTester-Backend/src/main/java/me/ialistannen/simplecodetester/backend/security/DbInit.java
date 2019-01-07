package me.ialistannen.simplecodetester.backend.security;

import java.util.List;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.services.checks.CheckCategoryService;
import me.ialistannen.simplecodetester.backend.services.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DbInit implements CommandLineRunner {

  private UserService userService;
  private CheckCategoryService checkCategoryService;
  private PasswordEncoder passwordEncoder;

  public DbInit(UserService userService, CheckCategoryService checkCategoryService,
      PasswordEncoder passwordEncoder) {
    this.userService = userService;
    this.checkCategoryService = checkCategoryService;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) throws Exception {
    if (userService.getUser("1234").isEmpty()) {
      userService.addUser(new User(
          "1234", "Johnny", passwordEncoder.encode("aVerySecurePassword"), true,
          List.of("ROLE_ADMIN")
      ));
    }
    if (checkCategoryService.getAll().isEmpty()) {
      checkCategoryService.addCategory("Test");
    }
  }
}
