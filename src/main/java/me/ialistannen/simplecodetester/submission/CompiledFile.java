package me.ialistannen.simplecodetester.submission;

import java.nio.file.Path;
import me.ialistannen.simplecodetester.exceptions.CompiledClassNotLoadableException;
import org.immutables.value.Value;

@Value.Immutable
public abstract class CompiledFile {

  public abstract Path classFile();

  public abstract Path sourceFile();

  public abstract String qualifiedName();

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
