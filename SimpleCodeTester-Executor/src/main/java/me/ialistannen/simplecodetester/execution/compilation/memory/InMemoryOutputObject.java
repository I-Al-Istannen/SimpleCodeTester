package me.ialistannen.simplecodetester.execution.compilation.memory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import javax.tools.SimpleJavaFileObject;

class InMemoryOutputObject extends SimpleJavaFileObject {

  private final ByteArrayOutputStream outputStream;
  private final String name;

  InMemoryOutputObject(String name) {
    // A path does necessarily exist
    super(Paths.get(name).toUri(), Kind.CLASS);
    this.name = name;

    this.outputStream = new ByteArrayOutputStream();
  }

  @Override
  public OutputStream openOutputStream() {
    return outputStream;
  }

  @Override
  public String getName() {
    return name;
  }

  /**
   * Returns the byte content of this file.
   *
   * @return the byte content
   */
  byte[] getContent() {
    return outputStream.toByteArray();
  }
}
