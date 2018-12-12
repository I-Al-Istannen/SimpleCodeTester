package me.ialistannen.simplecodetester.execution;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import me.ialistannen.simplecodetester.submission.Submission;

/**
 * A ClassLoader for {@link Submission Submissions} that reads from a folder and caches a list of
 * passed classes.
 *
 * Those cached classes will be unique to this classloader, so each classloader has its own
 * instance.
 */
public class SubmissionClassLoader extends URLClassLoader {

  private List<Pattern> classesToRedefine;

  private Map<String, Class<?>> myLoadedClasses;

  /**
   * Creates a new SubmissionClassLoader loading from the given folder and redefining the classes
   * matching the passed patterns.
   *
   * @param folder the folder to load from
   * @param classesToRedefine a list of patterns that specify which classes should be redefined and
   * therefore unique to this classloader
   * @throws MalformedURLException see {@link Path#toUri()} and {@link URI#toURL()}
   */
  public SubmissionClassLoader(Path folder, Pattern... classesToRedefine)
      throws MalformedURLException {
    super(
        new URL[]{folder.toAbsolutePath().toUri().toURL()},
        SubmissionClassLoader.class.getClassLoader() // allow class requests to bubble up
    );

    this.myLoadedClasses = new HashMap<>();
    this.classesToRedefine = new ArrayList<>(Arrays.asList(classesToRedefine));

    Collections.addAll(
        this.classesToRedefine,
        Pattern.compile("edu.kit.informatik.Terminal"),
        Pattern.compile("me.ialistannen.simplecodetester.checks.default.*")
    );
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    if (myLoadedClasses.containsKey(name)) {
      return myLoadedClasses.get(name);
    }

    if (classesToRedefine.stream().anyMatch(pattern -> pattern.matcher(name).matches())) {
      byte[] bytes = classAsBytes(name);
      Class<?> definedClass = defineClass(name, bytes, 0, bytes.length);

      myLoadedClasses.put(name, definedClass);

      return definedClass;
    }

    return super.loadClass(name);
  }

  private byte[] classAsBytes(String name) {
    String classPath = name.replace(".", "/") + ".class";

    if (getResource(classPath) == null) {
      throw new RuntimeException("Class not found");
    }

    try (InputStream inputStream = getResourceAsStream(classPath)) {

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      // We check that getResource exists and if the classloader is not garbage, this call will
      // have succeed too
      assert inputStream != null;
      inputStream.transferTo(outputStream);

      return outputStream.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
