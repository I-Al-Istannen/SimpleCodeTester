package me.ialistannen.simplecodetester.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.joor.ReflectException;

@UtilityClass
public class ExceptionUtil {

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

  /**
   * Returns the exception name, message and an excerpt from the stacktrace.
   *
   * @param e the exception to analyze
   * @return the exception name, message and a stacktrace excerpt
   */
  public static List<String> getRelevantStackTraceAndMessage(ReflectException e) {
    Throwable exceptionToAnalyze = e.getCause();

    if (exceptionToAnalyze instanceof InvocationTargetException) {
      exceptionToAnalyze = exceptionToAnalyze.getCause();
    }

    List<String> result = new ArrayList<>();

    result.add(exceptionToAnalyze.getClass().getSimpleName());

    if (exceptionToAnalyze.getMessage() != null) {
      result.add(exceptionToAnalyze.getMessage());
    }

    List<String> stacktrace = Arrays.stream(getStacktrace(exceptionToAnalyze).split("\n"))
        // only take the user's frames. Reflection is the marker that *our* code follows
        // s tad crude, but good enough in this case as reflection is blocked
        .takeWhile(s -> !s.contains("jdk.internal.reflect"))
        .collect(Collectors.toList());

    result.addAll(stacktrace);

    return result;
  }
}
