package me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound;

import me.ialistannen.simplecodetester.jvmcommunication.protocol.ProtocolMessage;

/**
 * Sent when the slave starts a check on a file.
 */
public class StartedCheck extends ProtocolMessage {

  private final String checkName;

  /**
   * Creates a new message.
   *
   * @param uid the uid of the slave
   * @param checkContext the name of the check that was started and some context
   */
  public StartedCheck(String uid, String checkContext) {
    super(uid);
    this.checkName = checkContext;
  }

  /**
   * Returns the name of the check that was started and some context.
   *
   * @return the name of the check that was started and some context
   */
  public String getCheckContext() {
    return checkName;
  }
}
