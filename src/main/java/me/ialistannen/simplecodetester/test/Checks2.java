package me.ialistannen.simplecodetester.test;

import edu.kit.informatik.Terminal;
import java.util.Arrays;
import java.util.List;
import me.ialistannen.simplecodetester.checks.defaults.MainClassRunnerCheck;
import me.ialistannen.simplecodetester.submission.CompiledFile;

public class Checks2 {

  public static void init(List<CompiledFile> files) {
    MainClassRunnerCheck check = new MainClassRunnerCheck(
        Arrays.asList("Hello", "World"), s -> s.matches("Hello.+")
    ) {
      @Override
      protected String getErrorMessage(CompiledFile file) {
        return "Ouch: '" + Terminal.getOutput() + "'";
      }
    };
    for (CompiledFile file : files) {
      System.out.println("Checking: " + file.qualifiedName());
      System.out.println(check.check(file));
    }
  }
}
