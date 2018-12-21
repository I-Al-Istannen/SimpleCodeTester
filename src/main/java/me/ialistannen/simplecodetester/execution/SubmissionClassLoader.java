package me.ialistannen.simplecodetester.execution;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import me.ialistannen.simplecodetester.submission.Submission;

/**
 * A ClassLoader for {@link Submission Submissions} that reads from a folder and caches a list of
 * passed classes.
 *
 * Those cached classes will be unique to this classloader, so each classloader has its own
 * instance.
 */
public class SubmissionClassLoader extends URLClassLoader {

  /**
   * Creates a new SubmissionClassLoader loading from the given folder and redefining the classes
   * matching the passed patterns.
   *
   * @param folder the folder to load from therefore unique to this classloader
   * @throws MalformedURLException see {@link Path#toUri()} and {@link URI#toURL()}
   */
  public SubmissionClassLoader(Path folder)
      throws MalformedURLException {
    super(
        new URL[]{folder.toAbsolutePath().toUri().toURL()},
        SubmissionClassLoader.class.getClassLoader() // allow class requests to bubble up
    );
  }
}
