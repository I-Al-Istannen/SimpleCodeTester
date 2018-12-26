package me.ialistannen.simplecodetester.backend.services.checks;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.services.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class CodeCheckServiceTest {

  @Autowired
  private UserService userService;
  @Autowired
  private CodeCheckService codeCheckService;

  private User user;

  @BeforeEach
  void setupUser() {
    user = createUser("TestUser");
    if (!userService.containsUser(user.getId())) {
      userService.addUser(user);
    }
  }

  private User createUser(String id) {
    return new User(id, "John", "32", true, Collections.emptyList());
  }

  @AfterEach
  void cleanChecks() {
    codeCheckService.removeAll();
  }

  @Test
  void addCheck() {
    CodeCheck addedCheck = codeCheckService.addCheck(new CodeCheck("Hello", user));

    addAndAssertAdded(addedCheck);
  }

  @Test
  void addTwo() {
    CodeCheck check1 = codeCheckService.addCheck(new CodeCheck("Hello", user));
    CodeCheck check2 = codeCheckService.addCheck(new CodeCheck("Hello", user));

    addAndAssertAdded(check1);
    addAndAssertAdded(check2);
    assertThat(
        codeCheckService.getAll().size(),
        is(2)
    );
  }

  @Test
  void removeOne() {
    CodeCheck check = codeCheckService.addCheck(new CodeCheck("Hello", user));

    addAndAssertAdded(check);
    assertThat(
        codeCheckService.getAll().size(),
        is(1)
    );

    assertThat(
        codeCheckService.removeCheck(check.getId()),
        is(true)
    );

    assertThat(
        codeCheckService.getCheck(check.getId()),
        is(Optional.empty())
    );
    assertThat(
        codeCheckService.getAll().size(),
        is(0)
    );

    assertThat(
        codeCheckService.removeCheck(check.getId()),
        is(false)
    );
  }

  @Test
  void findByOwner() {
    CodeCheck check = codeCheckService.addCheck(new CodeCheck("Hello", user));
    CodeCheck check2 = codeCheckService.addCheck(new CodeCheck("Hello", user));
    User user = createUser("12");
    userService.addUser(user);
    CodeCheck checkOtherUser = codeCheckService.addCheck(new CodeCheck("Hello", user));

    codeCheckService.addCheck(check);
    codeCheckService.addCheck(check2);
    codeCheckService.addCheck(checkOtherUser);

    assertThat(
        codeCheckService.getChecksForOwner(this.user.getId()),
        is(List.of(check, check2))
    );

    assertThat(
        codeCheckService.getChecksForOwner("12"),
        is(List.of(checkOtherUser))
    );
  }

  @Test
  void updateCheck() {
    CodeCheck check = codeCheckService.addCheck(new CodeCheck("Hello", user));

    codeCheckService.addCheck(check);

    assertThat(
        codeCheckService.updateCheck(check.getId(), codeCheck -> codeCheck.setText("Hey!")),
        is(true)
    );

    assertThat(
        codeCheckService.getCheck(check.getId()).map(CodeCheck::getText),
        is(Optional.of("Hey!"))
    );
  }

  private void addAndAssertAdded(CodeCheck check) {
    codeCheckService.addCheck(check);

    Optional<CodeCheck> check1 = codeCheckService.getCheck(check.getId());

    assertThat(
        check1,
        is(Optional.of(check))
    );
  }
}