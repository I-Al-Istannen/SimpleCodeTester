package edu.kit.kastel.trafficsimulation.io;

import edu.kit.informatik.Terminal;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class SimulationFileLoader {

  private final Path prefix;

  public SimulationFileLoader(String prefix) throws IOException {
    this.prefix = Path.of(prefix).normalize();
  }

  public List<String> loadStreets() throws IOException {
    try {
      return Arrays.asList(Terminal.readFile(prefix.resolve("streets.sim").toString()));
    } catch (IllegalArgumentException e) {
      throw new IOException(e.getMessage());
    }
  }

  public List<String> loadCrossings() throws IOException {
    try {
      return Arrays.asList(Terminal.readFile(prefix.resolve("crossings.sim").toString()));
    } catch (IllegalArgumentException e) {
      throw new IOException(e.getMessage());
    }
  }

  public List<String> loadCars() throws IOException {
    try {
      return Arrays.asList(Terminal.readFile(prefix.resolve("cars.sim").toString()));
    } catch (IllegalArgumentException e) {
      throw new IOException(e.getMessage());
    }
  }

}
