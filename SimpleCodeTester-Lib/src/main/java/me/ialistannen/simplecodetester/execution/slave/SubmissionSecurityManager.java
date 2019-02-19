package me.ialistannen.simplecodetester.execution.slave;

import java.security.Permission;
import me.ialistannen.simplecodetester.execution.SubmissionClassLoader;

public class SubmissionSecurityManager extends SecurityManager {

  @Override
  public void checkPermission(Permission perm) {
    // CallSite needed for lambdas
    // enum set because it does a reflective invocation to get the universe
    // let's hope that is actually safe and EnumSet can not be used to invoke arbitrary code
    if (containsClass("java.lang.invoke.CallSite", "java.util.EnumSet")) {
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

  private boolean containsClass(String first, String second) {
    for (Class<?> aClass : getClassContext()) {
      if (aClass.getName().equals(first) | aClass.getName().equals(second)) {
        return true;
      }
    }
    return false;
  }
}
