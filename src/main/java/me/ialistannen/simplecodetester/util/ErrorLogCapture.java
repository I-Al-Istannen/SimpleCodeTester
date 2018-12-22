package me.ialistannen.simplecodetester.util;

import java.io.PrintStream;

/**
 * Allows capturing {@link System#err}.
 */
public class ErrorLogCapture {

  private StringOutputStream outputStream = new StringOutputStream();
  private PrintStream originalError;

  /**
   * Starts capturing {@link System#err} output.
   */
  public void startCapture() {
    originalError = System.err;
    System.setErr(new PrintStream(outputStream));
  }

  /**
   * Stops capturing the {@link System#err} output.
   */
  public void stopCapture() {
    System.setErr(originalError);
    originalError = null;
  }

  /**
   * Returns the captured {@link System#err} output.
   *
   * @return the captured output
   */
  public String getCaptured() {
    return outputStream.toString();
  }
}
