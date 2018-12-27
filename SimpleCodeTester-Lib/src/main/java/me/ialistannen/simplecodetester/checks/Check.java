package me.ialistannen.simplecodetester.checks;

import me.ialistannen.simplecodetester.exceptions.CheckFailedException;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

/**
 * A single check that can validate some class properties. It accepts a single {@link CompiledFile}
 * and can use its source or bytecode to make a decision.
 */
@FunctionalInterface
public interface Check {

  /**
   * Checks a single {@link CompiledFile}.
   *
   * @param file the file to check
   * @return the result of checking the file
   * @throws CheckFailedException if the check failed and it makes more sense for the check to not
   * directly return a result
   */
  CheckResult check(CompiledFile file);

  /**
   * Returns the name of this check.
   *
   * @return the check's name
   * @implNote The default implementation just uses the class's simple name.
   */
  default String name() {
    return getClass().getSimpleName();
  }

  /**
   * Visits the given class file using the passed {@link ClassVisitor}.
   *
   * @param file the {@link CompiledFile} to visit
   * @param visitor the visitor to use
   */
  default void visitClassfile(CompiledFile file, ClassVisitor visitor) {
    ClassReader classReader = new ClassReader(file.classFile());
    classReader.accept(visitor, ClassReader.SKIP_DEBUG);
  }
}
