package me.ialistannen.simplecodetester.execution.slave;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
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
   */
  public void runSubmission(int port, Submission submission, String uid) {
    try {
      ProcessBuilder processBuilder = new ProcessBuilder(
          "/lib/jvm/java-11-openjdk/bin/java",
          "-cp",
          Paths.get(
              UntrustedCodeJvmStarter.class.getProtectionDomain().getCodeSource().getLocation()
                  .toURI()
          ).toAbsolutePath().toString()
              + ":/home/i_al_istannen/Programming/Uni/SimpleCodeTester/target/SimpleCodeTester.jar",
          "-Djava.security.policy=="
              + getClass().getResource("/SlavePolicy.policy").toExternalForm(),
          UntrustedJvmMain.class.getName(),
          Integer.toString(port),
          uid,
          submission.basePath().toAbsolutePath().toString(),
          DefaultImportCheck.class.getName(),
          TestOutput.class.getName()
      );

      System.out.println(String.join(" ", processBuilder.command()));
      processBuilder.directory(submission.basePath().toFile())
          .start();
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
