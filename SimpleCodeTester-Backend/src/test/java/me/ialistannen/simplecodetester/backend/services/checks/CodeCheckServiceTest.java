package me.ialistannen.simplecodetester.backend.services.checks;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import me.ialistannen.simplecodetester.backend.db.entities.CheckCategory;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.services.user.UserService;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.checks.defaults.StaticInputOutputCheck;
import me.ialistannen.simplecodetester.checks.storage.CheckSerializer;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.util.ConfiguredGson;
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
    addAndAssertAdded(getCheck());
  }

  @Test
  void addTwo() {
    addAndAssertAdded(getCheck());
    addAndAssertAdded(getCheck());
    assertThat(
        codeCheckService.getAll().size(),
        is(2)
    );
  }

  @Test
  void removeOne() {
    CodeCheck check = addAndAssertAdded(getCheck());

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
  void updateCheck() {
    CodeCheck check = addAndReturnCheck();

    StaticInputOutputCheck newCheck = new StaticInputOutputCheck(
        Collections.emptyList(), "Test", "Teste"
    );
    String newJson = new CheckSerializer(ConfiguredGson.createGson()).toJson(newCheck);

    assertThat(
        codeCheckService.updateCheck(check.getId(), newCheck),
        is(true)
    );

    assertThat(
        codeCheckService.getCheck(check.getId()).map(CodeCheck::getText),
        is(Optional.of(newJson))
    );
  }

  @Test
  void updateCheckResetsApproval() {
    SimpleCheck simpleCheck = new SimpleCheck(true);
    CodeCheck check = codeCheckService.addCheck(simpleCheck, user, category);

    codeCheckService.updateCheck(check.getId(), simpleCheck);

    // not updated
    assertThat(
        codeCheckService.getCheck(check.getId()).map(CodeCheck::isApproved),
        is(Optional.of(false))
    );
  }

  private CodeCheck addAndReturnCheck() {
    return codeCheckService.addCheck(getCheck(), user, category);
  }

  private CodeCheck addAndAssertAdded(Check check) {
    return addAndAssertAdded(check, user);
  }

  private CodeCheck addAndAssertAdded(Check check, User user) {
    CodeCheck added = codeCheckService.addCheck(check, user, category);

    Optional<CodeCheck> check1 = codeCheckService.getCheck(added.getId());

    assertThat(
        check1,
        is(Optional.of(added))
    );

    return added;
  }

  private StaticInputOutputCheck getCheck() {
    return new StaticInputOutputCheck(Collections.emptyList(), "", "Test");
  }

  private static class SimpleCheck implements Check {

    private boolean needsApproval;

    private SimpleCheck(boolean needsApproval) {
      this.needsApproval = needsApproval;
    }

    @Override
    public CheckResult check(CompiledFile file) {
      return CheckResult.emptySuccess(this);
    }

    @Override
    public boolean needsApproval() {
      return needsApproval;
    }
  }
}
