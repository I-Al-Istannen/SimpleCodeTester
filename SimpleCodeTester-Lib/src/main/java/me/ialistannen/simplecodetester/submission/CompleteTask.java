package me.ialistannen.simplecodetester.submission;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A complete task that can be passed to the input of an Executor.
 */
public class CompleteTask {

  private final List<String> checks;
  private final Submission submission;

  public CompleteTask(List<String> checks, Submission submission) {
    this.checks = checks;
    this.submission = submission;
  }

  public List<String> getChecks() {
    return Collections.unmodifiableList(checks);
  }

  public Submission getSubmission() {
    return submission;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CompleteTask that = (CompleteTask) o;
    return Objects.equals(checks, that.checks) && Objects.equals(submission,
        that.submission);
  }

  @Override
  public int hashCode() {
    return Objects.hash(checks, submission);
  }

  @Override
  public String toString() {
    return "CompleteTask{" +
        "checks=" + checks +
        ", submission=" + submission +
        '}';
  }
}
