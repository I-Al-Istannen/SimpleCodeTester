package me.ialistannen.simplecodetester;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.checks.SubmissionCheckResult;
import me.ialistannen.simplecodetester.execution.SubmissionClassLoader;
import me.ialistannen.simplecodetester.execution.master.SlaveManager;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SubmissionResult;
import me.ialistannen.simplecodetester.submission.ImmutableSubmission;

public class Test {

  public static void main(String[] args) throws Exception {
    String[] classpath = {
        "/home/i_al_istannen/Programming/Uni/SimpleCodeTester/target/SimpleCodeTester.jar"
    };
    SlaveManager slaveManager = new SlaveManager(
        protocolMessage -> {
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

    Path basePath = Paths.get("/tmp/test/hm");
    ImmutableSubmission submission = ImmutableSubmission.builder()
        .basePath(basePath)
        .classLoader(new SubmissionClassLoader(basePath))
        .build();

    slaveManager.runSubmission(submission, "hello-world");

    Thread.sleep(4000);
    System.out.println("Stopping...");
    slaveManager.stop();
  }
}
