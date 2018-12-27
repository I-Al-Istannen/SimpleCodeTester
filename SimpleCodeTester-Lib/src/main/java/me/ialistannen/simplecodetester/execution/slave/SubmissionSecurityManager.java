package me.ialistannen.simplecodetester.execution.slave;

import java.security.Permission;
import me.ialistannen.simplecodetester.execution.SubmissionClassLoader;

public class SubmissionSecurityManager extends SecurityManager {

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
        super.checkPermission(perm);
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

  private boolean containsClass(String name) {
    for (Class<?> aClass : getClassContext()) {
      if (aClass.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }
}
