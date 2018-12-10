package me.ialistannen.simplecodetester.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class StringOutputStream extends OutputStream {

  private static final int INITIAL_BUFFER_SIZE = 64;

  private byte[] buffer;
  private int size;

  public StringOutputStream() {

    reset();
  }

  /**
   * Resets this {@link StringOutputStream}, also discarding the buffer.
   */
  public void reset() {
    buffer = new byte[INITIAL_BUFFER_SIZE];
    size = 0;
  }

  @Override
  public void write(int b) throws IOException {
    ensureCapacity();

    if (size < buffer.length) {
      buffer[size++] = (byte) b;
    }
  }

  private void ensureCapacity() {
    if (size >= buffer.length) {
      int newSize = size * 2;

      buffer = Arrays.copyOf(buffer, newSize);
    }
  }

  @Override
  public String toString() {
    if (size < 1) {
      return "";
    }

    return new String(Arrays.copyOf(buffer, size), StandardCharsets.UTF_8);
  }
}
