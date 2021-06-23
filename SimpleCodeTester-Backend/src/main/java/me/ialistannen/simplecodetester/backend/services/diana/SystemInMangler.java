package me.ialistannen.simplecodetester.backend.services.diana;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import spoon.Launcher;
import spoon.OutputType;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.compiler.VirtualFile;

public class SystemInMangler {

  private static final String SCANNER_CLASS_NAME =
      "me.ialistannen.simplecodetester.execution.diana.TerminalScanner";
  private static final String READER_CLASS_NAME =
      "me.ialistannen.simplecodetester.execution.diana.TerminalBufferedReader";

  public void replace(Map<String, String> javaFiles) {
    Launcher launcher = new Launcher();
    launcher.getEnvironment().setOutputType(OutputType.NO_OUTPUT);
    launcher.getEnvironment().setAutoImports(false);
    launcher.getEnvironment().disableConsistencyChecks();

    for (var entry : javaFiles.entrySet()) {
      launcher.addInputResource(new VirtualFile(
          entry.getValue().replaceAll("\n", ""),
          entry.getKey().replace("/", ".").replace(".java", "")
      ));
    }

    CtModel model = launcher.buildModel();
    scanner(javaFiles, launcher, model);
    reader(javaFiles, launcher, model);
  }

  private void scanner(Map<String, String> javaFiles, Launcher launcher, CtModel model) {
    for (CtType<?> type : model.getAllTypes()) {
      List<CtConstructorCall<?>> constructorCalls =
          type.getElements(new TypeFilter<>(CtConstructorCall.class));
      List<CtConstructorCall<?>> scannerCalls = getScannerCalls(constructorCalls);
      testMe(
          javaFiles,
          launcher,
          type,
          scannerCalls,
          SCANNER_CLASS_NAME,
          "java.util.Scanner"
      );
    }
  }

  private void reader(Map<String, String> javaFiles, Launcher launcher, CtModel model) {
    for (CtType<?> type : model.getAllTypes()) {
      List<CtConstructorCall<?>> constructorCalls =
          type.getElements(new TypeFilter<>(CtConstructorCall.class));
      List<CtConstructorCall<?>> bufferedReaderCalls = getReaderCalls(constructorCalls);

      testMe(
          javaFiles,
          launcher,
          type,
          bufferedReaderCalls,
          READER_CLASS_NAME,
          "java.io.BufferedReader"
      );
    }
  }

  private void testMe(Map<String, String> javaFiles, Launcher launcher, CtType<?> type,
      List<CtConstructorCall<?>> bufferedReaderCalls, String className, String javaClassName) {
    for (CtConstructorCall<?> ctConstructorCall : bufferedReaderCalls) {
      CtConstructorCall<?> newConstructor;
      newConstructor = launcher.getFactory().createConstructorCall(
          launcher.getFactory().createReference(className),
          ctConstructorCall.getArguments().toArray(CtExpression[]::new));

      ctConstructorCall.replace(newConstructor);
    }

    List<CtTypeReference<?>> typeReferences = type.getElements(
        new TypeFilter<>(CtTypeReference.class)
    );

    typeReferences.removeIf(v -> !v.getQualifiedName().equals(javaClassName));

    for (CtTypeReference<?> ctTypeReference : typeReferences) {
      var ref = ctTypeReference.getFactory().createReference(className);
      ctTypeReference.replace(ref);
    }
    if (!bufferedReaderCalls.isEmpty()) {
      javaFiles.put(
          mangleFqnName(type.getQualifiedName()),
          type.getFactory().CompilationUnit().getOrCreate(type).prettyprint()
      );
    }
  }

  private static String mangleFqnName(String fqn) {
    return fqn.replace(".", "/") + ".java";
  }

  private List<CtConstructorCall<?>> getScannerCalls(List<CtConstructorCall<?>> constructorCalls) {
    return constructorCalls.stream().filter(v -> v.getType() != null)
        .filter(v -> v.getType().getQualifiedName().equals("java.util.Scanner"))
        .collect(Collectors.toList());
  }

  private List<CtConstructorCall<?>> getReaderCalls(List<CtConstructorCall<?>> constructorCalls) {
    return constructorCalls.stream().filter(v -> v.getType() != null)
        .filter(v -> v.getType().getQualifiedName().equals("java.io.BufferedReader"))
        .collect(Collectors.toList());
  }

  private List<CtConstructorCall<?>> getFileReaderCalls(List<CtConstructorCall<?>> constructorCalls) {
    return constructorCalls.stream().filter(v -> v.getType() != null)
        .filter(v -> v.getType().getQualifiedName().equals("java.io.FileReader"))
        .collect(Collectors.toList());
  }
}
