package me.ialistannen.simplecodetester.execution.diana;

import edu.kit.informatik.Terminal;
import java.io.IOException;
import java.io.Reader;
import me.ialistannen.simplecodetester.exceptions.ReadMoreLinesThanProvidedException;

public class TerminalBufferedReader {

  private final String[] fileContent;
  private int fileContentOffset;

  public TerminalBufferedReader(Reader reader) {
    if (reader instanceof TerminalFileReader) {
      fileContent = Terminal.readFile(((TerminalFileReader) reader).getFile().getName());
    } else {
      fileContent = null;
    }
  }

  public String readLine() throws IOException {
    if (fileContent == null) {
      return Terminal.readLine();
    }
    if (fileContentOffset >= fileContent.length) {
      throw new ReadMoreLinesThanProvidedException();
    }

    return fileContent[fileContentOffset++];
  }
}
