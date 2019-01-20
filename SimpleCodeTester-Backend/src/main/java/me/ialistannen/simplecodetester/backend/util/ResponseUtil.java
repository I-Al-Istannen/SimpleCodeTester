package me.ialistannen.simplecodetester.backend.util;

import java.util.Map;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@UtilityClass
public class ResponseUtil {

  /**
   * Returns an error {@link ResponseEntity} with the given status and message.
   *
   * @param status the {@link HttpStatus} code
   * @param message the message
   * @return the created response entity
   */
  public static <T> ResponseEntity<T> error(HttpStatus status, String message) {
    @SuppressWarnings("unchecked")
    ResponseEntity<T> error = (ResponseEntity<T>) ResponseEntity.status(status)
        .body(Map.of("error", message));
    return error;
  }
}
