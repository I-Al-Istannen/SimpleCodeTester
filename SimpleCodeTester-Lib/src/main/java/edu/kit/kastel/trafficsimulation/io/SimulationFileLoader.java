package edu.kit.kastel.trafficsimulation.io;

import edu.kit.informatik.Terminal;
import java.util.Arrays;
import java.util.List;

public class SimulationFileLoader {

  private final String prefix;

  public SimulationFileLoader(String prefix) {
    this.prefix = !prefix.isEmpty() && !prefix.endsWith("/") ? prefix + "/" : prefix;
  }

  public List<String> loadStreets() {
    return Arrays.asList(Terminal.readFile(prefix + "streets.sim"));
  }

  public List<String> loadCrossings() {
    return Arrays.asList(Terminal.readFile(prefix + "crossings.sim"));
  }

  public List<String> loadCars() {
    return Arrays.asList(Terminal.readFile(prefix + "cars.sim"));
  }

}
