package me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound;

import me.ialistannen.simplecodetester.jvmcommunication.protocol.ProtocolMessage;

public class SlaveTimedOut extends ProtocolMessage {

  public SlaveTimedOut(String uid) {
    super(uid);
  }

  @Override
  public String toString() {
    return "SlaveTimedOut{" +
        "uid=" + getUid()
        + "}";
  }
}
