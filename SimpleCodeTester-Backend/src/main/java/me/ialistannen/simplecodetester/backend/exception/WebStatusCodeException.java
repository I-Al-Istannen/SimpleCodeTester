package me.ialistannen.simplecodetester.backend.exception;

import org.springframework.http.HttpStatus;

public class WebStatusCodeException extends CodeTesterException {

  private final HttpStatus status;

  public WebStatusCodeException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }

  /**
   * @return the {@link HttpStatus} to set
   */
  public HttpStatus getStatus() {
    return status;
  }
}
