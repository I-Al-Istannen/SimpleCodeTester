package me.ialistannen.simplecodetester.compilation;

import java.io.IOException;
import java.nio.file.Path;

public interface Compiler {


  /**
   * Compiles the whole folder.
   *
   * @param folder the folder to compile
   * @return the compiler output
   */
  CompilationOutput compileFolder(Path folder) throws IOException;
}
