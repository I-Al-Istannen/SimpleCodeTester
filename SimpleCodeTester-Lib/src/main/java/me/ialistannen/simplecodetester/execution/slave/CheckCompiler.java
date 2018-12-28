package me.ialistannen.simplecodetester.execution.slave;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.compilation.Compiler;
import me.ialistannen.simplecodetester.exceptions.CompilationException;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.submission.CompiledSubmission;
import me.ialistannen.simplecodetester.submission.ImmutableSubmission;
import me.ialistannen.simplecodetester.submission.Submission;
import org.joor.Reflect;

class CheckCompiler {

  /**
   * Compiles and instantiates checks from source code.
   *
   * @param checks the source code of the checks
   * @param compiler the compiler to use
   * @return the compiled and instantiated checks
   */
  List<Check> compileAndInstantiateChecks(List<String> checks, Compiler compiler) {
    Map<String, String> checkMap = checks.stream()
        .collect(toMap(this::getClassPath, Function.identity()));

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

  private String getClassPath(String input) {
    Matcher matcher = Pattern.compile("class (\\w+) ?").matcher(input);
    if (!matcher.find()) {
      throw new RuntimeException("Input contains no class declaration: " + input);
    }
    return matcher.group(1) + ".java";
  }
}
