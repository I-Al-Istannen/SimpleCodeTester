package me.ialistannen.simplecodetester.execution.compilation.memory;

import java.io.OutputStream;
import java.nio.file.Path;
import javax.tools.SimpleJavaFileObject;

public class NullOutputObject extends SimpleJavaFileObject {

  /**
   * Construct a SimpleJavaFileObject discarding all output..
   *
   * @param path the path to the file
   */
  protected NullOutputObject(Path path) {
    super(path.toUri(), Kind.CLASS);
  }

  @Override
  public OutputStream openOutputStream() {
    return OutputStream.nullOutputStream();
  }
}
