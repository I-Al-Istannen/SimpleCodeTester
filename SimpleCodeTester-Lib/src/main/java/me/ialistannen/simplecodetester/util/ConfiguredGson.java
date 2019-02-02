package me.ialistannen.simplecodetester.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import me.ialistannen.simplecodetester.checks.defaults.StaticInputOutputCheck;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.InterleavedIoMatcher;

public class ConfiguredGson {

  /**
   * Creates a new configured {@link Gson} instance.
   *
   * @return the configured gson instance
   */
  public static Gson createGson() {
    GsonBuilder gsonBuilder = new GsonBuilder();

    for (TypeAdapterFactory factory : ServiceLoader.load(TypeAdapterFactory.class)) {
      gsonBuilder.registerTypeAdapterFactory(factory);
    }

    return gsonBuilder
        .registerTypeAdapter(Path.class, new PathTypeAdapter())
        .registerTypeAdapter(StaticInputOutputCheck.class, new StaticInputOutputCheckAdapter())
        .registerTypeAdapterFactory(new InterleavedMatcherAdapterFactory())
        .create();
  }

  private static class PathTypeAdapter extends TypeAdapter<Path> {

    @Override
    public void write(JsonWriter out, Path value) throws IOException {
      out.value(value.toAbsolutePath().toString());
    }

    @Override
    public Path read(JsonReader in) throws IOException {
      return Paths.get(in.nextString());
    }
  }

  private static class StaticInputOutputCheckAdapter extends TypeAdapter<StaticInputOutputCheck> {

    @Override
    public void write(JsonWriter out, StaticInputOutputCheck value) throws IOException {
      out.beginObject();

      out.name("name");
      out.value(value.name());

      out.name("input");
      out.beginArray();
      for (String input : value.getInput()) {
        out.value(input);
      }
      out.endArray();

      out.name("expectedOutput");
      out.value(value.getExpectedOutput());

      out.endObject();
    }

    @Override
    public StaticInputOutputCheck read(JsonReader in) throws IOException {
      in.beginObject();

      in.nextName();
      String name = in.nextString();

      in.nextName();
      in.beginArray();

      List<String> input = new ArrayList<>();
      while (in.peek() != JsonToken.END_ARRAY) {
        input.add(in.nextString());
      }
      in.endArray();

      in.nextName();
      String expectedOutput = in.nextString();

      in.endObject();

      return new StaticInputOutputCheck(input, expectedOutput, name);
    }
  }

  private static class InterleavedMatcherAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (InterleavedIoMatcher.class != type.getRawType()) {
        return null;
      }
      return new TreeTypeAdapter<>(
          (src, typeOfSrc, context) -> {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("class", src.getClass().getName());
            jsonObject.add("value", context.serialize(src));

            return jsonObject;
          },
          (json, typeOfT, context) -> {
            JsonObject jsonObject = json.getAsJsonObject();
            String className = jsonObject.get("class").getAsString();

            Class<?> tType;
            try {
              tType = Class.forName(className);
            } catch (ClassNotFoundException e) {
              throw new JsonSyntaxException("Unknown class " + e);
            }

            return context.deserialize(jsonObject.get("value"), tType);
          },
          gson,
          type,
          this
      );
    }
  }
}
