package me.ialistannen.simplecodetester.checks.storage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.ialistannen.simplecodetester.checks.Check;

/**
 * A class that can (de-)serialize checks to/from json.
 */
public class CheckSerializer {

  private Gson gson;

  /**
   * Creates a new check serializer.
   *
   * @param gson the gson instance to use
   */
  public CheckSerializer(Gson gson) {
    this.gson = gson;
  }

  /**
   * Converts a check to json blob.
   *
   * @param check the check
   * @return a json blob representing thr check
   */
  public String toJson(Check check) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("class", check.getClass().getName());
    jsonObject.addProperty("value", gson.toJson(check));

    return gson.toJson(jsonObject);
  }

  /**
   * Converts a string representation of a check to a {@link JsonObject}.
   *
   * @param checkJson the check's json
   * @return the created json object
   */
  public JsonObject toJson(String checkJson) {
    return gson.fromJson(checkJson, JsonObject.class);
  }

  /**
   * Converts a check back from json.
   *
   * @param json the json representation
   * @return the read check
   */
  public Check fromJson(String json) {
    try {
      JsonObject jsonObject = toJson(json);
      @SuppressWarnings("unchecked")
      Class<Check> checkClass = (Class<Check>) Class
          .forName(jsonObject.getAsJsonPrimitive("class").getAsString());

      if (!Check.class.isAssignableFrom(checkClass)) {
        throw new IllegalArgumentException("The check class did not extend check: " + checkClass);
      }

      String value = jsonObject.get("value").getAsString();

      return gson.fromJson(value, checkClass);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Check class not found", e);
    } catch (JsonSyntaxException | NullPointerException e) {
      throw new IllegalArgumentException("Malformed json: " + e.getMessage() + "\n" + json);
    }
  }
}
