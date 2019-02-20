package me.ialistannen.simplecodetester.execution.slave;

import java.security.Permission;
import me.ialistannen.simplecodetester.execution.SubmissionClassLoader;

public class SubmissionSecurityManager extends SecurityManager {

  // CallSite needed for lambdas
  // enum set/map because they do a reflective invocation to get the universe
  // let's hope that is actually safe and EnumSet/Map can not be used to invoke arbitrary code
  private static final String[] WHITELISTED_CLASSES = {
      "java.lang.invoke.CallSite", "java.util.EnumSet", "java.util.EnumMap"
  };

  @Override
  public void checkPermission(Permission perm) {
    if (containsWhitelistedClass()) {
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

  private boolean containsWhitelistedClass() {
    for (Class<?> aClass : getClassContext()) {
      for (String s : WHITELISTED_CLASSES) {
        if (s.equals(aClass.getName())) {
          return true;
        }
      }
    }
    return false;
  }
}
