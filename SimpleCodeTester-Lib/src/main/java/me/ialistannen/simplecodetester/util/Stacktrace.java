package me.ialistannen.simplecodetester.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Stacktrace {

  private Stacktrace() {
    throw new UnsupportedOperationException("No instantiation");
  }

  /**
   * Returns the stacktrace of a {@link Throwable} as a string.
   *
   * @param e the {@link Throwable}
   * @return the stacktrace of it
   */
  public static String getStacktrace(Throwable e) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    e.printStackTrace(printWriter);

    return stringWriter.getBuffer().toString();
  }
}
