package me.ialistannen.simplecodetester.execution.master;

import java.util.List;
import me.ialistannen.simplecodetester.submission.Submission;

class SubmissionCheckEntry {

  private Submission submission;
  private List<String> checks;

  SubmissionCheckEntry(Submission submission, List<String> checks) {
    this.submission = submission;
    this.checks = checks;
  }

  Submission getSubmission() {
    return submission;
  }

  List<String> getCheckSource() {
    return checks;
  }
}
