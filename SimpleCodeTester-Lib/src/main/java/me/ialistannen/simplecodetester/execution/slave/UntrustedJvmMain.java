package me.ialistannen.simplecodetester.execution.slave;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.checks.CheckRunner;
import me.ialistannen.simplecodetester.checks.SubmissionCheckResult;
import me.ialistannen.simplecodetester.compilation.Compiler;
import me.ialistannen.simplecodetester.compilation.java8.memory.Java8InMemoryCompiler;
import me.ialistannen.simplecodetester.execution.MessageClient;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.CompilationFailed;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.DyingMessage;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveDiedWithUnknownError;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveStarted;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveTimedOut;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SubmissionResult;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.slavebound.CompileAndCheckSubmission;
import me.ialistannen.simplecodetester.submission.CompiledSubmission;
import me.ialistannen.simplecodetester.submission.Submission;
import me.ialistannen.simplecodetester.util.ConfiguredGson;
import me.ialistannen.simplecodetester.util.Stacktrace;
import org.joor.Reflect;

/**
 * The main class of the untrusted slave vm.
 */
public class UntrustedJvmMain {

  private int port;
  private String uid;

  private MessageClient client;
  private TimerTask idleKiller;

  private UntrustedJvmMain(int port, String uid) throws IOException {
    this.port = port;
    this.uid = uid;

    this.client = createMessageClient();
  }

  private MessageClient createMessageClient() throws IOException {
    return new MessageClient(
        new Socket(InetAddress.getLocalHost(), port),
        ConfiguredGson.createGson(),
        (client, message) -> {
          if (message instanceof CompileAndCheckSubmission) {
            idleKiller.cancel();

            CompileAndCheckSubmission checkSubmissionMessage = (CompileAndCheckSubmission) message;
            Submission submission = checkSubmissionMessage.getSubmission();
            receivedSubmission(submission, checkSubmissionMessage.getChecks());
          }
        }
    );
  }

  private void execute() {
    new Thread(client).start();

    client.queueMessage(new SlaveStarted(uid));

    // Kill yourself if you get no task in a reasonable timeframe
    idleKiller = new TimerTask() {
      @Override
      public void run() {
        client.queueMessage(new SlaveTimedOut(uid));
        shutdown();
      }
    };

    new Timer(true)
        .schedule(idleKiller, TimeUnit.SECONDS.toMillis(30));
  }

  private void receivedSubmission(Submission submission, List<String> checks) {
    try {
      CompiledSubmission compiledSubmission = compile(submission);

      if (!compiledSubmission.compilationOutput().successful()) {
        shutdown();
        return;
      }

      runChecks(compiledSubmission, checks);
      shutdown();
    } catch (Throwable e) {
      e.printStackTrace();
      client.queueMessage(new SlaveDiedWithUnknownError(uid, Stacktrace.getStacktrace(e)));
    }
  }

  private CompiledSubmission compile(Submission submission) throws IOException {
    Compiler compiler = new Java8InMemoryCompiler();
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

  private void runChecks(CompiledSubmission compiledSubmission, List<String> checkNames) {
    List<Check> checks = checkNames.stream()
        .map(UntrustedJvmMain::unsafeClassGet)
        .map(aClass -> (Check) Reflect.on(aClass).create().get())
        .collect(Collectors.toList());

    System.out.println(checks);

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

    PrintStream out = new PrintStream(new FileOutputStream("/tmp/test_out.txt"));
    System.setOut(out);
    System.setErr(out);

    System.setSecurityManager(new SubmissionSecurityManager());

    new UntrustedJvmMain(port, uid).execute();
  }

  private static Class<?> unsafeClassGet(String name) {
    try {
      return Class.forName(name);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
