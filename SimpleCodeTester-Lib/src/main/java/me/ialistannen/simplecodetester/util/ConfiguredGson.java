package me.ialistannen.simplecodetester.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ServiceLoader;

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
}
