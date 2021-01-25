package me.ialistannen.simplecodetester.checks;

public enum CheckType {
  /**
   * A check that checks program in and output.
   */
  IO,
  /**
   * A check that just verifies the imports.
   */
  INTERLEAVED_IO,
  /**
   * A check of unknown type.
   */
  UNKNOWN,
}
