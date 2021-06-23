package me.ialistannen.simplecodetester.execution.diana;

import edu.kit.informatik.Terminal;
import java.io.File;
import java.io.InputStream;
import me.ialistannen.simplecodetester.exceptions.ReadMoreLinesThanProvidedException;

public class TerminalScanner {

  private final String[] fileContent;
  private int fileContentOffset;

  public TerminalScanner(File file) {
    fileContent = Terminal.readFile(file.getName());
  }

  public TerminalScanner(@SuppressWarnings("unused") InputStream ignored) {
    fileContent = null;
  }

  public String nextLine() {
    if (fileContent == null) {
      return Terminal.readLine();
    }
    if (fileContentOffset >= fileContent.length) {
      throw new ReadMoreLinesThanProvidedException();
    }

    return fileContent[fileContentOffset++];
  }
}
