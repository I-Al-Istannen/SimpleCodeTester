package me.ialistannen.simplecodetester.jvmcommunication.protocol;

/**
 * A message that can be send between a master and a slave.
 */
public abstract class ProtocolMessage {

  private String className;
  private String uid;

  public ProtocolMessage(String uid) {
    this.className = getClass().getName();
    this.uid = uid;
  }

  /**
   * Returns the name of this message's class.
   *
   * @return the name of this message's class
   */
  public String getClassName() {
    return className;
  }

  /**
   * Returns the uid for the message.
   *
   * @return the uid for the message
   */
  public String getUid() {
    return uid;
  }

  @Override
  public String toString() {
    return "ProtocolMessage{" +
        "className='" + className + '\'' +
        ", uid='" + uid + '\'' +
        '}';
  }
}
