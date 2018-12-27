package me.ialistannen.simplecodetester.compilation.java8.memory;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import me.ialistannen.simplecodetester.compilation.Compiler;
import me.ialistannen.simplecodetester.compilation.ImmutableCompilationOutput;
import me.ialistannen.simplecodetester.execution.SubmissionClassLoader;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.submission.CompiledSubmission;
import me.ialistannen.simplecodetester.submission.ImmutableCompiledFile;
import me.ialistannen.simplecodetester.submission.ImmutableCompiledSubmission;
import me.ialistannen.simplecodetester.submission.Submission;

public class Java8InMemoryCompiler implements Compiler {

  @Override
  public CompiledSubmission compileSubmission(Submission submission) {
    JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
    StringWriter output = new StringWriter();

    List<InMemoryFileInputObject> compilationUnits = submission.files().entrySet().stream()
        .filter(entry -> entry.getKey().endsWith(".java"))
        .map(entry -> new InMemoryFileInputObject(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());

    ClassFileManager manager = new ClassFileManager(
        javaCompiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)
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

    // ugly, but we have a circular reference here
    AtomicReference<ClassLoader> classLoaderReference = new AtomicReference<>();

    if (!successful) {
      compiledFiles = Collections.emptyList();
    } else {
      compiledFiles = compilationUnits.stream()
          .map(file -> ImmutableCompiledFile.builder()
              .classLoaderSupplier(classLoaderReference::get)
              .classFile(manager.getForClassPath(file.getName()).getContent())
              .content(file.getContent())
              .qualifiedName(
                  file.getName()
                      .replace("/", ".")
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

    ImmutableCompiledSubmission compiledSubmission = ImmutableCompiledSubmission.builder()
        .compilationOutput(compilationOutput)
        .compiledFiles(compiledFiles)
        .build();

    classLoaderReference.set(new SubmissionClassLoader(compiledSubmission));

    return compiledSubmission;
  }
}
