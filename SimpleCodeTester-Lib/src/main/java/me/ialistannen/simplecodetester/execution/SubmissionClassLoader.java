package me.ialistannen.simplecodetester.execution;

import static java.util.stream.Collectors.toMap;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.submission.CompiledSubmission;
import me.ialistannen.simplecodetester.submission.Submission;

/**
 * A ClassLoader for {@link Submission Submissions} that reads from a folder and caches a list of
 * passed classes.
 *
 * Those cached classes will be unique to this classloader, so each classloader has its own
 * instance.
 */
public class SubmissionClassLoader extends URLClassLoader {

  private final Map<String, byte[]> compiledClasses;

  /**
   * Creates a new SubmissionClassLoader loading from the given folder and redefining the classes
   * matching the passed patterns.
   *
   * @param submission the submission to load
   */
  public SubmissionClassLoader(CompiledSubmission submission) {
    // Allow class requests to bubble up
    super(new URL[0], SubmissionClassLoader.class.getClassLoader());

    this.compiledClasses = submission.compiledFiles().stream()
        .collect(toMap(
            CompiledFile::qualifiedName, CompiledFile::classFile, (a, b) -> a, HashMap::new
        ));
    this.compiledClasses.putAll(submission.generatedAuxiliaryClasses());
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    if (compiledClasses.containsKey(name)) {
      byte[] bytes = compiledClasses.get(name);
      return defineClass(name, bytes, 0, bytes.length);
    }
    return super.findClass(name);
  }
}
