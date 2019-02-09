package me.ialistannen.simplecodetester.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

import java.util.List;
import org.joor.Reflect;
import org.joor.ReflectException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExceptionUtilTest {


  @Test
  void rootCauseMayNotBeNull() {
    assertThat(
        ExceptionUtil.findRootCause(new RuntimeException()),
        notNullValue()
    );
  }

  @Test
  @DisplayName("if there is no root cause the result should be the exception")
  void rootCauseNotExistingIsException() {
    RuntimeException exception = new RuntimeException();
    assertThat(
        ExceptionUtil.findRootCause(exception),
        is(exception)
    );
  }

  @Test
  void findsNestedRootCause() {
    RuntimeException innerTwo = new RuntimeException();
    RuntimeException innerOne = new RuntimeException(innerTwo);

    RuntimeException outer = new RuntimeException(innerOne);

    assertThat(
        ExceptionUtil.findRootCause(outer),
        is(innerTwo)
    );
  }

  @Test
  void testRelevantStacktraceStopsAtReflectionWithMessage() {
    try {
      Reflect.on(ReflectionTestHelper.class)
          .call("iThrowWithMessage");
    } catch (ReflectException e) {
      assertGetStacktraceIsValid(e, "Message");
    }
  }

  @Test
  void testRelevantStacktraceStopsAtReflectionWithoutMessage() {
    try {
      Reflect.on(ReflectionTestHelper.class)
          .call("iThrowWithoutMessage");
    } catch (ReflectException e) {
      assertGetStacktraceIsValid(e, null);
    }
  }

  private void assertGetStacktraceIsValid(ReflectException e, String exceptionMessage) {
    List<String> message = ExceptionUtil.getRelevantStackTraceAndMessage(e);

    int listIndex = 0;
    assertThat(
        message.get(listIndex++),
        is(RuntimeException.class.getSimpleName())
    );

    if (exceptionMessage != null) {
      assertThat(
          message.get(listIndex++),
          is(exceptionMessage)
      );
      assertThat(
          message.get(listIndex++),
          is(RuntimeException.class.getName() + ": " + exceptionMessage)
      );
    } else {
      assertThat(
          message.get(listIndex++),
          is(RuntimeException.class.getName())
      );
    }

    assertThat(
        message.get(listIndex++),
        startsWith(
            "\tat me.ialistannen.simplecodetester.util.ExceptionUtilTest$ReflectionTestHelper"
        )
    );

    assertThat(
        "Stack trace size is 4",
        message.size(),
        is(listIndex)
    );
  }

  private static class ReflectionTestHelper {

    public static void iThrowWithoutMessage() {
      throw new RuntimeException();
    }

    public static void iThrowWithMessage() {
      throw new RuntimeException("Message");
    }
  }
}