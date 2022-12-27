package me.ialistannen.simplecodetester.runner.communication;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;
import java.util.UUID;
import me.ialistannen.simplecodetester.result.Result;
import me.ialistannen.simplecodetester.submission.CompleteTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendCommunicator {

  private static final Logger LOGGER = LoggerFactory.getLogger(BackendCommunicator.class);

  private final String password;
  private final HttpClient client;
  private final Gson gson;
  private final URI reportWorkUri;
  private final URI requestWorkUri;

  public BackendCommunicator(String backendUrl, String password, Gson gson) {
    this.password = password;
    this.gson = gson;

    this.client = HttpClient.newHttpClient();
    this.requestWorkUri = URI.create(backendUrl + "/request-work");
    this.reportWorkUri = URI.create(backendUrl + "/report-work");
  }

  /**
   * Requests a task from the backend.
   *
   * @return the task, if the backend had any to pass out
   */
  public Optional<CompleteTask> requestTask() throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(requestWorkUri)
        .header("Authorization", password)
        .build();

    HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

    if (response.statusCode() == 200) {
      return Optional.of(gson.fromJson(response.body(), CompleteTask.class));
    } else if (response.statusCode() == 404) {
      return Optional.empty();
    }

    LOGGER.warn("Got unexpected status code in request: {}", response.statusCode());
    return Optional.empty();
  }

  public void sendResults(Result result, UUID userId) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder(reportWorkUri)
        .POST(BodyPublishers.ofString(gson.toJson(new AttributedResult(userId.toString(), result))))
        .header("Authorization", password)
        .header("Content-Type", "application/json")
        .build();

    HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      LOGGER.warn(
          "Sending results failed with code {} and body {}",
          response.statusCode(),
          response.body()
      );
    }
  }

  private static class AttributedResult {

    private final String userId;
    private final Result result;

    private AttributedResult(String userId, Result result) {
      this.userId = userId;
      this.result = result;
    }

    public String getUserId() {
      return userId;
    }

    public Result getResult() {
      return result;
    }
  }
}
