package me.ialistannen.simplecodetester.execution;

import static java.util.stream.Collectors.toMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
public class SubmissionClassLoader extends SecureClassLoader {

  private final Map<String, byte[]> compiledClasses;
  private final Set<Class<?>> loadedSubmissionClasses;

  /**
   * Creates a new SubmissionClassLoader loading from the given folder and redefining the classes
   * matching the passed patterns.
   *
   * @param submission the submission to load
   */
  public SubmissionClassLoader(CompiledSubmission submission) {
    // Allow class requests to bubble up
    super(SubmissionClassLoader.class.getClassLoader());

    this.compiledClasses = submission.compiledFiles().stream()
        .collect(toMap(
            CompiledFile::qualifiedName, CompiledFile::classFile, (a, b) -> a, HashMap::new
        ));
    this.compiledClasses.putAll(submission.generatedAuxiliaryClasses());

    this.loadedSubmissionClasses = Collections.newSetFromMap(new ConcurrentHashMap<>());
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    if (compiledClasses.containsKey(name)) {
      byte[] bytes = compiledClasses.get(name);
      Class<?> defineClass = defineClass(name, bytes, 0, bytes.length, getProtectionDomain());
      loadedSubmissionClasses.add(defineClass);
      return defineClass;
    }
    return super.findClass(name);
  }

  private ProtectionDomain getProtectionDomain() {
    CodeSource codeSource;
    try {
      codeSource = new CodeSource(new URL("file://submission"), (Certificate[]) null);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    Permissions permissions = new Permissions();
    permissions.setReadOnly();
    return new ProtectionDomain(codeSource, permissions);
  }

  /**
   * Returns if this class loader loaded a <em>submission</em> class with a static field.
   *
   * @return true if this loader loaded a <em>submission</em> class with a static field
   */
  public boolean hasClassWithStaticField() {
    return loadedSubmissionClasses
        .stream()
        .anyMatch(this::hasStaticField);
  }

  private boolean hasStaticField(Class<?> clazz) {
    return Arrays.stream(clazz.getDeclaredFields())
        .map(Field::getModifiers)
        .anyMatch(Modifier::isStatic);
  }
}
