package me.ialistannen.simplecodetester.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.Expose;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import lombok.experimental.UtilityClass;
import me.ialistannen.simplecodetester.checks.GsonAdaptersCheckResult;
import me.ialistannen.simplecodetester.checks.defaults.StaticInputOutputCheck;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.InterleavedIoMatcher;
import me.ialistannen.simplecodetester.submission.GsonAdaptersCompleteTask;
import me.ialistannen.simplecodetester.submission.GsonAdaptersSubmission;

/**
 * A helper to create a configured {@link Gson} instance.
 */
@UtilityClass
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
        // Respect "Expose" annotations
        .addSerializationExclusionStrategy(new ExclusionStrategy() {
          @Override
          public boolean shouldSkipField(FieldAttributes f) {
            Expose annotation = f.getAnnotation(Expose.class);
            if (annotation == null) {
              return false;
            }
            return !annotation.serialize();
          }

          @Override
          public boolean shouldSkipClass(Class<?> clazz) {
            return false;
          }
        })
        .registerTypeAdapter(Path.class, new PathTypeAdapter())
        .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
        .registerTypeAdapter(StaticInputOutputCheck.class, new StaticInputOutputCheckAdapter())
        .registerTypeAdapterFactory(new GsonAdaptersCompleteTask())
        .registerTypeAdapterFactory(new GsonAdaptersCheckResult())
        .registerTypeAdapterFactory(new GsonAdaptersSubmission())
        .registerTypeAdapterFactory(new InterleavedMatcherAdapterFactory())
        .registerTypeAdapterFactory(OptionalTypeAdapter.FACTORY)
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

  private static class InstantTypeAdapter extends TypeAdapter<Instant> {

    @Override
    public void write(JsonWriter out, Instant value) throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value.toEpochMilli());
      }
    }

    @Override
    public Instant read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return Instant.ofEpochMilli(in.nextLong());
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

  // https://stackoverflow.com/a/25078422
  public static class OptionalTypeAdapter<E> extends TypeAdapter<Optional<E>> {

    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
      @SuppressWarnings("unchecked")
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<T> rawType = (Class<T>) type.getRawType();
        if (rawType != Optional.class) {
          return null;
        }
        final ParameterizedType parameterizedType = (ParameterizedType) type.getType();
        final Type actualType = parameterizedType.getActualTypeArguments()[0];
        final TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(actualType));
        return new OptionalTypeAdapter(adapter);
      }
    };

    private final TypeAdapter<E> adapter;

    public OptionalTypeAdapter(TypeAdapter<E> adapter) {
      this.adapter = adapter;
    }

    @Override
    public void write(JsonWriter out, Optional<E> value) throws IOException {
      if (value.isPresent()) {
        adapter.write(out, value.get());
      } else {
        out.nullValue();
      }
    }

    @Override
    public Optional<E> read(JsonReader in) throws IOException {
      final JsonToken peek = in.peek();
      if (peek != JsonToken.NULL) {
        return Optional.ofNullable(adapter.read(in));
      }

      in.nextNull();
      return Optional.empty();
    }
  }

}
