package me.ialistannen.simplecodetester.util;

import java.util.concurrent.ThreadFactory;

public class ThreadHelper {


  public static ThreadFactory deamonThreadFactory(ThreadFactory original) {
    return r -> {
      Thread thread = original.newThread(r);
      thread.setDaemon(true);
      return thread;
    };
  }
}
