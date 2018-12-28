package me.ialistannen.simplecodetester.backend.exception;

public class CheckRunningFailedException extends CodeTesterException {

  public CheckRunningFailedException(String message) {
    super(message);
  }

  public CheckRunningFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
