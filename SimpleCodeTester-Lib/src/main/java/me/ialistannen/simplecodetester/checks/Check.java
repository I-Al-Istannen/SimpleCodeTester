package me.ialistannen.simplecodetester.checks;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import lombok.Getter;
import lombok.ToString;
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
   * Sets the additional files this check uses.
   *
   * @param files the files the check uses
   */
  default void setFiles(Collection<CheckFile> files) {
  }

  /**
   * Whether this check needs to be manually approved by an administrator.
   *
   * @return true if the check needs to be manually approved by an administrator
   */
  default boolean needsApproval() {
    return true;
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

  @ToString
  @Getter(onMethod_ = {@JsonProperty})
  class CheckFile {

    private String name;
    private String content;

    public CheckFile(@JsonProperty("name") String name, @JsonProperty("content") String content) {
      this.name = name;
      this.content = content;
    }
  }
}
