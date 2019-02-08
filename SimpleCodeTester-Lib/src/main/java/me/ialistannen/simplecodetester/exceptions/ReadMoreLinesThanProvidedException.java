package me.ialistannen.simplecodetester.exceptions;

/**
 * Indicates the program read more lines than were provided.
 */
public class ReadMoreLinesThanProvidedException extends RuntimeException {

  public ReadMoreLinesThanProvidedException() {
    super("No input present but you tried to read!");
  }
}
