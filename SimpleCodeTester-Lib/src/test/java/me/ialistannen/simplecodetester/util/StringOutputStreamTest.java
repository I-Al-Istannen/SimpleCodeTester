package me.ialistannen.simplecodetester.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StringOutputStreamTest {

  private StringOutputStream outputStream;

  @BeforeEach
  void setup() {
    outputStream = new StringOutputStream();
  }

  @Test
  void canRetrieveData() throws IOException {
    outputStream.write("Hello!".getBytes(StandardCharsets.UTF_8));

    assertEquals(
        outputStream.toString(),
        "Hello!"
    );
  }

  @Test
  void preservesNewlines() throws IOException {
    outputStream.write("Hello\n!".getBytes(StandardCharsets.UTF_8));

    assertEquals(
        outputStream.toString(),
        "Hello\n!"
    );
  }

  @Test
  void canHandleUnicode() throws IOException {
    outputStream.write("Hello λψ".getBytes(StandardCharsets.UTF_8));

    assertEquals(
        outputStream.toString(),
        "Hello λψ"
    );
  }

}