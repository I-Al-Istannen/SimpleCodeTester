package me.ialistannen.simplecodetester.exceptions;

/**
 * An exception indicating that I/O is not supported.
 */
@SuppressStacktrace
public class UnsupportedIoException extends RuntimeException {

  public UnsupportedIoException(String message) {
    super(message);
  }
}
