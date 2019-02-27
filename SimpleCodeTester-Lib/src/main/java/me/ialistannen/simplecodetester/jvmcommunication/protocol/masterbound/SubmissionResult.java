package me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound;

import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.checks.SubmissionCheckResult;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.ProtocolMessage;

/**
 * Sends the result of running the submission to the master.
 */
public class SubmissionResult extends ProtocolMessage {

  private String fileName;
  private CheckResult result;

  public SubmissionResult(String fileName, CheckResult result, String uid) {
    super(uid);
    this.fileName = fileName;
    this.result = result;
  }

  /**
   * Returns the submission check result.
   *
   * @return the {@link SubmissionCheckResult}
   */
  public CheckResult getResult() {
    return result;
  }

  /**
   * Returns the filename of the file the result belongs to.
   *
   * @return the filename of the file the result belongs to
   */
  public String getFileName() {
    return fileName;
  }

  @Override
  public String toString() {
    return "SubmissionResult{" +
        "fileName='" + fileName + '\'' +
        ", result=" + result +
        '}';
  }
}