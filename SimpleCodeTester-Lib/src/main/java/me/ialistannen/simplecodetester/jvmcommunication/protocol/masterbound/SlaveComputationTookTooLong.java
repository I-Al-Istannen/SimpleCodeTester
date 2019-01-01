package me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound;

import me.ialistannen.simplecodetester.jvmcommunication.protocol.ProtocolMessage;

public class SlaveComputationTookTooLong extends ProtocolMessage {

  public SlaveComputationTookTooLong(String uid) {
    super(uid);
  }

  @Override
  public String toString() {
    return "SlaveComputationTookTooLong{"
        + "uid=" + getUid()
        + "}";
  }
}
