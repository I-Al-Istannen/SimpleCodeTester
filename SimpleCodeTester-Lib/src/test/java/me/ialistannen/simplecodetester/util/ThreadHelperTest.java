package me.ialistannen.simplecodetester.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ThreadFactory;
import org.junit.jupiter.api.Test;

class ThreadHelperTest {

  @Test
  void daemonCreatesIsDaemons() {
    ThreadFactory threadFactory = ThreadHelper.daemonThreadFactory(Thread::new);

    Thread thread = threadFactory.newThread(() -> {
    });

    assertTrue(thread.isDaemon(), "Thread was no daemon");
  }
}