package edu.kit.kastel.trafficsimulation.io;

import edu.kit.informatik.Terminal;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class SimulationFileLoader {

  private final Path prefix;

  public SimulationFileLoader(String prefix) {
    this.prefix = Path.of(prefix).normalize();
  }

  public List<String> loadStreets() {
    return Arrays.asList(Terminal.readFile(prefix.resolve("streets.sim").toString()));
  }

  public List<String> loadCrossings() {
    return Arrays.asList(Terminal.readFile(prefix.resolve("crossings.sim").toString()));
  }

  public List<String> loadCars() {
    return Arrays.asList(Terminal.readFile(prefix.resolve("cars.sim").toString()));
  }

}
