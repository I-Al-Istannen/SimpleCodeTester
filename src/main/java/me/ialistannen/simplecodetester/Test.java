package me.ialistannen.simplecodetester;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.checks.CheckRunner;
import me.ialistannen.simplecodetester.checks.SubmissionCheckResult;
import me.ialistannen.simplecodetester.compilation.java8.Java8FileCompiler;
import me.ialistannen.simplecodetester.execution.SubmissionClassLoader;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.submission.CompiledSubmission;
import me.ialistannen.simplecodetester.submission.ImmutableSubmission;
import me.ialistannen.simplecodetester.submission.Submission;
import me.ialistannen.simplecodetester.test.DefaultImportCheck;

public class Test {

  public static void main(String[] args) throws Exception {
    Java8FileCompiler compiler = new Java8FileCompiler();

    Submission submission = ImmutableSubmission.builder()
        .basePath(Paths.get("/tmp/test"))
        .classLoader(new SubmissionClassLoader(Paths.get("/tmp/test")))
        .build();

    CompiledSubmission compiledSubmission = compiler.compileSubmission(submission);

    CheckRunner checkRunner = new CheckRunner(List.of(new DefaultImportCheck()));

    SubmissionCheckResult checkResult = checkRunner.checkSubmission(compiledSubmission);

    for (Entry<CompiledFile, List<CheckResult>> entry : checkResult.fileResults().entrySet()) {
      System.out.println(entry);
    }

    System.out.println(checkResult.overallSuccessful());
  }
}
