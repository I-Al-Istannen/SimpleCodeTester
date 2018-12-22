package me.ialistannen.simplecodetester.execution.slave;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import me.ialistannen.simplecodetester.submission.Submission;
import me.ialistannen.simplecodetester.test.DefaultImportCheck;
import me.ialistannen.simplecodetester.test.TestOutput;

public class UntrustedCodeJvmStarter {

  /**
   * Runs a given submission in anew VM.
   *
   * @param port the port  the master listens on
   * @param submission the {@link Submission} to run
   * @param uid the uid of the client
   * @param classPath the class path to use. Should at least contain the class file of your own
   */
  public void runSubmission(int port, Submission submission, String uid, String... classPath) {
    try {
      List<String> command = buildArguments(port, submission, uid, classPath);
      ProcessBuilder processBuilder = new ProcessBuilder(command);

      System.out.println(String.join(" ", processBuilder.command()));
      processBuilder.directory(submission.basePath().toFile())
          .start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private List<String> buildArguments(int port, Submission submission, String uid,
      String... classpath) {

    return Arrays.asList(
        "/lib/jvm/java-11-openjdk/bin/java",
        // classpath only added when it is specified
        classpath.length == 0 ? "" : "-cp",
        String.join(":", classpath),
        // policy
        "-Djava.security.policy=="
            + getClass().getResource("/SlavePolicy.policy").toExternalForm(),
        // main class
        UntrustedJvmMain.class.getName(),
        Integer.toString(port),
        uid,
        submission.basePath().toAbsolutePath().toString(),
        DefaultImportCheck.class.getName(),
        TestOutput.class.getName()
    );
  }
}
