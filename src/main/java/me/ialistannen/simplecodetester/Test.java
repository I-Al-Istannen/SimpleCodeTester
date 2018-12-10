package me.ialistannen.simplecodetester;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import me.ialistannen.simplecodetester.compilation.CompilationOutput;
import me.ialistannen.simplecodetester.compilation.java8.Java8FileCompiler;
import me.ialistannen.simplecodetester.execution.SubmissionClassLoader;
import org.joor.Reflect;

public class Test {

  public static void main(String[] args) throws Exception {
//    testCompile();
//    createNewTest();
//    System.out.println();
//    System.out.println("-----------------");
//    System.out.println();
//    createNewTest();
    Java8FileCompiler compiler = new Java8FileCompiler();
    CompilationOutput compilationOutput = compiler.compileFolder(Paths.get("/tmp/test"));

    if (!compilationOutput.successful()) {
      System.out.println("Error :(");
      System.out.println(compilationOutput.output());
      System.out.println(compilationOutput.diagnostics());
      return;
    }
    System.out.println("Compiled: " + compilationOutput);

    SubmissionClassLoader submissionClassLoader = new SubmissionClassLoader(
        Paths.get("/tmp/test"),
        Pattern.compile("edu.kit.informatik.*"),
        Pattern.compile("me.ialistannen.simplecodetester.checks.defaults.*"),
        Pattern.compile("me.ialistannen.simplecodetester.test.*")
    );

    Reflect.on("me.ialistannen.simplecodetester.test.Checks2", submissionClassLoader)
        .create()
        .call("init", compilationOutput.files());
  }

  private static void createNewTest() throws Exception {
    SubmissionClassLoader loader = new SubmissionClassLoader(
        Paths.get("/home/i_al_istannen/Programming/Uni/SimpleCodeTester/src/main/java"),
        Pattern.compile("edu.kit.informatik.*"),
        Pattern.compile("me.ialistannen.simplecodetester.checks.defaults.*"),
        Pattern.compile("me.ialistannen.simplecodetester.test.*")
    );

    Reflect.on("me.ialistannen.simplecodetester.test.Checks", loader)
        .create()
        .call("init");
  }

  private static void testCompile() throws IOException {
    Path folder = Paths.get("/tmp/test");
    Files.walk(folder)
        .filter(path -> path.toString().endsWith(".class"))
        .forEach(path -> {
          try {
            Files.deleteIfExists(path);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });

    Java8FileCompiler compiler = new Java8FileCompiler();
    CompilationOutput compilationOutput = compiler.compileFolder(folder);

    System.out.println("Successful:");
    System.out.println(compilationOutput.successful());
    System.out.println("----------------");
    System.out.println("Output:");
    System.out.println(compilationOutput.output());
    System.out.println("----------------");
    System.out.println("Diagnostics:");
    for (Entry<String, List<String>> entry : compilationOutput.diagnostics().entrySet()) {
      System.out.println("File: " + entry.getKey());
      for (String s : entry.getValue()) {
        System.out.println(s);
      }
    }
    System.out.println("----------------");
  }
}
