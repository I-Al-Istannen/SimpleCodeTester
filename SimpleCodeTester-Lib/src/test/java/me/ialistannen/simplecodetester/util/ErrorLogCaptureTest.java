package me.ialistannen.simplecodetester.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ErrorLogCaptureTest {

  private ErrorLogCapture capture;

  @BeforeEach
  void setup() {
    capture = new ErrorLogCapture();
  }

  @AfterEach
  void teardown() {
    capture.stopCapture();
  }

  @Test
  void doesNotChangeSysOut() {
    PrintStream originalOut = System.out;
    capture.startCapture();

    assertThat(
        System.out,
        is(originalOut)
    );

  }

  @Test
  void replacesSysErr() {
    PrintStream originalError = System.err;
    capture.startCapture();

    assertThat(
        System.err,
        is(not(originalError))
    );
  }

  @Test
  void capturesError() {
    String text = "Hello world!";
    capture.startCapture();

    System.err.print(text);

    assertThat(
        capture.getCaptured(),
        is(text)
    );
  }


}