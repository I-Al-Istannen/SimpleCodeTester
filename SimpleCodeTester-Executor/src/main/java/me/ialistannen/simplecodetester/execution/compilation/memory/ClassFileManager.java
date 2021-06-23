package me.ialistannen.simplecodetester.execution.compilation.memory;

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

  private final Map<String, InMemoryOutputObject> outputFileMap;

  /**
   * Creates a new instance of ForwardingJavaFileManager.
   *
   * @param fileManager delegate to this file manager
   */
  ClassFileManager(StandardJavaFileManager fileManager) {
    super(fileManager);

    this.outputFileMap = new HashMap<>();
  }

  @Override
  public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind,
      FileObject sibling) {
    InMemoryOutputObject outputObject = new InMemoryOutputObject(className);

    outputFileMap.put(className, outputObject);

    return outputObject;
  }

  InMemoryOutputObject getForClassPath(String path) {
    return outputFileMap.get(sanitizeToClassName(path));
  }

  String sanitizeToClassName(String path) {
    return path
        .replace("/", ".")
        .replace(".java", "");
  }

  /**
   * Returns all written output objects.
   *
   * @return all written output objects
   */
  Map<String, InMemoryOutputObject> getAll() {
    return outputFileMap;
  }
}
