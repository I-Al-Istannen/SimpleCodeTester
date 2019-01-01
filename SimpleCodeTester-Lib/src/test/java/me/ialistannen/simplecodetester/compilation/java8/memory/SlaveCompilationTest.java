package me.ialistannen.simplecodetester.compilation.java8.memory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import me.ialistannen.simplecodetester.checks.SubmissionCheckResult;
import me.ialistannen.simplecodetester.execution.master.SlaveManager;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.DyingMessage;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveDiedWithUnknownError;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveStarted;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveTimedOut;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SubmissionResult;
import me.ialistannen.simplecodetester.submission.ImmutableSubmission;
import me.ialistannen.simplecodetester.submission.Submission;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class SlaveCompilationTest {

  private String[] getClasspath() {
    Path basePath = Paths.get(
        SlaveCompilationTest.class.getProtectionDomain().getCodeSource().getLocation().getPath()
    );
    String testClassPath = basePath.toString();
    // Oh god, this is so dirty
    String normalClassesPath = basePath.resolveSibling("SimpleCodeTester-Lib.jar").toString();
    return new String[]{testClassPath, normalClassesPath};
  }

  // This test only works if the built jar is present, as it is more of an Integration test
  @Disabled
  @Test
  void compileSimpleProgram() throws Throwable {
    Submission submission = fileToSubmission(
        "Test.java",
        "import java.util.function.*;\n"
            + "import edu.kit.informatik.Terminal;\n"
            + "public class Test {\n"
            + "  public static void main(String[] args) {\n"
            + "    Supplier<String> run = new SampleSupplier();\n"
            + "    Terminal.printLine(run.get());\n"
            + "  }\n"
            + "  private static class SampleSupplier implements Supplier<String> {\n"
            + "    public String get() { return \"Hello world!\"; }\n"
            + "  }\n"
            + "}"
    );

    AtomicReference<Throwable> exception = new AtomicReference<>(
        new RuntimeException("No message sent!")
    );
    Semaphore semaphore = new Semaphore(0);

    SlaveManager slaveManager = new SlaveManager(
        (messageClient, protocolMessage) -> {
          System.out.println(protocolMessage);
          if (protocolMessage instanceof SlaveStarted) {
            System.out.println("Slave is up!");
          } else if (protocolMessage instanceof SlaveDiedWithUnknownError) {
            exception.set(
                new AssertionError(((SlaveDiedWithUnknownError) protocolMessage).getMessage())
            );
          } else if (protocolMessage instanceof SlaveTimedOut) {
            exception.set(
                new RuntimeException("Slave timed out!")
            );
          } else if (protocolMessage instanceof SubmissionResult) {
            SubmissionCheckResult result = ((SubmissionResult) protocolMessage).getResult();

            if (result.fileResults().get("Test").isEmpty()) {
              exception.set(new RuntimeException("Test has no checks"));
            } else if (!result.overallSuccessful()) {
              exception.set(
                  new RuntimeException("Not overall successful, got: " + result.fileResults())
              );
            } else {
              exception.set(null);
            }
          }

          if (protocolMessage instanceof DyingMessage) {
            semaphore.release();
          }
        },
        getClasspath(),
        Duration.ofSeconds(30)
    );
    slaveManager.start();
    slaveManager.runSubmission(
        submission,
        List.of(getCheckSource()),
        "test"
    );

    try {
      semaphore.tryAcquire(10, TimeUnit.SECONDS);
    } finally {
      slaveManager.stop();
    }

    if (exception.get() != null) {
      throw exception.get();
    }
  }

  private Submission fileToSubmission(String name, String code) {
    return ImmutableSubmission.builder()
        .putFiles(name, code)
        .build();
  }

  private String getCheckSource() {
    return "import edu.kit.informatik.Terminal;\n"
        + "import me.ialistannen.simplecodetester.checks.defaults.MainClassRunnerCheck;\n"
        + "import me.ialistannen.simplecodetester.submission.CompiledFile;\n"
        + "import java.util.*;\n"
        + ""
        + "public class ExpectHelloWorldCheck extends MainClassRunnerCheck {\n"
        + "  @Override\n"
        + "  protected List<String> getInput(CompiledFile file) {\n"
        + "    return Collections.emptyList();\n"
        + "  }\n"
        + "  @Override\n"
        + "  protected void assertOutputValid(CompiledFile file) {\n"
        + "    if (!Terminal.getOutput().equals(\"Hello world!\\n\")) {\n"
        + "      throw new AssertionError(\"Expected: 'Hello world!', got '\" + Terminal.getOutput() + \"'!\");\n"
        + "    }\n"
        + "  }\n"
        + "}";
  }
}
