package me.ialistannen.simplecodetester.execution.diana;

import edu.kit.informatik.Terminal;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.stream.Stream;

public class TerminalBufferedReader implements AutoCloseable {

  private final String[] fileContent;
  private int fileContentOffset;

  public TerminalBufferedReader(Reader reader) {
    if (reader instanceof TerminalFileReader) {
      fileContent = Terminal.readFile(((TerminalFileReader) reader).getFile().toString());
    } else {
      fileContent = null;
    }
  }

  public String readLine() throws IOException {
    if (fileContent == null) {
      return Terminal.readLine();
    }
    if (fileContentOffset >= fileContent.length) {
      return null;
    }

    return fileContent[fileContentOffset++];
  }

  public Stream<String> lines() {
    return Arrays.stream(fileContent);
  }

  @Override
  public void close() {
  }
}
