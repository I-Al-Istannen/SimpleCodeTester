package me.ialistannen.simplecodetester.checks.defaults;

import edu.kit.informatik.Terminal;
import java.util.List;
import java.util.function.Predicate;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.checks.ImmutableCheckResult;
import me.ialistannen.simplecodetester.exceptions.CheckFailedException;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.util.ReflectionHelper;
import org.joor.Reflect;

/**
 * A Check that finds and executes a class with a main method with some given input and validates
 * the output.
 *
 * The validation and input are produced by the subclasses.
 */
public abstract class MainClassRunnerCheck implements Check {

  private Predicate<CompiledFile> mainClassPredicate;

  /**
   * Creates a new MainClassRunnerCheck running every file that has a main method.
   *
   * @see ReflectionHelper#hasMainMethod(Class)
   */
  public MainClassRunnerCheck() {
    this(file -> ReflectionHelper.hasMainMethod(file.asClass()));
  }

  /**
   * Creates a new MainClassRunnerCheck running the psvm of files that match the given predicate.
   *
   * @param mainClassPredicate the predicate main classes have to pass
   */
  public MainClassRunnerCheck(Predicate<CompiledFile> mainClassPredicate) {
    this.mainClassPredicate = mainClassPredicate;
  }

  @Override
  public CheckResult check(CompiledFile file) {
    if (!mainClassPredicate.test(file)) {
      return CheckResult.emptySuccess(this);
    }

    Terminal.setInput(getInput(file));

    Class<?> clazz = file.asClass();

    Reflect.on(clazz)
        .call("main", (Object) new String[0]);

    assertOutputValid(file);

    return ImmutableCheckResult.builder()
        .from(CheckResult.emptySuccess(this))
        .message(Terminal.getOutput())
        .build();
  }

  /**
   * Returns the input to use for the given file.
   *
   * @param file the file being checked
   * @return the input to use
   */
  protected abstract List<String> getInput(CompiledFile file);

  /**
   * Asserts that the output is valid. If not throws an appropriate {@link CheckFailedException}.
   *
   * @param file the file being checked
   * @throws CheckFailedException if the check failed
   */
  protected abstract void assertOutputValid(CompiledFile file);
}
