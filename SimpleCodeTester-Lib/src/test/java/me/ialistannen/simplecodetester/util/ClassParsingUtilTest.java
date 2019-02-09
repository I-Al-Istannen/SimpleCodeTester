package me.ialistannen.simplecodetester.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClassParsingUtilTest {

  @ParameterizedTest(name = "\"{0}\" should be {1}.")
  @CsvSource({
      "class Test, Test",
      "public class Test, Test",
      "interface Test, Test",
      "public interface Test, Test",
      "enum Test, Test",
      "public enum Test, Test",
      "@interface Test, Test",
      "public @interface Test, Test"
  })
  void retrieveName(String input, String className) {
    assertEquals(
        className,
        ClassParsingUtil.getClassName(input).get()
    );
  }

  @ParameterizedTest(name = "\"{0}\" should be {1}.")
  @CsvSource({
      "/*\\n * Hello\\n */\\nclass Test, Test",
      "/*\\n * Hello\\n */\\npublic class Test, Test",
      "/*\\n * Hello\\n */\\ninterface Test, Test",
      "/*\\n * Hello\\n */\\npublic interface Test, Test",
      "/*\\n * Hello\\n */\\nenum Test, Test",
      "/*\\n * Hello\\n */\\npublic enum Test, Test",
      "/*\\n * Hello\\n */\\n@interface Test, Test",
      "/*\\n * Hello\\n */\\npublic @interface Test, Test"
  })
  void retrieveNameWithJavadoc(String input, String className) {
    assertEquals(
        className,
        ClassParsingUtil.getClassName(input).get()
    );
  }

  @ParameterizedTest(name = "\"{0}\" should be {1}.")
  @CsvSource({
      "package me.ialistannen;\\nclass Test, me.ialistannen",
      "package me.ialistannen;\\npublic class Test, me.ialistannen",
      "package me.ialistannen;\\ninterface Test, me.ialistannen",
      "package me.ialistannen;\\npublic interface Test, me.ialistannen",
      "package me.ialistannen;\\nenum Test, me.ialistannen",
      "package me.ialistannen;\\npublic enum Test, me.ialistannen",
      "package me.ialistannen;\\n@interface Test, me.ialistannen",
      "package me.ialistannen;\\npublic @interface Test, me.ialistannen"
  })
  void retrievePackage(String input, String packageName) {
    assertEquals(
        packageName,
        ClassParsingUtil.getPackage(input).get()
    );
  }
}