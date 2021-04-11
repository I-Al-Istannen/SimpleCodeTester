package me.ialistannen.simplecodetester.execution.compilation.memory;

import java.nio.file.Paths;
import javax.tools.SimpleJavaFileObject;

class InMemoryFileInputObject extends SimpleJavaFileObject {

  private final String name;
  private String content;

  /**
   * Construct a SimpleJavaFileObject providing input from a string.
   *
   * @param name the name of this file
   * @param content the content
   */
  InMemoryFileInputObject(String name, String content) {
    super(Paths.get(name).toUri(), Kind.SOURCE);
    this.name = name;

    this.content = content;
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return content;
  }

  @Override
  public String getName() {
    return name;
  }

  /**
   * The content of this file.
   *
   * @return content of this file
   */
  String getContent() {
    return content;
  }
}
