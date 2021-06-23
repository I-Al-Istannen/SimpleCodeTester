package me.ialistannen.simplecodetester.execution.security;

import java.security.AllPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import me.ialistannen.simplecodetester.execution.running.SubmissionClassLoader;

public class ExecutorSandboxPolicy extends Policy {

  private static final PermissionCollection ELEVATED_PERMISSIONS = getElevatedPermissions();

  @Override
  public PermissionCollection getPermissions(ProtectionDomain domain) {
    if (domain.getClassLoader() instanceof SubmissionClassLoader) {
      return domain.getPermissions();
    }
    return ELEVATED_PERMISSIONS;
  }

  @Override
  public boolean implies(ProtectionDomain domain, Permission permission) {
    return getPermissions(domain).implies(permission);
  }

  private static PermissionCollection getElevatedPermissions() {
    Permissions permissions = new Permissions();
    permissions.add(new AllPermission());
    permissions.setReadOnly();
    return permissions;
  }

}
