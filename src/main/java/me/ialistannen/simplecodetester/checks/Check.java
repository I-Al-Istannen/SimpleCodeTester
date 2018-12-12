package me.ialistannen.simplecodetester.checks;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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
   * Visits the given class file using the passed {@link ClassVisitor}.
   *
   * @param file the {@link CompiledFile} to visit
   * @param visitor the visitor to use
   */
  default void visitClassfile(CompiledFile file, ClassVisitor visitor) {
    try (InputStream inputStream = Files.newInputStream(file.classFile())) {
      ClassReader classReader = new ClassReader(inputStream);
      classReader.accept(visitor, ClassReader.SKIP_DEBUG);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
