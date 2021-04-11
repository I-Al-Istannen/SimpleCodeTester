package me.ialistannen.simplecodetester.runner.execution;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;
import me.ialistannen.simplecodetester.runner.util.DaemonThreadFactory;
import me.ialistannen.simplecodetester.runner.util.ProgramResult;
import me.ialistannen.simplecodetester.util.StringOutputStream;

/**
 * A small utility for executing programs.
 */
public class ProgramExecutor {

  // If multiple ProgramExecutors are used, could block the common fork join pool completely.
  // We occupy 2 threads per execution (draining stderr and stdio)
  // Using a cached thread pool allows the executor to scale, while also trimming down unused
  // tasks after some delay specified in the JDK method (currently 60 seconds).
  // As the threads are daemons, we do not need to shut down the pool at all. If nobody waits for
  // the result, the threads will die as well, otherwise they will be kept alive and things work
  // as expected.
  private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(
      new DaemonThreadFactory()
  );

  /**
   * Executes a program using the passed command. Cancelling the future <em>does not</em> affect the
   * running task.
   *
   * @param command the command to execute
   * @param stdin the stdin to pass to the program
   * @return a future representing the result
   */
  public StreamsProcessOutput<ProgramResult> execute(List<String> command, String stdin) {
    StringOutputStream stdOutStream = new StringOutputStream();
    StringOutputStream stdErrStream = new StringOutputStream();

    FutureTask<ProgramResult> futureTask = new FutureTask<>(() -> {
      Instant startTime = Instant.now();

      Process process = new ProcessBuilder(command).start();

      if (!stdin.isEmpty()) {
        writeStdIn(stdin, process::getOutputStream);
      }

      CompletableFuture<String> stdOut = readOutput(stdOutStream, process::getInputStream);
      CompletableFuture<String> stdErr = readOutput(stdErrStream, process::getErrorStream);

      try {
        int exitCode = process.waitFor();

        return new ProgramResult(
            exitCode,
            stdOut.get(),
            stdErr.get(),
            Duration.between(startTime, Instant.now())
        );
      } catch (InterruptedException e) {
        throw new CancellationException("Interrupted :(");
      }
    });

    return new WaitingFutureTask<>(futureTask, stdOutStream, stdErrStream);
  }

  /**
   * Executes a program using the passed command. Cancelling the future <em>does not</em> affect the
   * running task.
   *
   * @param command the command to execute
   * @return a future representing the result
   */
  public StreamsProcessOutput<ProgramResult> execute(List<String> command) {
    return execute(command, "");
  }

  private CompletableFuture<String> readOutput(StringOutputStream output,
      UncheckedSupplier<InputStream> input) {
    return CompletableFuture
        .supplyAsync(asUnchecked(() -> {
              try (InputStream inputStream = input.get()) {
                inputStream.transferTo(output);
              }
              return output.getString();
            }),
            EXECUTOR
        )
        .exceptionally(throwable -> output.getString());
  }

  private CompletableFuture<Void> writeStdIn(String stdin,
      UncheckedSupplier<OutputStream> sink) {
    return CompletableFuture
        .supplyAsync(asUnchecked(() -> {
              try (OutputStream outputStream = sink.get()) {
                new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8))
                    .transferTo(outputStream);
              }
              return (Void) null;
            }),
            EXECUTOR
        )
        .exceptionally(throwable -> null);
  }

  private <T> Supplier<T> asUnchecked(UncheckedSupplier<T> supplier) {
    return () -> {
      try {
        return supplier.get();
      } catch (Exception exception) {
        throw new RuntimeException(exception);
      }
    };
  }

  private interface UncheckedSupplier<T> {

    T get() throws Exception;
  }

}

