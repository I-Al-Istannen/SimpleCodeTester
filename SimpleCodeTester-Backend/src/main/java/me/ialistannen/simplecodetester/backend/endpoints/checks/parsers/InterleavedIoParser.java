package me.ialistannen.simplecodetester.backend.endpoints.checks.parsers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.ialistannen.simplecodetester.backend.exception.CheckParseException;
import me.ialistannen.simplecodetester.checks.defaults.io.InterleavedStaticIOCheck;

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

      rawData.lines()
          .map(s ->)

      return new InterleavedStaticIOCheck(parserHelper.parse(), name);
    } catch (JsonSyntaxException e) {
      throw new CheckParseException("Error parsing check: " + e.getMessage());
    }
  }

}
