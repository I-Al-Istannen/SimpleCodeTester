package me.ialistannen.simplecodetester.exceptions;

public class CompiledClassNotLoadableException extends RuntimeException {

  public CompiledClassNotLoadableException(String qualifiedName, Throwable cause) {
    super(String.format("The class '%s' was not loadable.", qualifiedName), cause);
  }
}
