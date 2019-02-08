package me.ialistannen.simplecodetester.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ExceptionUtil {

  private ExceptionUtil() {
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

  /**
   * Finds the root cause of an exception.
   *
   * @param throwable the exception
   * @return the root cause
   */
  public static Throwable findRootCause(Throwable throwable) {
    if (throwable.getCause() != null) {
      return findRootCause(throwable.getCause());
    }
    return throwable;
  }
}
