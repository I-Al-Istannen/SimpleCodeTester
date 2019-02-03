package me.ialistannen.simplecodetester.backend.endpoints.checks.parsers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.List;
import me.ialistannen.simplecodetester.backend.exception.CheckParseException;
import me.ialistannen.simplecodetester.checks.defaults.StaticInputOutputCheck;

/**
 * A parser for a {@link StaticInputOutputCheck}.
 */
public class StaticInputOutputCheckParser implements CheckParser<StaticInputOutputCheck> {

  private Gson gson;

  /**
   * Creates a new static input output parser.
   *
   * @param gson the gson to use
   */
  public StaticInputOutputCheckParser(Gson gson) {
    this.gson = gson;
  }

  @Override
  public StaticInputOutputCheck parse(String payload) {
    try {
      ResponseBase response = gson.fromJson(payload, ResponseBase.class);

      List<String> input = Arrays.asList(response.data.input.split("\n"));

      return new StaticInputOutputCheck(input, response.data.output, response.name);
    } catch (JsonSyntaxException e) {
      throw new CheckParseException("Error parsing check: " + e.getMessage());
    }
  }

  private static class ResponseBase {

    String name;

    CheckRepresentation data;
  }

  private static class CheckRepresentation {

    String input;
    String output;
  }
}
