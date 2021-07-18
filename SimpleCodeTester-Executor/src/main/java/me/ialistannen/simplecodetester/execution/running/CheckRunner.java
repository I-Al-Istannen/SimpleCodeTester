package me.ialistannen.simplecodetester.execution.running;

import static me.ialistannen.simplecodetester.util.ExceptionUtil.findRootCause;

import edu.kit.informatik.Terminal;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.checks.CheckResult.ResultType;
import me.ialistannen.simplecodetester.checks.ImmutableCheckResult;
import me.ialistannen.simplecodetester.exceptions.CheckFailedException;
import me.ialistannen.simplecodetester.exceptions.UnsupportedIoException;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.submission.CompiledSubmission;
import me.ialistannen.simplecodetester.util.ExceptionUtil;
import me.ialistannen.simplecodetester.util.StringOutputStream;

/**
 * A class that runs a given suit of tests against a {@link CompiledSubmission}, reporting the
 * output.
 */
public class CheckRunner {

  private final List<Check> checks;

  /**
   * Creates a new CheckRunner that will check a {@link CompiledSubmission}.
   *
   * @param checks the {@link Check}s to use
   */
  public CheckRunner(List<Check> checks) {
    this.checks = new ArrayList<>(checks);
  }

  /**
   * Runs all checks against the given {@link CompiledSubmission}.
   *
   * @param compiledSubmission the {@link CompiledSubmission} to test
   * @param resultConsumer the consumer for individual file results
   * @param checkStartingConsumer a consumer that is notified when a check is started
   */
  public void checkSubmission(CompiledSubmission compiledSubmission,
      BiConsumer<String, CheckResult> resultConsumer, Consumer<String> checkStartingConsumer) {
    modifySystemInAndOut();

    for (CompiledFile file : compiledSubmission.compiledFiles()) {
      for (Check check : checks) {
        checkStartingConsumer.accept(
            "'" + check.name() + "' on class '" + file.qualifiedName() + "'"
        );

        long startTime = System.currentTimeMillis();
        CheckResult checkResult = tryCheck(check, file);
        long endTime = System.currentTimeMillis();

        if (checkResult.result() != ResultType.NOT_APPLICABLE) {
          ImmutableCheckResult timedResult = ImmutableCheckResult.builder()
              .from(checkResult)
              .durationMillis(Optional.of(endTime - startTime))
              .build();
          resultConsumer.accept(file.qualifiedName(), timedResult);
        }
      }
    }
  }

  private CheckResult tryCheck(Check check, CompiledFile file) {
    try {
      Terminal.reset();
      return check.check(file);
    } catch (CheckFailedException e) {
      return ImmutableCheckResult.builder()
          .message(findRootCause(e).getMessage() == null ? "" : findRootCause(e).getMessage())
          .check(check.name())
          .files(check.getFiles())
          .result(ResultType.FAILED)
          .errorOutput("")
          .output(e.getOutputLines())
          .build();
    } catch (Throwable e) { // user checks should not crash everything
      e.printStackTrace();
      return ImmutableCheckResult.builder()
          .message(getRootCauseMessage(findRootCause(e)))
          .check(check.name())
          .files(check.getFiles())
          .result(ResultType.FAILED)
          .errorOutput("")
          .build();
    }
  }

  private String getRootCauseMessage(Throwable rootCause) {
    String message = rootCause.getMessage() == null
        ? rootCause.getClass().getSimpleName()
        : rootCause.getMessage();
    return message + "\n\nStacktrace:\n" + ExceptionUtil.getStacktrace(rootCause);
  }

  private void modifySystemInAndOut() {
    System.setIn(new InputStream() {
      @Override
      public int read() {
        throw new UnsupportedIoException(
            "You can not read from System.in directly. Artemis I/O is too restrictive to allow proper"
                + " error messages so we had to get creative."
                + " Please use a Scanner or BufferedReader to read from System.in or files."
        );
      }
    });
    System.setOut(new PrintStream(new OutputStream() {
      private StringOutputStream outputStream = new StringOutputStream();

      @Override
      public void write(int b) {
        if (b == '\n') {
          Terminal.printLine(outputStream.getString());
          outputStream = new StringOutputStream();
        } else {
          outputStream.write(b);
        }
      }
    }));
    System.setErr(new PrintStream(new OutputStream() {
      private StringOutputStream outputStream = new StringOutputStream();

      @Override
      public void write(int b) {
        if (b == '\n') {
          Terminal.printLine(outputStream.getString());
          outputStream = new StringOutputStream();
        } else {
          outputStream.write(b);
        }
      }
    }));
  }
}
