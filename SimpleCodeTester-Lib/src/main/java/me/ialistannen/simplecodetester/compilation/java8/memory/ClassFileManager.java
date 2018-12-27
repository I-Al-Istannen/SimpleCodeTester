package me.ialistannen.simplecodetester.compilation.java8.memory;

import java.util.HashMap;
import java.util.Map;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;

/**
 * A manager for compiled class files.
 */
class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

  private Map<String, InMemoryOutputObject> compiledClasses;

  /**
   * Creates a new instance of ForwardingJavaFileManager.
   *
   * @param fileManager delegate to this file manager
   */
  ClassFileManager(StandardJavaFileManager fileManager) {
    super(fileManager);

    this.compiledClasses = new HashMap<>();
  }

  @Override
  public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind,
      FileObject sibling) {
    InMemoryOutputObject outputObject = new InMemoryOutputObject(className);

    compiledClasses.put(className, outputObject);

    return outputObject;
  }

  InMemoryOutputObject getForClassPath(String path) {
    return compiledClasses.get(
        path.replace("/", ".")
            .replace(".java", "")
    );
  }
}
