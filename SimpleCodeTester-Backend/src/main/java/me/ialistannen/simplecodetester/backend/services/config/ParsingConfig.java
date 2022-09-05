package me.ialistannen.simplecodetester.backend.services.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
}
