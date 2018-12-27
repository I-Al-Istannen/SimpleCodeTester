package me.ialistannen.simplecodetester.submission;

import java.util.Optional;
import java.util.function.Supplier;
import me.ialistannen.simplecodetester.exceptions.CompiledClassNotLoadableException;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

/**
 * A submitted file that was successfully compiled.
 */
@Gson.TypeAdapters
@Value.Immutable
public abstract class CompiledFile {

  /**
   * Returns the bytes of this file's ".class" file.
   *
   * @return the bytes of this file's ".class" file
   */
  public abstract byte[] classFile();

  /**
   * Returns the file source content.
   *
   * @return the file source content
   */
  public abstract String content();

  /**
   * Returns the fully qualified name of this class.
   *
   * @return the qualified name of this class
   */
  public abstract String qualifiedName();

  /**
   * Supplies the classloader that should be used to load this class, if any.
   *
   * @return classloader that should be used to load this class, if any
   * @see #asClass()
   */
  @Gson.Ignore
  public abstract Optional<Supplier<ClassLoader>> classLoaderSupplier();

  /**
   * Returns this CompiledFile as a java class.
   *
   * @return the loaded java class for this CompiledFile
   * @throws CompiledClassNotLoadableException if the class was not found for some reason
   */
  public Class<?> asClass() {
    try {
      ClassLoader classLoader = classLoaderSupplier().orElseThrow().get();

      return Class.forName(qualifiedName(), true, classLoader);
    } catch (ClassNotFoundException e) {
      throw new CompiledClassNotLoadableException(qualifiedName(), e);
    }
  }
}
