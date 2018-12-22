package me.ialistannen.simplecodetester.execution;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.ProtocolMessage;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.DyingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client that can receive and send {@link ProtocolMessage ProtocolMessages}.
 */
public class MessageClient implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageClient.class);

  private final Socket socket;
  private Consumer<ProtocolMessage> messageHandler;
  private Gson gson;
  private ConcurrentLinkedQueue<ProtocolMessage> outgoing;
  private volatile boolean stop;

  public MessageClient(Socket socket, Gson gson, Consumer<ProtocolMessage> messageHandler) {
    this.socket = socket;
    this.messageHandler = messageHandler;

    this.gson = gson;
    this.outgoing = new ConcurrentLinkedQueue<>();
  }

  /**
   * Queues a message for later sending.
   *
   * @param message the message to send
   */
  public void queueMessage(ProtocolMessage message) {
    outgoing.add(message);
  }

  /**
   * Stops this client.
   */
  public void stop() {
    stop = true;
  }

  @Override
  public void run() {
    try (socket;
        InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        OutputStream outputStream = socket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {

      socket.setSoTimeout(1000);

      while (!Thread.interrupted() && !stop) {
        try {
          readInput(dataInputStream);
        } catch (SocketTimeoutException ignored) {
        }
        writeQueue(dataOutputStream);
      }

      // flush output
      writeQueue(dataOutputStream);

    } catch (EOFException e) {
      LOGGER.info("Client disconnected roughly by EOF.");
    } catch (Exception e) {
      LOGGER.warn("Error in socket client run.", e);
    }

    LOGGER.info("Closed thread!");
  }

  private void writeQueue(DataOutputStream dataOutputStream) throws IOException {
    while (!outgoing.isEmpty()) {
      dataOutputStream.writeUTF(gson.toJson(outgoing.poll()));
    }
  }

  private void readInput(DataInputStream dataInputStream)
      throws IOException, ClassNotFoundException {
    String messageJson = dataInputStream.readUTF();

    JsonObject jsonObject = gson.fromJson(messageJson, JsonObject.class);
    JsonPrimitive className = jsonObject.getAsJsonPrimitive("className");

    Class<?> messageClass = Class.forName(className.getAsString());

    if (!ProtocolMessage.class.isAssignableFrom(messageClass)) {
      throw new IllegalArgumentException("Message class is no message: " + messageClass);
    }

    ProtocolMessage message = gson.fromJson(messageJson, (Type) messageClass);

    if (message instanceof DyingMessage) {
      Thread.currentThread().interrupt();
    }

    messageHandler.accept(message);
  }
}
