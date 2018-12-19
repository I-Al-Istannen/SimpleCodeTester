package me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound;

import me.ialistannen.simplecodetester.jvmcommunication.protocol.ProtocolMessage;

public class SlaveStarted extends ProtocolMessage {

  public SlaveStarted(String uid) {
    super(uid);
  }

  @Override
  public String toString() {
    return "SlaveStarted{"
        + "uid=" + getUid()
        + "}";
  }
}
