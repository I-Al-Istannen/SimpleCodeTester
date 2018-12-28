package me.ialistannen.simplecodetester.backend.exception;

public class CheckAlreadyRunningException extends CodeTesterException {

  public CheckAlreadyRunningException() {
    super("There is a check already running!");
  }
}
