package me.ialistannen.simplecodetester.backend.services.compilation;

import me.ialistannen.simplecodetester.compilation.CompilationOutput;
import me.ialistannen.simplecodetester.compilation.java8.memory.Java8InMemoryCompiler;
import me.ialistannen.simplecodetester.submission.CompiledSubmission;
import me.ialistannen.simplecodetester.submission.ImmutableSubmission;
import org.springframework.stereotype.Service;

@Service
public class LocalCompilationService {

  private Java8InMemoryCompiler compiler;

  public LocalCompilationService() {
    this.compiler = new Java8InMemoryCompiler();
  }

  /**
   * Compiles a single file.
   *
   * @param path the path to the file
   * @param content the content of the file
   * @return the {@link CompilationOutput}
   */
  public CompilationOutput compile(String path, String content) {
    CompiledSubmission compiledSubmission = compiler.compileSubmission(
        ImmutableSubmission.builder()
            .putFiles(path, content)
            .build()
    );
    return compiledSubmission.compilationOutput();
  }
}
