package me.ialistannen.simplecodetester.execution.slave;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Paths;
import me.ialistannen.simplecodetester.util.StringOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UntrustedCodeJvmStarterIT {

  private UntrustedCodeJvmStarter untrustedCodeJvmStarter;

  @BeforeEach
  void setUp() {
    untrustedCodeJvmStarter = new UntrustedCodeJvmStarter();
  }

  @Test
  void slaveIsStartedCorrectly() throws InterruptedException, IOException {
    Process process = untrustedCodeJvmStarter.startSlave(
        2222, "Test",
        Paths.get("target/SimpleCodeTester-Lib.jar").toAbsolutePath().toString()
    );

    StringOutputStream stringOutputStream = new StringOutputStream();

    process.getErrorStream().transferTo(stringOutputStream);

    process.waitFor();

    assertEquals(
        1,
        process.exitValue()
    );
    assertThat(
        stringOutputStream.toString(),
        matchesPattern(".+java.net.ConnectException:.+\\(Connection refused\\)[\\s\\S]+")
    );
  }

}