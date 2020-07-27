package me.ialistannen.simplecodetester.execution.master;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.checks.CheckResult.ResultType;
import me.ialistannen.simplecodetester.checks.ImmutableCheckResult;
import me.ialistannen.simplecodetester.checks.ImmutableSubmissionCheckResult;
import me.ialistannen.simplecodetester.checks.SubmissionCheckResult;
import me.ialistannen.simplecodetester.checks.defaults.io.LineResult;
import me.ialistannen.simplecodetester.checks.defaults.io.LineResult.Type;
import me.ialistannen.simplecodetester.checks.defaults.io.parsing.InterleavedIoParser;
import me.ialistannen.simplecodetester.checks.storage.CheckSerializer;
import me.ialistannen.simplecodetester.compilation.CompilationOutput;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.CompilationFailed;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.DyingMessage;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveComputationTookTooLong;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveDiedWithUnknownError;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveTimedOut;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SubmissionResult;
import me.ialistannen.simplecodetester.submission.ImmutableSubmission;
import me.ialistannen.simplecodetester.submission.Submission;
import me.ialistannen.simplecodetester.util.ConfiguredGson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SlaveManagerTestIT {

  private Semaphore lock;
  private SlaveManager slaveManager;
  private SlaveResult result;
  private CheckSerializer checkSerializer;
  private InterleavedIoParser interleavedIoParser;

  @BeforeEach
  void setUp() {
    lock = new Semaphore(0);
    result = new SlaveResult();

    slaveManager = new SlaveManager(
        (client, protocolMessage) -> {
          if (protocolMessage instanceof SlaveDiedWithUnknownError) {
            result.error = ((SlaveDiedWithUnknownError) protocolMessage).getMessage();
            lock.release();
          } else if (protocolMessage instanceof SlaveTimedOut) {
            result.error = "Slave got no master response.";
            lock.release();
          } else if (protocolMessage instanceof SubmissionResult) {
            SubmissionResult message = (SubmissionResult) protocolMessage;
            result.addResult(message.getFileName(), message.getResult());
          } else if (protocolMessage instanceof CompilationFailed) {
            result.compilationOutput = ((CompilationFailed) protocolMessage).getOutput();
            lock.release();
          } else if (protocolMessage instanceof SlaveComputationTookTooLong) {
            result.error = "Computation took too long!";
            lock.release();
          } else if (protocolMessage instanceof DyingMessage) {
            lock.release();
          }
        },
        getClasspath(),
        Duration.ofSeconds(30)
    );

    slaveManager.start();

    checkSerializer = new CheckSerializer(ConfiguredGson.createGson());
    interleavedIoParser = new InterleavedIoParser("quit", 0);
  }

  @AfterEach
  void tearDown() {
    slaveManager.stop();
  }

  @Test
  void compilationError() throws InterruptedException {
    runSubmission(
        ImmutableSubmission.builder()
            .putFiles("Test.java", "class Hey")
            .build(),
        ""
    );

    assertThat(
        result.toResult().fileResults(),
        is(anEmptyMap())
    );
    assertNotNull(result.compilationOutput);
    assertFalse(result.compilationOutput.successful(), "Compilation not successful");
    assertNotNull(result.compilationOutput.diagnostics().get("Test.java"));
  }

  @Test
  void workingSubmissionSingleFile() throws InterruptedException {
    runSubmission(
        ImmutableSubmission.builder()
            .putFiles("Test.java",
                "import edu.kit.informatik.Terminal;"
                    + "public class Test {"
                    + "public static void main(String[] args) {"
                    + "Terminal.readLine();"
                    + "Terminal.printLine(\"world\");"
                    + "}"
                    + "}"
            )
            .build(),
        "> hello\nworld\n> quit"
    );

    assertNotNull(result);
    assertNull(result.compilationOutput);
    assertNull(result.error);
    assertEquals(1, result.toResult().fileResults().size());
    assertEquals(
        result.toResult().fileResults(),
        Map.of("Test", List.of(
            ImmutableCheckResult.builder()
                .message("")
                .errorOutput("")
                .check("Test")
                .result(ResultType.SUCCESSFUL)
                .output(List.of(
                    new LineResult(Type.INPUT, "hello"),
                    new LineResult(Type.OUTPUT, "world"),
                    new LineResult(Type.INPUT, "quit")
                ))
                .build()
        ))
    );
  }

  @Test
  void doesNotAppendQuitIfCheckContainsIt() throws InterruptedException {
    runSubmission(
        ImmutableSubmission.builder()
            .putFiles("Test.java",
                "import edu.kit.informatik.Terminal;"
                    + "public class Test {"
                    + "public static void main(String[] args) {"
                    + "Terminal.readLine();"
                    + "Terminal.printLine(\"world\");"
                    + "}"
                    + "}"
            )
            .build(),
        "> hello\nworld\n> quit\n> print"
    );

    assertNotNull(result);
    assertNull(result.compilationOutput);
    assertNull(result.error);
    assertEquals(1, result.toResult().fileResults().size());
    assertEquals(
        result.toResult().fileResults(),
        Map.of("Test", List.of(
            ImmutableCheckResult.builder()
                .message("")
                .errorOutput("")
                .check("Test")
                .result(ResultType.SUCCESSFUL)
                .output(List.of(
                    new LineResult(Type.INPUT, "hello"),
                    new LineResult(Type.OUTPUT, "world"),
                    new LineResult(Type.INPUT, "quit"),
                    new LineResult(Type.INPUT, "print")
                ))
                .build()
        ))
    );
  }

  @Test
  void tooMuchOutput() throws InterruptedException {
    runSubmission(
        ImmutableSubmission.builder()
            .putFiles("Test.java",
                "import edu.kit.informatik.Terminal;"
                    + "public class Test {"
                    + "public static void main(String[] args) {"
                    + "Terminal.readLine();"
                    + "Terminal.printLine(\"world\");"
                    + "Terminal.printLine(\"world\");"
                    + "}"
                    + "}"
            )
            .build(),
        "> hello\nworld"
    );

    assertEquals(1, result.toResult().fileResults().size());
    assertEquals(
        result.toResult().fileResults(),
        Map.of("Test", List.of(
            ImmutableCheckResult.builder()
                .message("")
                .errorOutput("")
                .check("Test")
                .result(ResultType.FAILED)
                .output(List.of(
                    new LineResult(Type.INPUT, "hello"),
                    new LineResult(Type.OUTPUT, "world"),
                    new LineResult(Type.OUTPUT, "world"),
                    new LineResult(Type.ERROR, "Did not expect any output."),
                    new LineResult(Type.INPUT, "quit")
                ))
                .build()
        ))
    );
  }

  @Test
  void multipleChecks() throws InterruptedException {
    slaveManager.runSubmission(
        ImmutableSubmission.builder()
            .putFiles("Test.java",
                "import edu.kit.informatik.Terminal;"
                    + "public class Test {"
                    + "public static void main(String[] args) {"
                    + "Terminal.readLine();"
                    + "Terminal.printLine(\"world\");"
                    + "Terminal.printLine(\"world\");"
                    + "}"
                    + "}"
            )
            .build(),
        List.of(
            checkSerializer.toJson(interleavedIoParser.fromString("> hello\nworld\nworld", "A")),
            checkSerializer.toJson(interleavedIoParser.fromString("> hello\nworld", "B"))
        ),
        "Test"
    );

    lock.acquire();

    assertEquals(1, result.toResult().fileResults().size());
    assertEquals(
        result.toResult().fileResults(),
        Map.of(
            "Test", List.of(
                ImmutableCheckResult.builder()
                    .message("")
                    .errorOutput("")
                    .check("A")
                    .result(ResultType.SUCCESSFUL)
                    .output(List.of(
                        new LineResult(Type.INPUT, "hello"),
                        new LineResult(Type.OUTPUT, "world"),
                        new LineResult(Type.OUTPUT, "world"),
                        new LineResult(Type.INPUT, "quit")
                    ))
                    .build(),
                ImmutableCheckResult.builder()
                    .message("")
                    .errorOutput("")
                    .check("B")
                    .result(ResultType.FAILED)
                    .output(List.of(
                        new LineResult(Type.INPUT, "hello"),
                        new LineResult(Type.OUTPUT, "world"),
                        new LineResult(Type.OUTPUT, "world"),
                        new LineResult(Type.ERROR, "Did not expect any output."),
                        new LineResult(Type.INPUT, "quit")
                    ))
                    .build()
            )
        )
    );
  }

  @Test
  void exceptedClassesWork() throws InterruptedException {
    runSubmission(
        ImmutableSubmission.builder()
            .putFiles("Test.java",
                "import edu.kit.informatik.Terminal;"
                    + "public class Test {"
                    + "public static void main(String[] args) {"
                    + "java.util.EnumSet.allOf(java.nio.file.StandardOpenOption.class);"
                    + "java.util.EnumSet.of(java.nio.file.StandardOpenOption.CREATE);"
                    + "new java.util.EnumMap<>(java.nio.file.StandardOpenOption.class);"
                    + "Terminal.printLine(Character.getName(':'));"
                    + "java.util.stream.Stream.of(2, 5).parallel().forEach(it -> {});"
                    + "java.nio.file.StandardCopyOption.class.getEnumConstants();"
                    + "}"
                    + "}"
            )
            .build(),
        "COLON\n> hello\n> quit"
    );

    assertNotNull(result);
    assertNull(result.compilationOutput);
    assertNull(result.error);
    assertEquals(1, result.toResult().fileResults().size());
    assertEquals(
        Map.of("Test", List.of(
            ImmutableCheckResult.builder()
                .message("")
                .errorOutput("")
                .check("Test")
                .result(ResultType.SUCCESSFUL)
                .output(List.of(
                    new LineResult(Type.OUTPUT, "COLON"), // the getName call
                    new LineResult(Type.INPUT, "hello"),
                    new LineResult(Type.INPUT, "quit")
                ))
                .build()
        )),
        result.toResult().fileResults()
    );
  }

  @ParameterizedTest
  @CsvSource({
      "System.exit(0);",
      "Runtime.getRuntime().exec(\"hey\");",
      "Runtime.class.getDeclaredField(\"version\").setAccessible(true);",
  })
  void blocksEvilClasses(String methodString) throws InterruptedException {
    runSubmission(
        ImmutableSubmission.builder()
            .putFiles("Test.java",
                "import edu.kit.informatik.Terminal;"
                    + "public class Test {"
                    + "public static void main(String[] args) throws Exception {"
                    + methodString
                    + "}"
                    + "}"
            )
            .build(),
        "COLON\n> hello\n> quit"
    );

    assertNotNull(result);
    assertNull(result.compilationOutput);
    assertNull(result.error);
    assertEquals(1, result.toResult().fileResults().size());
    assertEquals(1, result.toResult().fileResults().size());
    assertNotNull(result.toResult().fileResults().get("Test"));
    List<CheckResult> results = result.toResult().fileResults().get("Test");
    assertEquals(1, results.size());
    CheckResult result = results.get(0);

    assertEquals(ResultType.FAILED, result.result());
    assertEquals("", result.errorOutput());
    assertEquals("", result.message());
    assertTrue(
        result.output().stream().map(Object::toString).collect(Collectors.joining(""))
            .contains("access denied")
    );
  }


  private void runSubmission(Submission submission, String check) throws InterruptedException {
    slaveManager.runSubmission(
        submission,
        List.of(checkSerializer.toJson(interleavedIoParser.fromString(check, "Test"))),
        "Test"
    );

    lock.acquire();
  }

  private String[] getClasspath() {
    return new String[]{
        Paths.get("target/SimpleCodeTester-Lib.jar").toAbsolutePath().toString()
    };
  }

  private static class SlaveResult {

    private String error;
    private CompilationOutput compilationOutput;
    private Map<String, List<CheckResult>> results = new HashMap<>();

    void addResult(String file, CheckResult result) {
      List<CheckResult> list = results.getOrDefault(file, new ArrayList<>());
      list.add(result);
      results.put(file, list);
    }

    SubmissionCheckResult toResult() {
      return ImmutableSubmissionCheckResult.builder()
          .putAllFileResults(results)
          .build();
    }

    @Override
    public String toString() {
      return "SlaveResult{" +
          "error='" + error + '\'' +
          ", compilationOutput=" + compilationOutput +
          ", result=" + toResult() +
          '}';
    }
  }
}