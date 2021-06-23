package me.ialistannen.simplecodetester.execution.diana;

import edu.kit.informatik.Terminal;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import me.ialistannen.simplecodetester.exceptions.ReadMoreLinesThanProvidedException;

public class TerminalBufferedReader {

  private final String[] fileContent;
  private int fileContentOffset;

  public TerminalBufferedReader(Reader reader) {
    if (reader instanceof FileReader) {
      try {
        Field lockField = Reader.class.getDeclaredField("lock");
        lockField.setAccessible(true);
        FileInputStream fileInputStream = (FileInputStream) lockField.get(reader);
        Field pathField = FileInputStream.class.getDeclaredField("path");
        String path = (String) pathField.get(fileInputStream);

        fileContent = Terminal.readFile(new File(path).getName());
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }
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
