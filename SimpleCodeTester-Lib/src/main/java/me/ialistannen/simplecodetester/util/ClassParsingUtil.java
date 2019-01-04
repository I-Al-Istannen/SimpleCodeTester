package me.ialistannen.simplecodetester.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ClassParsingUtil {

  private static final Pattern CLASS_DECLARATION_PATTERN = Pattern
      .compile("(class|interface|@interface|enum) (\\w+) ?");
  private static final Pattern PACKAGE_PATTERN = Pattern.compile("package (\\S+);");

  /**
   * Returns the name of the class.
   *
   * @param input the input to check
   * @return the class name if found
   */
  public static Optional<String> getClassName(String input) {
    Matcher matcher = CLASS_DECLARATION_PATTERN.matcher(input);
    if (!matcher.find()) {
      return Optional.empty();
    }
    return Optional.ofNullable(matcher.group(2));
  }

  /**
   * Returns the package of the class.
   *
   * @param input the input to check
   * @return the package if found
   */
  public static Optional<String> getPackage(String input) {
    Matcher matcher = PACKAGE_PATTERN.matcher(input);
    if (!matcher.find()) {
      return Optional.empty();
    }
    return Optional.ofNullable(matcher.group(1));
  }

}
