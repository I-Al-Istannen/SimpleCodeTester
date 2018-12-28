package me.ialistannen.simplecodetester.execution.master;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.execution.MessageClient;
import me.ialistannen.simplecodetester.execution.slave.UntrustedCodeJvmStarter;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.ProtocolMessage;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveStarted;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.slavebound.CompileAndCheckSubmission;
import me.ialistannen.simplecodetester.submission.Submission;
import me.ialistannen.simplecodetester.util.ConfiguredGson;
import me.ialistannen.simplecodetester.util.ThreadHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that manages different slave vms, starting them and reporting their results.
 */
public class SlaveManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(SlaveManager.class);

  private ExecutorService executorService;

  private Gson gson;
  private Thread thread;
  private BiConsumer<MessageClient, ProtocolMessage> messageHandler;
  private int port;
  private UntrustedCodeJvmStarter untrustedCodeJvmStarter;
  private String[] classpath;

  private Map<String, SubmissionCheckEntry> pendingSubmissions;
  private volatile boolean started;

  public SlaveManager(BiConsumer<MessageClient, ProtocolMessage> messageHandler,
      String[] classpath) {
    this.messageHandler = messageHandler;
    this.classpath = classpath;

    this.gson = ConfiguredGson.createGson();
    this.executorService = Executors
        .newCachedThreadPool(ThreadHelper.deamonThreadFactory(Thread::new));
    this.untrustedCodeJvmStarter = new UntrustedCodeJvmStarter();
    this.pendingSubmissions = new ConcurrentHashMap<>();
  }

  /**
   * Starts the manager. <em>Can not be called twice!</em>
   */
  public void start() {
    if (thread != null) {
      throw new IllegalStateException("Already started");
    }
    thread = new Thread(this::runServer);
    thread.start();

    while (!started) {
      Thread.onSpinWait();
    }
  }

  /**
   * Stops the server.
   */
  public void stop() {
    executorService.shutdown();
    if (thread != null) {
      thread.interrupt();
    }
  }

  /**
   * Runs a {@link Submission} on a new slave.
   *
   * @param submission the submission to run
   * @param checks the source code of all {@link Check}s to run
   * @param uid the UID of the submission
   */
  public void runSubmission(Submission submission, List<String> checks, String uid) {
    pendingSubmissions.put(uid, new SubmissionCheckEntry(submission, checks));
    untrustedCodeJvmStarter.startSlave(port, uid, classpath);
  }

  private void runServer() {
    try (ServerSocket serverSocket = new ServerSocket()) {
      serverSocket.bind(null);

      started = true;

      port = serverSocket.getLocalPort();
      serverSocket.setSoTimeout(2000);

      while (!Thread.interrupted()) {
        tryAcceptConnection(serverSocket);
      }
    } catch (IOException e) {
      LOGGER.warn("Error running master server.", e);
    } finally {
      started = true;
    }
  }

  private void tryAcceptConnection(ServerSocket serverSocket) throws IOException {
    try {
      Socket acceptedSocket = serverSocket.accept();
      MessageClient client = new MessageClient(acceptedSocket, gson, wrappedMessageHandler());

      executorService.submit(client);
    } catch (SocketTimeoutException ignored) {
    }
  }

  private BiConsumer<MessageClient, ProtocolMessage> wrappedMessageHandler() {
    return (client, message) -> {
      if (message instanceof SlaveStarted) {
        String uid = message.getUid();
        SubmissionCheckEntry checkEntry = pendingSubmissions.get(uid);
        client.queueMessage(
            new CompileAndCheckSubmission(
                uid, checkEntry.getSubmission(), checkEntry.getCheckSource()
            )
        );
      }

      messageHandler.accept(client, message);
    };
  }
}
