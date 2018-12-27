package me.ialistannen.simplecodetester.execution.master;

import java.util.List;
import me.ialistannen.simplecodetester.submission.Submission;

class SubmissionCheckEntry {

  private Submission submission;
  private List<String> checkNames;

  SubmissionCheckEntry(Submission submission, List<String> checkNames) {
    this.submission = submission;
    this.checkNames = checkNames;
  }

  Submission getSubmission() {
    return submission;
  }

  List<String> getCheckNames() {
    return checkNames;
  }
}
