package me.ialistannen.simplecodetester.backend.security;

import java.util.Collections;
import java.util.List;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.db.repos.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TestInitDbStuff implements CommandLineRunner {

  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;

  public TestInitDbStuff(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    try {
      userRepository.save(new User(
          "123",
          "John",
          passwordEncoder.encode("aVerySecurePassword"),
          true,
          Collections.emptyList()
      ));
      userRepository.save(new User(
          "1234",
          "Johnny",
          passwordEncoder.encode("aVerySecurePassword"),
          true,
          List.of("ROLE_ADMIN")
      ));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
