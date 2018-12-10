package me.ialistannen.simplecodetester.test;

import edu.kit.informatik.Terminal;
import java.nio.file.Paths;
import java.util.List;
import me.ialistannen.simplecodetester.checks.defaults.MainClassRunnerCheck;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.submission.ImmutableCompiledFile;

public class Checks extends MainClassRunnerCheck {

  public Checks() {
    super(List.of("Hello", "World"), s -> s.equals("HelloWorld\n"));
  }

  public static void init() {
    ImmutableCompiledFile compiledFile = ImmutableCompiledFile.builder()
        .sourceFile(Paths.get(
            "/home/i_al_istannen/Programming/Uni/SimpleCodeTester/src/main/java/me/ialistannen/simplecodetester/test/Test.java"))
        .classFile(Paths.get(
            "/home/i_al_istannen/Programming/Uni/SimpleCodeTester/target/classes/me/ialistannen/simplecodetester/test/Test.class"))
        .qualifiedName("me.ialistannen.simplecodetester.test.Test")
        .build();

    System.out.println(new Checks().check(compiledFile));
  }

  @Override
  protected String getErrorMessage(CompiledFile file) {
    return "You were wrong (logically): '" + Terminal.getOutput() + "'";
  }
}
