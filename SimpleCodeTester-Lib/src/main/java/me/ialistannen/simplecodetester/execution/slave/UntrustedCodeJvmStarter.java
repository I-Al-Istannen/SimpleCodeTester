package me.ialistannen.simplecodetester.execution.slave;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Starts the slave vms running untrusted code.
 */
public class UntrustedCodeJvmStarter {

  /**
   * Runs a given submission in anew VM.
   *
   * @param port the port the master listens on
   * @param uid the uid of the client
   * @param classPath the class path to use. Should at least contain the class file of your own
   * @return the created {@link Process}
   */
  public Process startSlave(int port, String uid, String... classPath) {
    try {
      List<String> command = buildArguments(port, uid, classPath);
      return new ProcessBuilder(command).start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private List<String> buildArguments(int port, String uid, String... classpath) {
    return Arrays.asList(
        "nice",
        "-n",
        "10",
        ProcessHandle.current().info().command().orElseThrow(),
        // Limit heap size
        "-Xmx20m",
        // classpath only added when it is specified
        classpath.length == 0 ? "" : "-cp",
        String.join(":", classpath),
        // policy
        "-Djava.security.policy=="
            + getClass().getResource("/SlavePolicy.policy").toExternalForm(),
        // main class
        UntrustedJvmMain.class.getName(),
        Integer.toString(port),
        uid
    );
  }
}
