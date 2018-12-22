package me.ialistannen.simplecodetester.execution.slave;

import java.io.FilePermission;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Permission;
import me.ialistannen.simplecodetester.execution.SubmissionClassLoader;
import me.ialistannen.simplecodetester.submission.Submission;

public class SubmissionSecurityManager extends SecurityManager {

  private Path submissionPath;

  /**
   * Creates a new {@link SecurityManager} for a given submission path.
   *
   * @param submissionPath the base path of the {@link Submission}
   */
  public SubmissionSecurityManager(Path submissionPath) {
    this.submissionPath = submissionPath;
  }

  @Override
  public void checkPermission(Permission perm) {
    if (isMe()) {
      return;
    }

    if (containsClass("java.lang.invoke.CallSite")) {
      return;
    }

    for (Class<?> aClass : getClassContext()) {
      if (aClass.getClassLoader() instanceof SubmissionClassLoader) {
        processSubmissionPermissionRequest(perm);
        return;
      }
    }
  }

  private boolean isMe() {
    for (int i = 2; i < getClassContext().length; i++) {
      if (getClassContext()[i] == SubmissionSecurityManager.class) {
        return true;
      }
    }
    return false;
  }

  private void processSubmissionPermissionRequest(Permission perm) {
    if (perm instanceof FilePermission) {
      String fileName = perm.getName();
      Path path = Paths.get(fileName).toAbsolutePath();
      if (pathAllowed(path)) {
        return;
      }
    }

    super.checkPermission(perm);
  }

  private boolean containsClass(String name) {
    for (Class<?> aClass : getClassContext()) {
      if (aClass.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  private boolean pathAllowed(Path path) {
    return path.startsWith(submissionPath);
  }
}
