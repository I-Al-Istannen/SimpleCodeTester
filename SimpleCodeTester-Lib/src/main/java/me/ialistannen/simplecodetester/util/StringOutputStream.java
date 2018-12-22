package me.ialistannen.simplecodetester.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class StringOutputStream extends OutputStream {

  private ByteArrayOutputStream underlyingData;

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
