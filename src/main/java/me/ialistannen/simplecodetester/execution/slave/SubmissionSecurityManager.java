package me.ialistannen.simplecodetester.execution.slave;

import java.nio.file.Path;
import java.security.Permission;

public class SubmissionSecurityManager extends SecurityManager {

  private Path ownPath;

  public SubmissionSecurityManager(Path ownPath) {
    this.ownPath = ownPath;
  }

  @Override
  public void checkPermission(Permission perm) {
    // allow all
  }
}
