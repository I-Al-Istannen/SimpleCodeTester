package me.ialistannen.simplecodetester.execution.slave;

import java.lang.StackWalker.StackFrame;
import java.security.Permission;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import me.ialistannen.simplecodetester.execution.SubmissionClassLoader;

public class SubmissionSecurityManager extends SecurityManager {

  private final Map<String, Function<StackFrame, Boolean>> STACK_BASED_WHITELIST = new HashMap<>();

  SubmissionSecurityManager() {
    // Allow calls to getUniverse as that is used to enumerate the enum and uses reflection
    STACK_BASED_WHITELIST.put(
        "java.util.EnumSet",
        frame -> frame.getMethodName().equals("getUniverse")
    );
    // Allow calls to getKeyUniverse, as that is used to enumerate the enum and uses reflection
    STACK_BASED_WHITELIST.put(
        "java.util.EnumMap",
        frame -> frame.getMethodName().equals("getKeyUniverse")
    );
    // Allow calls to Enum.valueOf and Class.getEnumConstants (it uses setAccessible)
    STACK_BASED_WHITELIST.put(
        "java.lang.Class",
        frame -> frame.getMethodName().equals("getEnumConstants")
            || frame.getMethodName().equals("getEnumConstantsShared")
    );
    // ignore all lambda creations
    STACK_BASED_WHITELIST.put(
        "java.lang.invoke.CallSite", ignored -> true
    );
    // Character.getName accesses a system resource (uniName.dat)
    STACK_BASED_WHITELIST.put(
        "java.lang.CharacterName", ignored -> true
    );    // Character.getName accesses a system resource (uniName.dat)
    STACK_BASED_WHITELIST.put(
        "java.util.concurrent.ForkJoinPool$InnocuousForkJoinWorkerThreadFactory", ignored -> true
    );
  }

  @Override
  public void checkPermission(Permission perm) {
    if (comesFromMe()) {
      return;
    }

    // lambda init call
    if (containsWhitelistedClass()) {
      return;
    }

    if (comesFromUserCode()) {
      super.checkPermission(perm);
    }
  }

  private boolean comesFromUserCode() {
    Class<?>[] classContext = getClassContext();
    for (int i = 0; i < classContext.length; i++) {
      Class<?> aClass = classContext[i];

      if (i > 1 && aClass == SubmissionSecurityManager.class) {
        return false;
      }

      if (aClass.getClassLoader() instanceof SubmissionClassLoader) {
        return true;
      }
    }
    return false;
  }

  private boolean comesFromMe() {
    return Arrays.stream(getClassContext())
        // one frame for this method, one frame for the call to checkPermission
        .skip(2)
        // see if the security manager appears anywhere else in the context. If so, we initiated
        // the call
        .anyMatch(aClass -> aClass == getClass());
  }

  private boolean containsWhitelistedClass() {
    return StackWalker.getInstance().walk(stream ->
        stream.anyMatch(this::isWhitelisted)
    );
  }

  private boolean isWhitelisted(StackFrame frame) {
    Function<StackFrame, Boolean> function = STACK_BASED_WHITELIST
        .get(frame.getClassName());

    return function != null && function.apply(frame);
  }
}
