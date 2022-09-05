package me.ialistannen.simplecodetester.execution.compilation.memory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import me.ialistannen.simplecodetester.submission.CompiledSubmission;
import me.ialistannen.simplecodetester.submission.ImmutableSubmission;
import me.ialistannen.simplecodetester.submission.Submission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Java17InMemoryCompilerTest {

  private Java17InMemoryCompiler inMemoryCompiler;

  @BeforeEach
  void setupCompiler() {
    inMemoryCompiler = new Java17InMemoryCompiler();
  }

  @Test
  void compileAndRunHelloWorld() throws ReflectiveOperationException {
    Submission submission = fileToSubmission(
        "Test.java",
        "public class Test {"
            + "  public static String getHelloWorld() {"
            + "    return \"Hello world!\";"
            + "  }"
            + "}"
    );

    assertOutputIsHelloWorld(submission);
  }

  @Test
  void compileAndRunLambda() throws ReflectiveOperationException {
    Submission submission = fileToSubmission(
        "Test.java",
        "import java.util.function.*;"
            + "public class Test {"
            + "  public static String getHelloWorld() {"
            + "    Supplier<String> run = () -> \"Hello world!\";"
            + "    return run.get();"
            + "  }"
            + "}"
    );

    assertOutputIsHelloWorld(submission);
  }

  @Test
  void compileAndRunAnonymousClass() throws ReflectiveOperationException {
    Submission submission = fileToSubmission(
        "Test.java",
        "import java.util.function.*;"
            + "public class Test {"
            + "  public static String getHelloWorld() {"
            + "    Supplier<String> run = new Supplier<String>() {"
            + "       public String get() { return \"Hello world!\"; }"
            + "    };"
            + "    return run.get();"
            + "  }"
            + "}"
    );

    assertOutputIsHelloWorld(submission);
  }

  @Test
  void compileAndRunInnerClass() throws ReflectiveOperationException {
    Submission submission = fileToSubmission(
        "Test.java",
        "import java.util.function.*;"
            + "public class Test {"
            + "  public static String getHelloWorld() {"
            + "    Supplier<String> run = new SampleSupplier();"
            + "    return run.get();"
            + "  }"
            + "  private static class SampleSupplier implements Supplier<String> {"
            + "    public String get() { return \"Hello world!\"; }"
            + "  }"
            + "}"
    );

    assertOutputIsHelloWorld(submission);
  }

  private void assertOutputIsHelloWorld(Submission submission)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    CompiledSubmission compiledSubmission = inMemoryCompiler.compileSubmission(submission);
    assertThat(
        compiledSubmission.compilationOutput().output(),
        is("")
    );
    assertThat(
        compiledSubmission.compilationOutput().diagnostics(),
        is(Collections.emptyMap())
    );
    assertThat(
        compiledSubmission.compilationOutput().successful(),
        is(true)
    );
    assertThat(
        "Size does not match",
        compiledSubmission.compiledFiles().size(),
        is(1)
    );

    Class<?> aClass = compiledSubmission.compiledFiles().get(0).asClass();
    assertThat(
        aClass.getDeclaredMethod("getHelloWorld").invoke(null),
        is("Hello world!")
    );
  }

  private Submission fileToSubmission(String name, String code) {
    return ImmutableSubmission.builder()
        .putFiles(name, code)
        .build();
  }
}
