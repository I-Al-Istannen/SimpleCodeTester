package me.ialistannen.simplecodetester;

import java.util.List;
import java.util.Map.Entry;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.checks.SubmissionCheckResult;
import me.ialistannen.simplecodetester.execution.master.SlaveManager;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SubmissionResult;
import me.ialistannen.simplecodetester.submission.ImmutableSubmission;
import me.ialistannen.simplecodetester.submission.Submission;
import me.ialistannen.simplecodetester.test.DefaultImportCheck;
import me.ialistannen.simplecodetester.test.TestOutput;

public class Test {

  public static void main(String[] args) throws Exception {
    String[] classpath = {
        "/home/i_al_istannen/Programming/Uni/SimpleCodeTester/SimpleCodeTester-Lib/target/SimpleCodeTester-Lib.jar"
    };
    SlaveManager slaveManager = new SlaveManager(
        (client, protocolMessage) -> {
          if (protocolMessage instanceof SubmissionResult) {
            SubmissionCheckResult result = ((SubmissionResult) protocolMessage).getResult();
            System.out.println("Result:");
            System.out.println("\tSuccessful: " + result.overallSuccessful());
            for (Entry<String, List<CheckResult>> entry : result.fileResults().entrySet()) {
              System.out.println("\tFile: " + entry.getKey());
              for (CheckResult checkResult : entry.getValue()) {
                if (!checkResult.successful()) {
                  System.out.println(
                      "\t\tFailed: " + checkResult.errorOutput() + "  " + checkResult.message()
                  );
                } else {
                  System.out.println("\t\tSuccessful");
                }
              }
            }
          } else {
            System.out.println("Got Master " + protocolMessage);
          }
        },
        classpath
    );
    slaveManager.start();

    Submission submission = ImmutableSubmission.builder()
        .putFiles(
            "test/Test.java",
            "package test;"
                + "import edu.kit.informatik.Terminal;"
                + "public class Test { public static void main(String[] args) {"
                + " Terminal.printLine(\"Little star!\");"
                + " }}"
        )
        .build();

    slaveManager.runSubmission(
        submission,
        List.of(TestOutput.class.getName(), DefaultImportCheck.class.getName()),
        "hello-world"
    );

    Thread.sleep(4000);
    System.out.println("Stopping...");
    slaveManager.stop();
  }
}
