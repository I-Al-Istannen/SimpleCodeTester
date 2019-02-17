package me.ialistannen.simplecodetester.backend.services.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "parsing")
@Getter
@Setter
@Valid
public class ParsingConfig {

  @Positive
  private int minCommands;

  @NotNull
  private String quitCommand;
}
