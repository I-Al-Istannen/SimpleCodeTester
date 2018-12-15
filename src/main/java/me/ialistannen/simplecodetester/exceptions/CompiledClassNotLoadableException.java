package me.ialistannen.simplecodetester.exceptions;

public class CompiledClassNotLoadableException extends RuntimeException {

  public CompiledClassNotLoadableException(String qualifiedName, Throwable cause) {
    super(String.format("The class '%s' was not loadable.", qualifiedName), cause);
  }

  public CompiledClassNotLoadableException(String qualifiedName, String message) {
    super(String.format("The class '%s' was not loadable: %s", qualifiedName, message));
  }
}
