package me.ialistannen.simplecodetester.test;

import edu.kit.informatik.Terminal;

public class Test {

  public static void main(String[] args) {
    String line = Terminal.readLine();
    String line2 = Terminal.readLine();
    Terminal.printLine(line + line2);
  }
}
