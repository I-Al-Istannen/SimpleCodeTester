package me.ialistannen.simplecodetester.execution.slave;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.checks.CheckRunner;
import me.ialistannen.simplecodetester.checks.SubmissionCheckResult;
import me.ialistannen.simplecodetester.compilation.Compiler;
import me.ialistannen.simplecodetester.compilation.java8.Java8FileCompiler;
import me.ialistannen.simplecodetester.execution.MessageClient;
import me.ialistannen.simplecodetester.execution.SubmissionClassLoader;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.CompilationFailed;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.DyingMessage;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveDiedWithUnknownError;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveStarted;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SubmissionResult;
import me.ialistannen.simplecodetester.submission.CompiledSubmission;
import me.ialistannen.simplecodetester.submission.ImmutableSubmission;
import me.ialistannen.simplecodetester.submission.Submission;
import me.ialistannen.simplecodetester.util.ConfiguredGson;
import org.joor.Reflect;

/**
 * The main class of the untrusted slave vm.
 */
public class UntrustedJvmMain {

  private int port;
  private String uid;
  private List<Check> checks;

  private MessageClient client;
  private Submission submission;

  private UntrustedJvmMain(int port, String uid, List<Check> checks, Submission submission)
      throws IOException {
    this.port = port;
    this.uid = uid;
    this.checks = checks;
    this.submission = submission;

    this.client = createMessageClient();
  }

  private MessageClient createMessageClient() throws IOException {
    return new MessageClient(
        new Socket(InetAddress.getLocalHost(), port),
        ConfiguredGson.createGson(),
        protocolMessage -> {
          // nothing relevant currently
        }
    );
  }

  private void execute() throws IOException {
    new Thread(client).start();

    client.queueMessage(new SlaveStarted(uid));

    try {
      CompiledSubmission compiledSubmission = compile();

      if (!compiledSubmission.compilationOutput().successful()) {
        shutdown();
        return;
      }

      runChecks(compiledSubmission);
      shutdown();
    } catch (Throwable e) {
      e.printStackTrace();
      client.queueMessage(new SlaveDiedWithUnknownError(uid, e.getMessage()));
      throw e;
    }
  }

  private CompiledSubmission compile() throws IOException {
    Compiler compiler = new Java8FileCompiler();
    CompiledSubmission compiledSubmission = compiler.compileSubmission(submission);

    if (!compiledSubmission.compilationOutput().successful()) {
      client.queueMessage(new CompilationFailed(compiledSubmission.compilationOutput(), uid));
    }

    return compiledSubmission;
  }

  private void shutdown() {
    client.queueMessage(new DyingMessage(uid));
    client.stop();
  }

  private void runChecks(CompiledSubmission compiledSubmission) {
    CheckRunner checkRunner = new CheckRunner(checks);
    SubmissionCheckResult checkResult = checkRunner.checkSubmission(compiledSubmission);

    client.queueMessage(new SubmissionResult(checkResult, uid));
  }

  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      throw new IllegalArgumentException(
          "Usage: java <program> <master port> <slave uid> <path to submission> <check classes...>"
      );
    }
    int port = Integer.parseInt(args[0]);
    String uid = args[1];
    Path path = Paths.get(args[2]);

    if (!Files.isDirectory(path)) {
      throw new IllegalArgumentException(String.format("File '%s' is not a directory.", path));
    }

    System.setSecurityManager(new SubmissionSecurityManager(path));

    Submission submission = ImmutableSubmission.builder()
        .classLoader(new SubmissionClassLoader(path))
        .basePath(path)
        .build();

    new UntrustedJvmMain(port, uid, parseChecks(args), submission).execute();
  }

  private static List<Check> parseChecks(String[] args) {
    return Arrays.stream(args)
        .skip(3)
        .map(UntrustedJvmMain::unsafeClassGet)
        .map(aClass -> (Check) Reflect.on(aClass).create().get())
        .collect(Collectors.toList());
  }

  private static Class<?> unsafeClassGet(String name) {
    try {
      return Class.forName(name);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
