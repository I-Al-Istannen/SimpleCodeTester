package me.ialistannen.simplecodetester.compilation.java8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import javax.tools.SimpleJavaFileObject;

class InMemoryOutputObject extends SimpleJavaFileObject {

  private ByteArrayOutputStream outputStream;

  public InMemoryOutputObject(Path path) {
    super(path.toUri(), Kind.CLASS);

    this.outputStream = new ByteArrayOutputStream();
  }

  @Override
  public OutputStream openOutputStream() throws IOException {
    return outputStream;
  }

  byte[] getBytes() {
    return outputStream.toByteArray();
  }
}
