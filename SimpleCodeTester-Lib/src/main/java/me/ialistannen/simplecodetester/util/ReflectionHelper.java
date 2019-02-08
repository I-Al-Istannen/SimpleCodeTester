package me.ialistannen.simplecodetester.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A small helper class for dealing with reflection.
 */
public final class ReflectionHelper {

  private ReflectionHelper() {
    throw new UnsupportedOperationException("No instantiation");
  }

  /**
   * Returns a predicate matching whether a class has a main class.
   *
   * @param extractor the extractor to convert the given object to a class
   * @param <T> the type of the object
   * @return a predicate checking {@link #hasMainMethod(Class)}
   */
  public static <T> Predicate<T> hasMain(Function<T, Class<?>> extractor) {
    return t -> hasMainMethod(extractor.apply(t));
  }

  /**
   * Checks if the given class has a JVM main method.
   *
   * @param target the class to check
   * @return true if the given class has a main method
   */
  public static boolean hasMainMethod(Class<?> target) {
    Method mainMethod;
    try {
      mainMethod = target.getMethod("main", String[].class);
    } catch (NoSuchMethodException e) {
      return false;
    }

    return mainMethod.getReturnType() == void.class
        && Modifier.isStatic(mainMethod.getModifiers())
        && Modifier.isPublic(mainMethod.getModifiers());
  }
}
