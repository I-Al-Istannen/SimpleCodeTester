package me.ialistannen.simplecodetester.checks.defaults;

import edu.kit.informatik.Terminal;
import java.util.List;
import java.util.function.Predicate;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.util.ReflectionHelper;
import org.joor.Reflect;

public abstract class MainClassRunnerCheck implements Check {

  private Predicate<CompiledFile> mainClassPredicate;
  private List<String> input;
  private Predicate<String> outputPredicate;

  public MainClassRunnerCheck(Predicate<CompiledFile> mainClassPredicate, List<String> input,
      Predicate<String> outputPredicate) {
    this.mainClassPredicate = mainClassPredicate;
    this.input = input;
    this.outputPredicate = outputPredicate;
  }

  public MainClassRunnerCheck(List<String> input, Predicate<String> outputPredicate) {
    this(hasMainMethod(MainClassRunnerCheck.class.getClassLoader()), input, outputPredicate);
  }

  @Override
  public CheckResult check(CompiledFile file) {
    if (!mainClassPredicate.test(file)) {
      return CheckResult.emptySuccess(this);
    }

    Terminal.setInput(input);

    Class<?> clazz = file.asClass();

    Reflect.on(clazz)
        .call("main", (Object) new String[0]);

    if (outputPredicate.test(Terminal.getOutput())) {
      return CheckResult.emptySuccess(this);
    }

    return CheckResult.failure(getErrorMessage(file));
  }

  /**
   * Returns an appropriate error message for why it failed.
   *
   * <p>You can access {@link Terminal#getOutput()} in this method.</p>
   *
   * @param file the file that failed
   * @return the error message
   */
  protected abstract String getErrorMessage(CompiledFile file);

  /**
   * A predicate checking whether the class has a main method.
   *
   * @param classLoader the class loader to use for the class lookup
   * @return true if the class has a main method
   */
  public static Predicate<CompiledFile> hasMainMethod(ClassLoader classLoader) {
    return file -> ReflectionHelper.hasMainMethod(file.asClass());
  }
}
