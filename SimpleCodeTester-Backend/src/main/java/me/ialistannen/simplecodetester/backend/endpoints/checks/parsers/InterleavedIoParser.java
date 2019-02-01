package me.ialistannen.simplecodetester.backend.endpoints.checks.parsers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.ialistannen.simplecodetester.backend.exception.CheckParseException;
import me.ialistannen.simplecodetester.backend.util.StringReader;
import me.ialistannen.simplecodetester.checks.defaults.io.InterleavedStaticIOCheck;
import me.ialistannen.simplecodetester.util.Pair;

/**
 * A parser for an {@link InterleavedStaticIOCheck}.
 */
public class InterleavedIoParser implements CheckParser<InterleavedStaticIOCheck> {

  private Gson gson;

  /**
   * Creates a new InterleavedIoParser.
   *
   * @param gson the gson instance to use
   */
  public InterleavedIoParser(Gson gson) {
    this.gson = gson;
  }

  @Override
  public InterleavedStaticIOCheck parse(String payload) {
    try {
      JsonObject jsonObject = gson.fromJson(payload, JsonObject.class);
      String name = jsonObject.getAsJsonPrimitive("name").getAsString();

      String rawData = jsonObject.getAsJsonPrimitive("data").getAsString();

      ParserHelper parserHelper = new ParserHelper(rawData);

      return new InterleavedStaticIOCheck(parserHelper.parse(), name);
    } catch (JsonSyntaxException e) {
      throw new CheckParseException("Error parsing check: " + e.getMessage());
    }
  }

  /**
   * A helper class that keeps the state for a single input.
   */
  private static class ParserHelper {

    private List<Pair<String, List<String>>> data;
    private StringReader reader;

    private ParserHelper(String rawInput) {
      this.reader = new StringReader(rawInput);

      this.data = new ArrayList<>();
    }

    private List<Pair<String, List<String>>> parse() {
      if (!reader.canRead()) {
        return Collections.emptyList();
      }

      while (reader.canRead()) {
        parseInput();
      }

      return data;
    }

    private void parseInput() {
      if (!reader.peek(2).equalsIgnoreCase("> ")) {
        throw new CheckParseException("Expected input at " + reader.getCursor());
      }

      String input = reader.readLine();
      List<String> output = parseOutput();

      data.add(new Pair<>(input, output));
    }

    private List<String> parseOutput() {
      List<String> output = new ArrayList<>();

      while (reader.canRead() && !reader.peek(2).equalsIgnoreCase("> ")) {
        String line = reader.readLine();
        if (line == null) {
          break;
        }
        output.add(line);
      }

      return output;
    }
  }

}
