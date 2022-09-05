package me.ialistannen.simplecodetester.execution.compilation.memory;

import static java.util.stream.Collectors.toMap;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import me.ialistannen.simplecodetester.compilation.CompilationOutput;
import me.ialistannen.simplecodetester.compilation.ImmutableCompilationOutput;
import me.ialistannen.simplecodetester.exceptions.CompilationException;
import me.ialistannen.simplecodetester.execution.compilation.Compiler;
import me.ialistannen.simplecodetester.execution.running.SubmissionClassLoader;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.submission.CompiledSubmission;
import me.ialistannen.simplecodetester.submission.ImmutableCompiledFile;
import me.ialistannen.simplecodetester.submission.ImmutableCompiledSubmission;
import me.ialistannen.simplecodetester.submission.Submission;

/**
 * A java 8 compiler that works in-memory.
 */
public class Java17InMemoryCompiler implements Compiler {

  @Override
  public CompiledSubmission compileSubmission(Submission submission) {
    JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
    StringWriter output = new StringWriter();

    List<InMemoryFileInputObject> compilationUnits = submission.files().entrySet().stream()
        .filter(entry -> entry.getKey().endsWith(".java"))
        .filter(entry -> !entry.getKey().endsWith("package-info.java"))
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
        Arrays.asList("-Xlint:all", "-Xlint:-processing", "-Xlint:-serial", "--release=17"),
        null,
        compilationUnits
    )
        .call();

    List<CompiledFile> compiledFiles;

    // ugly, but we have a circular reference here
    AtomicReference<Supplier<ClassLoader>> classLoaderReference = new AtomicReference<>();

    if (!successful) {
      compiledFiles = Collections.emptyList();
    } else {
      for (InMemoryFileInputObject file : compilationUnits) {
        if (manager.getForClassPath(file.getName()) == null) {
          throw new CompilationException(
              String.format("No class file found for source file '%s'.", file.getName())
          );
        }
      }
      compiledFiles = compilationUnits.stream()
          .map(file -> ImmutableCompiledFile.builder()
              .classLoaderSupplier(() -> classLoaderReference.get().get())
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

    CompilationOutput compilationOutput = ImmutableCompilationOutput.builder()
        .successful(successful)
        .output(output.toString())
        .diagnostics(diagnostics)
        .files(compiledFiles)
        .build();

    Map<String, InMemoryOutputObject> auxiliaryClasses = new HashMap<>(manager.getAll());
    for (InMemoryFileInputObject unit : compilationUnits) {
      auxiliaryClasses.remove(manager.sanitizeToClassName(unit.getName()));
    }

    CompiledSubmission compiledSubmission = ImmutableCompiledSubmission.builder()
        .compilationOutput(compilationOutput)
        .compiledFiles(compiledFiles)
        .generatedAuxiliaryClasses(
            auxiliaryClasses.entrySet().stream()
                .collect(toMap(Entry::getKey, entry -> entry.getValue().getContent()))
        )
        .build();

    classLoaderReference.set(getMemoizingClassLoaderSupplier(compiledSubmission));

    return compiledSubmission;
  }

  private Supplier<ClassLoader> getMemoizingClassLoaderSupplier(
      CompiledSubmission compiledSubmission) {
    return new Supplier<>() {
      private SubmissionClassLoader last;

      @Override
      public ClassLoader get() {
        if (last == null || last.hasClassWithStaticField()) {
          return last = new SubmissionClassLoader(compiledSubmission);
        }
        return last;
      }
    };
  }
}
