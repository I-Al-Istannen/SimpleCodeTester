package me.ialistannen.simplecodetester.execution.diana;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

public class TerminalFileReader extends Reader {

  private final File file;

  public TerminalFileReader(File file) {
    this.file = file;
  }

  public TerminalFileReader(String file) {
    this(new File(file));
  }

  public File getFile() {
    return file;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    throw new IllegalStateException("You shouldn't be calling this :( Rewriting failed!");
  }

  @Override
  public void close() throws IOException {
  }
}
