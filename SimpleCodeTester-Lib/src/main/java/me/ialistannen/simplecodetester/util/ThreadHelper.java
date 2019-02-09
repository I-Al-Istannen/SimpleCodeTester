package me.ialistannen.simplecodetester.util;

import java.util.concurrent.ThreadFactory;

/**
 * Helps with creating and handling threads.
 */
public final class ThreadHelper {


  /**
   * Wraps a {@link ThreadFactory} to only create {@link Thread#isDaemon() daemon} threads.
   *
   * @param original the original thread factory
   * @return a thread factory only creating daemon threads
   */
  public static ThreadFactory daemonThreadFactory(ThreadFactory original) {
    return r -> {
      Thread thread = original.newThread(r);
      thread.setDaemon(true);
      return thread;
    };
  }
}
