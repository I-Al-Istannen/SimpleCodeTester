package me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound;

import me.ialistannen.simplecodetester.compilation.CompilationOutput;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.ProtocolMessage;

/**
 * A message indicating compilation has failed.
 */
public class CompilationFailed extends ProtocolMessage {

  private CompilationOutput output;

  public CompilationFailed(CompilationOutput output, String uid) {
    super(uid);
    this.output = output;
  }

  /**
   * Returns the {@link CompilationOutput}.
   *
   * @return the compilation output
   */
  public CompilationOutput getOutput() {
    return output;
  }

  @Override
  public String toString() {
    return "CompilationFailed{" +
        "uid=" + getUid() +
        ", output=" + output +
        '}';
  }
}
