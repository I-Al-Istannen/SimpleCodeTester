package me.ialistannen.simplecodetester.compilation.java8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.SimpleJavaFileObject;

class FileInputObject extends SimpleJavaFileObject {

  private final Path path;

  /**
   * Construct a FileInputObject object for the given file.
   *
   * @param path the path to the file
   */
  protected FileInputObject(Path path) {
    super(path.toUri(), Kind.SOURCE);
    this.path = path;
  }

  public Path getPath() {
    return path;
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
    return Files.readString(path);
  }
}
