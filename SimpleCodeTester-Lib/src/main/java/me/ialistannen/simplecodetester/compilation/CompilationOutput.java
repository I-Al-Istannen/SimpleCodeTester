package me.ialistannen.simplecodetester.compilation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

/**
 * The compiler output for a whole collection of files.
 */
@Gson.TypeAdapters
@JsonSerialize(as = ImmutableCompilationOutput.class)
@JsonDeserialize(as = ImmutableCompilationOutput.class)
@Value.Immutable
public abstract class CompilationOutput {

  /**
   * Returns diagnostic warnings for each class.
   *
   * @return the diagnostic warnings for the files
   */
  public abstract Map<String, List<String>> diagnostics();

  /**
   * Checks whether the compilation was successful.
   *
   * @return whether the compilation was successful
   */
  public abstract boolean successful();

  /**
   * Returns any compiler output.
   *
   * @return the compiler output or an empty string if none
   */
  public abstract String output();

  /**
   * Returns a list with all compiled files.
   *
   * @return all compiled files. Empty if an error occurred.
   */
  public abstract Collection<CompiledFile> files();

}
