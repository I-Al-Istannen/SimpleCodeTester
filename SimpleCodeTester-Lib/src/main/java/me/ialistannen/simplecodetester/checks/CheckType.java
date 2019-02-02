package me.ialistannen.simplecodetester.checks;

public enum CheckType {
  /**
   * A check that consists of source code that is compiled.
   */
  SOURCE_CODE,
  /**
   * A check that checks program in and output.
   */
  IO,
  /**
   * A check that just verifies the imports.
   */
  IMPORT,
  /**
   * An unknown check type.
   */
  UNKNOWN
}