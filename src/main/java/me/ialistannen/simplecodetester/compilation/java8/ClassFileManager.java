package me.ialistannen.simplecodetester.compilation.java8;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;

class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

  private Map<String, InMemoryOutputObject> compiledClasses;

  /**
   * Creates a new instance of ForwardingJavaFileManager.
   *
   * @param fileManager delegate to this file manager
   */
  protected ClassFileManager(StandardJavaFileManager fileManager) {
    super(fileManager);

    this.compiledClasses = new HashMap<>();
  }

  @Override
  public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind,
      FileObject sibling) throws IOException {
    InMemoryOutputObject outputObject = new InMemoryOutputObject(Path.of(className));

    compiledClasses.put(className, outputObject);

    return outputObject;
  }

  public Map<String, InMemoryOutputObject> getCompiledClasses() {
    return compiledClasses;
  }
}
