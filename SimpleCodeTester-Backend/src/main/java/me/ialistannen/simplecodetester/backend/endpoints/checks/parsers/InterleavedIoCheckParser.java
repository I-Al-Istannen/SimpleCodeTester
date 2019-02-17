package me.ialistannen.simplecodetester.backend.endpoints.checks.parsers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.ialistannen.simplecodetester.backend.exception.CheckParseException;
import me.ialistannen.simplecodetester.backend.services.config.ParsingConfig;
import me.ialistannen.simplecodetester.checks.defaults.io.InterleavedStaticIOCheck;
import me.ialistannen.simplecodetester.checks.defaults.io.parsing.InterleavedIoParser;

/**
 * A parser for an {@link InterleavedStaticIOCheck}.
 */
public class InterleavedIoCheckParser implements CheckParser<InterleavedStaticIOCheck> {

  private final InterleavedIoParser interleavedIoParser;
  private final Gson gson;

  /**
   * Creates a new InterleavedIoCheckParser.
   *
   * @param gson the gson instance to use
   * @param parsingConfig the parsing config
   */
  public InterleavedIoCheckParser(Gson gson, ParsingConfig parsingConfig) {
    this.gson = gson;
    this.interleavedIoParser = new InterleavedIoParser(
        parsingConfig.getQuitCommand(),
        parsingConfig.getMinCommands()
    );
  }

  @Override
  public InterleavedStaticIOCheck parse(String payload) {
    try {
      ResponseBase responseBase = gson.fromJson(payload, ResponseBase.class);

      return interleavedIoParser.fromString(responseBase.data.input, responseBase.name);
    } catch (JsonSyntaxException | IllegalArgumentException e) {
      throw new CheckParseException("Error parsing check: " + e.getMessage());
    }
  }

  private static class ResponseBase {

    String name;
    CheckRepresentation data;
  }

  private static class CheckRepresentation {

    String input;
  }
}
