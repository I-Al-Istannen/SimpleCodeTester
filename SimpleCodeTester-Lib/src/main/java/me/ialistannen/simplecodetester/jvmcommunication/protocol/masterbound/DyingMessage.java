package me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound;

import me.ialistannen.simplecodetester.jvmcommunication.protocol.ProtocolMessage;

/**
 * A message telling the master that the slave is dying.
 */
public class DyingMessage extends ProtocolMessage {

  public DyingMessage(String uid) {
    super(uid);
  }

  @Override
  public String toString() {
    return "DyingMessage{"
        + "uid=" + getUid()
        + "}";
  }
}
