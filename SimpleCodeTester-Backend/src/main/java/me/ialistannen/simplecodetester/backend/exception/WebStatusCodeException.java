package me.ialistannen.simplecodetester.backend.exception;

import org.springframework.http.HttpStatus;

public class WebStatusCodeException extends CodeTesterException {

  private HttpStatus status;

  public WebStatusCodeException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }

  public WebStatusCodeException(String message, Throwable cause, HttpStatus status) {
    super(message, cause);
    this.status = status;
  }

  /**
   * Returns the {@link HttpStatus} to set.
   *
   * @return the {@link HttpStatus} to set
   */
  public HttpStatus getStatus() {
    return status;
  }
}
