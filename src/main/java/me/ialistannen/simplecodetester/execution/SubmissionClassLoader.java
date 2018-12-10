package me.ialistannen.simplecodetester.execution;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SubmissionClassLoader extends URLClassLoader {

  private List<Pattern> redefinedClassWhitelist;

  private Map<String, Class<?>> myLoadedClasses;

  public SubmissionClassLoader(Path folder, Pattern... redefinedClassWhitelist)
      throws MalformedURLException {
    super(
        new URL[]{folder.toAbsolutePath().toUri().toURL()},
        SubmissionClassLoader.class.getClassLoader() // allow class requests to bubble up
    );

    this.myLoadedClasses = new HashMap<>();
    this.redefinedClassWhitelist = Arrays.asList(redefinedClassWhitelist);
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    return super.loadClass(name, resolve);
  }

  @Override
  protected Class<?> findClass(String moduleName, String name) {
    return super.findClass(moduleName, name);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    return super.findClass(name);
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    if (myLoadedClasses.containsKey(name)) {
      return myLoadedClasses.get(name);
    }

    if (redefinedClassWhitelist.stream().anyMatch(pattern -> pattern.matcher(name).matches())) {
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
      inputStream.transferTo(outputStream);

      return outputStream.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
