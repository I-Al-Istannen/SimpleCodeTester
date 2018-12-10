package me.ialistannen.simplecodetester.compilation.java8;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import me.ialistannen.simplecodetester.compilation.Compiler;
import me.ialistannen.simplecodetester.compilation.ImmutableCompilationOutput;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.submission.CompiledSubmission;
import me.ialistannen.simplecodetester.submission.ImmutableCompiledFile;
import me.ialistannen.simplecodetester.submission.ImmutableCompiledSubmission;
import me.ialistannen.simplecodetester.submission.Submission;

public class Java8FileCompiler implements Compiler {

  @Override
  public CompiledSubmission compileSubmission(Submission submission) throws IOException {
    JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
    StringWriter output = new StringWriter();

    List<FileInputObject> compilationUnits = Files.walk(submission.basePath())
        .filter(Files::isRegularFile)
        .filter(path -> path.toString().endsWith(".java"))
        .map(FileInputObject::new)
        .collect(Collectors.toList());

    ClassFileManager manager = new ClassFileManager(
        javaCompiler.getStandardFileManager(null, null, StandardCharsets.UTF_8),
        submission.basePath()
    );

    Map<String, List<String>> diagnostics = new HashMap<>();

    Boolean successful = javaCompiler.getTask(
        output,
        manager,
        diagnostic -> {
          // Skip general information not related to the classes
          // Like "Note: Some messages have been simplified;"
          if (diagnostic.getSource() == null) {
            return;
          }
          List<String> diagnosticMessages = diagnostics
              .getOrDefault(diagnostic.getSource().getName(), new ArrayList<>());

          diagnosticMessages.add(diagnostic.toString());

          diagnostics.put(diagnostic.getSource().getName(), diagnosticMessages);
        },
        Arrays.asList("-Xlint:all", "-Xlint:-processing", "--release=8"),
        null,
        compilationUnits
    )
        .call();

    List<CompiledFile> compiledFiles;

    if (!successful) {
      compiledFiles = Collections.emptyList();
    } else {
      compiledFiles = compilationUnits.stream()
          .map(FileInputObject::getPath)
          .map(path -> ImmutableCompiledFile.builder()
              .classLoader(submission.classLoader())
              .classFile(
                  path.resolveSibling(path.getFileName().toString().replace(".java", ".class"))
              )
              .sourceFile(path)
              .qualifiedName(
                  submission.basePath().relativize(path).toString().replace("/", ".")
                      .replace(".java", "")
              )
              .build()
          )
          .collect(Collectors.toList());
    }

    ImmutableCompilationOutput compilationOutput = ImmutableCompilationOutput.builder()
        .successful(successful)
        .output(output.toString())
        .diagnostics(diagnostics)
        .files(compiledFiles)
        .build();

    return ImmutableCompiledSubmission.builder()
        .from(submission)
        .compilationOutput(compilationOutput)
        .files(compiledFiles)
        .build();
  }
}
