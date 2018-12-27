package me.ialistannen.simplecodetester.compilation.java8.memory;

import edu.kit.informatik.Terminal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import me.ialistannen.simplecodetester.checks.SubmissionCheckResult;
import me.ialistannen.simplecodetester.checks.defaults.MainClassRunnerCheck;
import me.ialistannen.simplecodetester.execution.master.SlaveManager;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveDiedWithUnknownError;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveStarted;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveTimedOut;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SubmissionResult;
import me.ialistannen.simplecodetester.submission.CompiledFile;
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

  // This test only works if the dependency jar is present
  @Disabled
  @Test
  void compileSimpleProgram() throws Throwable {
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
            semaphore.release();
          } else if (protocolMessage instanceof SlaveTimedOut) {
            exception.set(
                new RuntimeException("Slave timed out!")
            );
            semaphore.release();
          } else if (protocolMessage instanceof SubmissionResult) {
            SubmissionCheckResult result = ((SubmissionResult) protocolMessage).getResult();

            if (result.files().isEmpty()) {
              exception.set(new RuntimeException("Files are empty!"));
            } else if (result.fileResults().get("Test").isEmpty()) {
              exception.set(new RuntimeException("Test has no checks"));
            } else if (!result.overallSuccessful()) {
              exception.set(
                  new RuntimeException("Not overall successful, got: " + result.fileResults())
              );
            } else {
              exception.set(null);
            }

            semaphore.release();
          }
        },
        getClasspath()
    );
    slaveManager.start();
    slaveManager.runSubmission(
        submission,
        List.of(ExpectHelloWorldCheck.class.getName()),
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

  public static class ExpectHelloWorldCheck extends MainClassRunnerCheck {

    @Override
    protected List<String> getInput(CompiledFile file) {
      return Collections.emptyList();
    }

    @Override
    protected void assertOutputValid(CompiledFile file) {
      if (Terminal.getOutput().equals("Hello world!")) {
        throw new AssertionError("Expected: 'Hello world!', got '" + Terminal.getOutput() + "'!");
      }
    }
  }
}
