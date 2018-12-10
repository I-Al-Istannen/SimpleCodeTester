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

  private final Path folder;
  private Map<String, PathOutputObject> compiledClasses;

  /**
   * Creates a new instance of ForwardingJavaFileManager.
   *
   * @param fileManager delegate to this file manager
   * @param folder the folder to store classes in
   */
  protected ClassFileManager(StandardJavaFileManager fileManager, Path folder) {
    super(fileManager);
    this.folder = folder;

    this.compiledClasses = new HashMap<>();
  }

  @Override
  public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind,
      FileObject sibling) throws IOException {
    PathOutputObject outputObject = new PathOutputObject(
        folder.resolve(className.replace(".", "/") + ".class")
    );

    compiledClasses.put(className, outputObject);

    return outputObject;
  }

  public Map<String, PathOutputObject> getCompiledClasses() {
    return compiledClasses;
  }
}
