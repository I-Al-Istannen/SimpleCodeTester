package me.ialistannen.simplecodetester.jvmcommunication.protocol.slavebound;

import java.util.List;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.ProtocolMessage;
import me.ialistannen.simplecodetester.submission.Submission;

public class CompileAndCheckSubmission extends ProtocolMessage {

  private Submission submission;
  private List<String> checks;

  /**
   * Creates a new message.
   *
   * @param uid the uid of the client
   * @param submission the submission to compile and check
   * @param checks a list with the fully qualified name of all checks to run
   */
  public CompileAndCheckSubmission(String uid, Submission submission, List<String> checks) {
    super(uid);
    this.submission = submission;
    this.checks = checks;
  }

  /**
   * Returns the {@link Submission} to compile.
   *
   * @return the submission to compile
   */
  public Submission getSubmission() {
    return submission;
  }

  /**
   * Returns a list with the fully qualified name of all checks to run.
   *
   * @return a list with the fully qualified name of all checks to run
   */
  public List<String> getChecks() {
    return checks;
  }

  @Override
  public String toString() {
    return "CompileAndCheckSubmission{" +
        "uid=" + getUid() +
        ", submission=" + submission +
        "m checks=" + checks +
        '}';
  }
}
