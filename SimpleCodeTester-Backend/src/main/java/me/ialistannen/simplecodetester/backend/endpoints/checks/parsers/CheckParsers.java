package me.ialistannen.simplecodetester.backend.endpoints.checks.parsers;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import me.ialistannen.simplecodetester.backend.exception.CheckParseException;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.checks.defaults.StaticInputOutputCheck;
import me.ialistannen.simplecodetester.checks.defaults.io.InterleavedStaticIOCheck;

/**
 * Contains parsers that parse some input to a proper check.
 */
public class CheckParsers {

  private Map<String, CheckParser<?>> parsers;

  /**
   * Creates a new collection of check parsers.
   *
   * @param gson the gson instance to use
   */
  public CheckParsers(Gson gson) {
    this.parsers = new HashMap<>();

    addParser(InterleavedStaticIOCheck.class, new InterleavedIoCheckParser(gson));
    addParser(StaticInputOutputCheck.class, new StaticInputOutputCheckParser(gson));
  }

  /**
   * Adds a new parser.
   *
   * @param checkClass the class of the check
   * @param parser the check parser
   * @param <T> the type of the check to parse
   */
  public <T extends Check> void addParser(Class<T> checkClass, CheckParser<T> parser) {
    parsers.put(checkClass.getSimpleName(), parser);
  }

  /**
   * Parses a payload to a check.
   *
   * @param payload the payload to parse
   * @param checkKeyword the keyword for the check
   * @param <T> the type of the check
   * @return the parsed check
   * @throws me.ialistannen.simplecodetester.backend.exception.CheckParseException if parsing
   *     fails
   */
  public <T extends Check> T parsePayload(String payload, String checkKeyword) {
    // actually safe, as we store it correctl
    @SuppressWarnings("unchecked")
    CheckParser<T> checkParser = (CheckParser<T>) parsers.get(checkKeyword);
    if (checkParser == null) {
      throw new CheckParseException("No parser found for " + checkKeyword);
    }
    return checkParser.parse(payload);
  }
}
