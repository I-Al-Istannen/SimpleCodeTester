package me.ialistannen.simplecodetester.backend.exception;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WebStatusExceptionMapperAdvice {

  @ExceptionHandler(WebStatusCodeException.class)
  public void setStatusAndRemap(WebStatusCodeException exception, HttpServletResponse response)
      throws IOException {
    response.sendError(exception.getStatus().value(), exception.getMessage());
  }
}
