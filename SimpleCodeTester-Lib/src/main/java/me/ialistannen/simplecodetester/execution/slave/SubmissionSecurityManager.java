package me.ialistannen.simplecodetester.execution.slave;

import me.ialistannen.simplecodetester.execution.SubmissionClassLoader;

import java.security.Permission;

public class SubmissionSecurityManager extends SecurityManager {

  @Override
  public void checkPermission(Permission perm) {
    // CallSite needed for lambdas
    // enum set because it does a reflective invocation to get the universe
    // let's hope that is actually safe and EnumSet can not be used to invoke arbitrary code
    // let's hope the same for EnumMap :P
    if (containsClass("java.lang.invoke.CallSite", "java.util.EnumSet", "java.util.EnumMap")) {
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

  private boolean containsClass(String... check) {
    for (Class<?> aClass : getClassContext()) {
      for (String name : check) {
        if (aClass.getName().equals(name)) {
          return true;
        }
      }
    }
    return false;
  }
}
