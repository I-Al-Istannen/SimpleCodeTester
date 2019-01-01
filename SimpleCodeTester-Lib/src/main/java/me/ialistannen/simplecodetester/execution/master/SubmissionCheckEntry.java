package me.ialistannen.simplecodetester.execution.master;

import java.util.List;
import me.ialistannen.simplecodetester.checks.CheckType;
import me.ialistannen.simplecodetester.submission.Submission;
import me.ialistannen.simplecodetester.util.Pair;

class SubmissionCheckEntry {

  private Submission submission;
  private List<Pair<CheckType, String>> checks;

  SubmissionCheckEntry(Submission submission, List<Pair<CheckType, String>> checks) {
    this.submission = submission;
    this.checks = checks;
  }

  Submission getSubmission() {
    return submission;
  }

  List<Pair<CheckType, String>> getChecks() {
    return checks;
  }
}
