package me.ialistannen.simplecodetester.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * A simple {@link OutputStream} that collects the data in a string.
 */
public class StringOutputStream extends OutputStream {

  private ByteArrayOutputStream underlyingData;

  /**
   * Creates a new string output stream.
   */
  public StringOutputStream() {
    this.underlyingData = new ByteArrayOutputStream();
  }

  @Override
  public void write(int b) {
    underlyingData.write(b);
  }

  @Override
  public String toString() {
    return new String(underlyingData.toByteArray());
  }
}
