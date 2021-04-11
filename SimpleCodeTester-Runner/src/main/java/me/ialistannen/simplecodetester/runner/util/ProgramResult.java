package me.ialistannen.simplecodetester.runner.util;

import java.time.Duration;

/**
 * The result of executing a program.
 */
public class ProgramResult {

  private final int exitCode;
  private final String stdOut;
  private final String stdErr;
  private final Duration runtime;

  public ProgramResult(int exitCode, String stdOut, String stdErr, Duration runtime) {
    this.exitCode = exitCode;
    this.stdOut = stdOut;
    this.stdErr = stdErr;
    this.runtime = runtime;
  }

  public int getExitCode() {
    return exitCode;
  }

  public String getStdOut() {
    return stdOut;
  }

  public String getStdErr() {
    return stdErr;
  }

  public Duration getRuntime() {
    return runtime;
  }
}
