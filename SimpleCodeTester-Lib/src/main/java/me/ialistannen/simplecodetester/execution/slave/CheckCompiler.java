package me.ialistannen.simplecodetester.execution.slave;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.compilation.Compiler;
import me.ialistannen.simplecodetester.exceptions.CompilationException;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.submission.CompiledSubmission;
import me.ialistannen.simplecodetester.submission.ImmutableSubmission;
import me.ialistannen.simplecodetester.submission.Submission;
import me.ialistannen.simplecodetester.util.ClassParsingUtil;
import org.joor.Reflect;

class CheckCompiler {

  private AtomicInteger checkCounter = new AtomicInteger();

  /**
   * Compiles and instantiates checks from source code.
   *
   * @param checks the source code of the checks
   * @param compiler the compiler to use
   * @return the compiled and instantiated checks
   */
  List<Check> compileAndInstantiateChecks(List<String> checks, Compiler compiler) {
    Map<String, String> checkMap = checks.stream()
        .map(this::prependPackage)
        .collect(toMap(this::getClassPath, x -> x));

    Submission submission = ImmutableSubmission.builder()
        .putAllFiles(checkMap)
        .build();

    CompiledSubmission compiledSubmission = compiler.compileSubmission(submission);

    if (!compiledSubmission.compilationOutput().successful()) {
      throw new CompilationException(compiledSubmission.compilationOutput());
    }

    return compiledSubmission.compiledFiles().stream()
        .map(CompiledFile::asClass)
        .map(aClass -> (Check) Reflect.on(aClass).create().get())
        .collect(toList());
  }

  private String prependPackage(String input) {
    return String.format("package check_%d;%n%s", checkCounter.getAndIncrement(), input);
  }

  private String getClassPath(String input) {
    String qualifiedName =
        ClassParsingUtil.getPackage(input).orElse("")
            + "."
            + ClassParsingUtil.getClassName(input).orElseThrow();
    return qualifiedName.replace(".", "/") + ".java";
  }
}
