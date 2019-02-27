package me.ialistannen.simplecodetester.execution.slave;

import static java.util.stream.Collectors.toCollection;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.checks.CheckRunner;
import me.ialistannen.simplecodetester.checks.storage.CheckSerializer;
import me.ialistannen.simplecodetester.compilation.Compiler;
import me.ialistannen.simplecodetester.compilation.java8.memory.Java8InMemoryCompiler;
import me.ialistannen.simplecodetester.exceptions.CompilationException;
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
import me.ialistannen.simplecodetester.util.ExceptionUtil;

/**
 * The main class of the untrusted slave vm.
 */
public class UntrustedJvmMain {

  private int port;
  private String uid;

  private MessageClient client;
  private TimerTask idleKiller;
  private CheckSerializer checkSerializer;
  private Gson gson;

  private UntrustedJvmMain(int port, String uid) throws IOException {
    this.port = port;
    this.uid = uid;

    this.gson = ConfiguredGson.createGson();
    this.client = createMessageClient();
    this.checkSerializer = new CheckSerializer(gson);
  }

  private MessageClient createMessageClient() throws IOException {
    return new MessageClient(
        new Socket(InetAddress.getLocalHost(), port),
        gson,
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

    client.queueMessage(new SlaveStarted(uid, ProcessHandle.current().pid()));

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
    } catch (CompilationException e) {
      e.printStackTrace();
      client.queueMessage(new CompilationFailed(e.getOutput(), uid));
    } catch (Throwable e) {
      e.printStackTrace();
      client.queueMessage(new SlaveDiedWithUnknownError(uid, ExceptionUtil.getStacktrace(e)));
    } finally {
      shutdown();
    }
  }

  private CompiledSubmission compile(Submission submission) {
    Compiler compiler = new Java8InMemoryCompiler();
    CompiledSubmission compiledSubmission = compiler.compileSubmission(submission);

    if (!compiledSubmission.compilationOutput().successful()) {
      throw new CompilationException(compiledSubmission.compilationOutput());
    }
    // compiler emitted something as normal output
    if (!compiledSubmission.compilationOutput().output().isEmpty()) {
      throw new CompilationException(compiledSubmission.compilationOutput().output());
    }
    // a compiler warning
    if (!compiledSubmission.compilationOutput().diagnostics().isEmpty()) {
      throw new CompilationException(compiledSubmission.compilationOutput());
    }

    return compiledSubmission;
  }

  private void shutdown() {
    client.queueMessage(new DyingMessage(uid));
    client.stop();
  }

  private void runChecks(CompiledSubmission compiledSubmission, List<String> receivedChecks) {

    List<Check> checks = receivedChecks.stream()
        .map(checkSerializer::fromJson)
        .collect(toCollection(ArrayList::new));

    CheckRunner checkRunner = new CheckRunner(checks);
    checkRunner.checkSubmission(
        compiledSubmission,
        (fileName, result) -> client.queueMessage(new SubmissionResult(fileName, result, uid))
    );
  }

  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      throw new IllegalArgumentException(
          "Usage: java <program> <master port> <slave uid>"
      );
    }
    int port = Integer.parseInt(args[0]);
    String uid = args[1];

    System.setSecurityManager(new SubmissionSecurityManager());

    new UntrustedJvmMain(port, uid).execute();
  }
}
