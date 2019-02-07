package me.ialistannen.simplecodetester.execution.slave;

import java.security.Permission;
import me.ialistannen.simplecodetester.execution.SubmissionClassLoader;

public class SubmissionSecurityManager extends SecurityManager {

  @Override
  public void checkPermission(Permission perm) {
    if (containsClass("java.lang.invoke.CallSite")) {
      return;
    }

    Class<?>[] classContext = getClassContext();
    for (int i = 0; i < classContext.length; i++) {
      Class<?> aClass = classContext[i];

      if (i > 1 && aClass == SubmissionSecurityManager.class) {
        return;
      }

      if (aClass.getClassLoader() instanceof SubmissionClassLoader) {
        super.checkPermission(perm);
        return;
      }
    }
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
