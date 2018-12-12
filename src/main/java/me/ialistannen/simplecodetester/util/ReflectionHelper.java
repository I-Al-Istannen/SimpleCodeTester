package me.ialistannen.simplecodetester.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A small helper class for dealing with reflection.
 */
public final class ReflectionHelper {

  private ReflectionHelper() {
    throw new UnsupportedOperationException("No instantiation");
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
