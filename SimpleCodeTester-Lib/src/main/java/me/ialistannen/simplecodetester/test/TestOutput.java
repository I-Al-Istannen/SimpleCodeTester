package me.ialistannen.simplecodetester.test;

import java.util.Collections;
import me.ialistannen.simplecodetester.checks.defaults.StaticInputOutputCheck;

public class TestOutput extends StaticInputOutputCheck {

  public TestOutput() {
    super(Collections.singletonList("Twinkle, Twinkle"), "Little star!\n");
  }
}
