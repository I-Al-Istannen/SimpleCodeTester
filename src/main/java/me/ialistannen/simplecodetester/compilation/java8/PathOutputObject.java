package me.ialistannen.simplecodetester.compilation.java8;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.SimpleJavaFileObject;

class PathOutputObject extends SimpleJavaFileObject {

  private final Path path;

  public PathOutputObject(Path path) {
    super(path.toUri(), Kind.CLASS);

    this.path = path;
  }

  @Override
  public OutputStream openOutputStream() throws IOException {
    return Files.newOutputStream(path);
  }
}
