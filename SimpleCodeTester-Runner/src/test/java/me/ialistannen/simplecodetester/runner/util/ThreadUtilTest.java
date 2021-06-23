package me.ialistannen.simplecodetester.runner.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ThreadFactory;
import org.junit.jupiter.api.Test;

class ThreadUtilTest {

  @Test
  void daemonCreatesIsDaemons() {
    ThreadFactory threadFactory = ThreadUtil.daemonThreadFactory(Thread::new);

    Thread thread = threadFactory.newThread(() -> {
    });

    assertTrue(thread.isDaemon(), "Thread was no daemon");
  }
}
