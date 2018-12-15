package me.ialistannen.simplecodetester.execution.master;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import me.ialistannen.simplecodetester.execution.MessageClient;
import me.ialistannen.simplecodetester.execution.slave.UntrustedCodeJvmStarter;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.ProtocolMessage;
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
  private Consumer<ProtocolMessage> messageHandler;
  private int port;
  private UntrustedCodeJvmStarter untrustedCodeJvmStarter;

  public SlaveManager(Consumer<ProtocolMessage> messageHandler) {
    this.messageHandler = messageHandler;

    this.gson = ConfiguredGson.createGson();
    this.executorService = Executors
        .newCachedThreadPool(ThreadHelper.deamonThreadFactory(Thread::new));
    this.untrustedCodeJvmStarter = new UntrustedCodeJvmStarter();
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
   * @param uid the UID of the submission
   */
  public void runSubmission(Submission submission, String uid) {
    untrustedCodeJvmStarter.runSubmission(
        port, submission, uid
    );
  }

  private void runServer() {
    try (ServerSocket serverSocket = new ServerSocket()) {
      serverSocket.bind(null);

      port = serverSocket.getLocalPort();
      serverSocket.setSoTimeout(2000);

      while (!Thread.interrupted()) {
        tryAcceptConnection(serverSocket);
      }
    } catch (IOException e) {
      LOGGER.warn("Error running master server.", e);
    }
  }

  private void tryAcceptConnection(ServerSocket serverSocket) throws IOException {
    try {
      Socket acceptedSocket = serverSocket.accept();
      MessageClient client = new MessageClient(acceptedSocket, gson, messageHandler);

      executorService.submit(client);
    } catch (SocketTimeoutException ignored) {
    }
  }
}
