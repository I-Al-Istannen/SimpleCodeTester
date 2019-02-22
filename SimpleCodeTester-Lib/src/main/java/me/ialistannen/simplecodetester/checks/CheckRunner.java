package me.ialistannen.simplecodetester.checks;

import static java.util.stream.Collectors.toList;
import static me.ialistannen.simplecodetester.util.ExceptionUtil.findRootCause;

import edu.kit.informatik.Terminal;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import me.ialistannen.simplecodetester.checks.CheckResult.ResultType;
import me.ialistannen.simplecodetester.checks.ImmutableSubmissionCheckResult.Builder;
import me.ialistannen.simplecodetester.exceptions.CheckFailedException;
import me.ialistannen.simplecodetester.exceptions.UnsupportedIoException;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.submission.CompiledSubmission;
import me.ialistannen.simplecodetester.util.ErrorLogCapture;

/**
 * A class that runs a given suit of tests against a {@link CompiledSubmission}, reporting the
 * output.
 */
public class CheckRunner {

  private List<Check> checks;

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
   * @return the {@link SubmissionCheckResult}
   */
  public SubmissionCheckResult checkSubmission(CompiledSubmission compiledSubmission) {
    disableSystemInAndOut();

    Builder builder = ImmutableSubmissionCheckResult.builder();

    for (CompiledFile file : compiledSubmission.compiledFiles()) {
      List<CheckResult> checkResults = checks.stream()
          .map(check -> tryCheck(check, file))
          .collect(toList());
      builder.putFileResults(file.qualifiedName(), checkResults);
    }

    return builder.build();
  }

  private CheckResult tryCheck(Check check, CompiledFile file) {
    ErrorLogCapture capture = new ErrorLogCapture();
    try {
      capture.startCapture();
      Terminal.reset();
      return check.check(file);
    } catch (CheckFailedException e) {
      return ImmutableCheckResult.builder()
          .message(findRootCause(e).getMessage() == null ? "" : findRootCause(e).getMessage())
          .check(check.name())
          .result(ResultType.FAILED)
          .errorOutput(capture.getCaptured())
          .output(e.getOutputLines())
          .build();
    } catch (Throwable e) { // user checks should not crash everything
      e.printStackTrace(System.out);
      return ImmutableCheckResult.builder()
          .message(getRootCauseMessage(findRootCause(e)))
          .check(check.name())
          .result(ResultType.FAILED)
          .errorOutput(capture.getCaptured())
          .build();
    } finally {
      capture.stopCapture();
    }
  }

  private String getRootCauseMessage(Throwable rootCause) {
    return rootCause.getMessage() == null
        ? rootCause.getClass().getSimpleName()
        : rootCause.getMessage();
  }

  private void disableSystemInAndOut() {
    System.setIn(new InputStream() {
      @Override
      public int read() {
        throw new UnsupportedIoException(
            "You can not read from System.in, please use the Terminal class for io!"
        );
      }
    });
    System.setOut(new PrintStream(new OutputStream() {
      @Override
      public void write(int b) {
        throw new UnsupportedIoException(
            "You can not write to System.out, please use the Terminal class for io!"
        );
      }
    }));
  }
}
