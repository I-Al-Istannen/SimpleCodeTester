package me.ialistannen.simplecodetester.submission;

import java.nio.file.Path;
import me.ialistannen.simplecodetester.exceptions.CompiledClassNotLoadableException;
import org.immutables.value.Value;

/**
 * A submitted file that was successfully compiled.
 */
@Value.Immutable
public abstract class CompiledFile {

  /**
   * Returns the path this file's ".class" file.
   *
   * @return the path this file's ".class" file
   */
  public abstract Path classFile();

  /**
   * Returns the path this file's ".java" file.
   *
   * @return the path this file's ".java" file
   */
  public abstract Path sourceFile();

  /**
   * Returns the fully qualified name of this class.
   *
   * @return the qualified name of this class
   */
  public abstract String qualifiedName();

  /**
   * The classloader that should be used to load this class.
   *
   * @return classloader that should be used to load this class
   * @see #asClass()
   */
  public abstract ClassLoader classLoader();

  /**
   * Returns this CompiledFile as a java class.
   *
   * @return the loaded java class for this CompiledFile
   * @throws CompiledClassNotLoadableException if the class was not found for some reason
   */
  public Class<?> asClass() {
    try {
      return Class.forName(qualifiedName(), true, classLoader());
    } catch (ClassNotFoundException e) {
      throw new CompiledClassNotLoadableException(qualifiedName(), e);
    }
  }
}
