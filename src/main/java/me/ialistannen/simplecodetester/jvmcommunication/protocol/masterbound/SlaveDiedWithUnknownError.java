package me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound;

import me.ialistannen.simplecodetester.jvmcommunication.protocol.ProtocolMessage;

public class SlaveDiedWithUnknownError extends ProtocolMessage {

  private final String message;

  public SlaveDiedWithUnknownError(String uid, String message) {
    super(uid);
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "SlaveDiedWithUnknownError{" +
        "uid=" + getUid() +
        ", message='" + message + '\'' +
        '}';
  }
}
