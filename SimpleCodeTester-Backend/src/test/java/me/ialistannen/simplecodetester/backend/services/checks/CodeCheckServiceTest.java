package me.ialistannen.simplecodetester.backend.services.checks;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import me.ialistannen.simplecodetester.backend.db.entities.CheckCategory;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.exception.InvalidCheckException;
import me.ialistannen.simplecodetester.backend.services.user.UserService;
import me.ialistannen.simplecodetester.checks.CheckType;
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
  @Autowired
  private CheckCategoryService checkCategoryService;

  private User user;
  private CheckCategory category;

  @BeforeEach
  void setupUser() {
    user = createUser("TestUser");
    if (!userService.containsUser(user.getId())) {
      userService.addUser(user);
    }
    category = checkCategoryService.addCategory("Test");
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
    CodeCheck addedCheck = codeCheckService.addCheck(getCheck(user));

    addAndAssertAdded(addedCheck);
  }

  @Test
  void addTwo() {
    CodeCheck check1 = codeCheckService.addCheck(getCheck(user));
    CodeCheck check2 = codeCheckService.addCheck(getCheck(user));

    addAndAssertAdded(check1);
    addAndAssertAdded(check2);
    assertThat(
        codeCheckService.getAll().size(),
        is(2)
    );
  }

  @Test
  void removeOne() {
    CodeCheck check = codeCheckService.addCheck(getCheck(user));

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
    CodeCheck check = codeCheckService.addCheck(getCheck(user));
    CodeCheck check2 = codeCheckService.addCheck(getCheck(user));
    User user = createUser("12");
    userService.addUser(user);
    CodeCheck checkOtherUser = codeCheckService.addCheck(getCheck(user));

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
    CodeCheck check = codeCheckService.addCheck(getCheck(user));

    codeCheckService.addCheck(check);

    String newText = getCodeCheckText().replace("Hello", "MyClass");
    assertThat(
        codeCheckService.updateCheck(check.getId(), codeCheck -> codeCheck.setText(newText)),
        is(true)
    );

    assertThat(
        codeCheckService.getCheck(check.getId()).map(CodeCheck::getText),
        is(Optional.of(newText))
    );
  }

  @Test
  void updateCheckPreservesValidity() {
    CodeCheck check = codeCheckService.addCheck(getCheck(user));

    codeCheckService.addCheck(check);

    String newText = getCodeCheckText() + "this is a compiler error";
    assertThrows(
        InvalidCheckException.class,
        () -> codeCheckService.updateCheck(check.getId(), codeCheck -> codeCheck.setText(newText))
    );

    // not updated
    assertThat(
        codeCheckService.getCheck(check.getId()).map(CodeCheck::getText),
        is(Optional.of(getCodeCheckText()))
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

  private CodeCheck getCheck(User user) {
    return new CodeCheck(getCodeCheckText(), CheckType.SOURCE_CODE, user, category);
  }

  private String getCodeCheckText() {
    return "import me.ialistannen.simplecodetester.checks.*;\n"
        + "import me.ialistannen.simplecodetester.submission.*;\n"
        + "public class Hello implements Check{\n"
        + "\n"
        + "  @Override\n"
        + "  public CheckResult check(CompiledFile file) {\n"
        + "    return CheckResult.emptySuccess(this);\n"
        + "  }\n"
        + "}";
  }

}