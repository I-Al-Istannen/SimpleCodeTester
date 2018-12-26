package me.ialistannen.simplecodetester.backend.services.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserServiceTest {

  @Autowired
  private UserService userService;

  @BeforeEach
  void setupUser() {
    userService.removeAll();
  }

  @Test
  void addUser() {
    User user = new User("Test", "User", "31", true, List.of());
    userService.addUser(user);

    assertThat(
        userService.getUser("Test"),
        is(Optional.of(user))
    );
  }

  @Test
  void addMultipleUser() {
    Supplier<User> userSupplier = () -> new User("Test", "User", "31", true, List.of());
    User user = userSupplier.get();
    userService.addUser(user);

    assertThat(
        userService.getUser("Test"),
        is(Optional.of(user))
    );

    assertThrows(
        IllegalArgumentException.class,
        () -> userService.addUser(userSupplier.get())
    );
  }

  @Test
  void addDeleteUser() {
    User user = new User("Test", "User", "31", true, List.of());
    userService.addUser(user);

    assertThat(
        userService.getUser("Test"),
        is(Optional.of(user))
    );

    assertThat(
        userService.removeUser("Test"),
        is(true)
    );

    assertThat(
        userService.getUser("Test"),
        is(Optional.empty())
    );
  }

  @Test
  void updateUserName() {
    User user = new User("Test", "User", "31", true, List.of());
    userService.addUser(user);

    assertThat(
        userService.getUser("Test"),
        is(Optional.of(user))
    );

    assertThat(
        userService.updateUser("Test", u -> u.setEnabled(false)),
        is(true)
    );

    assertThat(
        userService.getUser("Test").map(User::getEnabled),
        is(Optional.of(false))
    );
  }

}