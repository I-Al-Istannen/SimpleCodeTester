package me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound;

import me.ialistannen.simplecodetester.checks.SubmissionCheckResult;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.ProtocolMessage;

/**
 * Sends the result of running the submission to the master.
 */
public class SubmissionResult extends ProtocolMessage {

  private SubmissionCheckResult result;

  public SubmissionResult(SubmissionCheckResult result, String uid) {
    super(uid);
    this.result = result;
  }

  /**
   * Returns the submission check result.
   *
   * @return the {@link SubmissionCheckResult}
   */
  public SubmissionCheckResult getResult() {
    return result;
  }

  @Override
  public String toString() {
    return "SubmissionResult{" +
        "uid=" + getUid() +
        ", result=" + result +
        '}';
  }
}