package me.ialistannen.simplecodetester.backend.services.config;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class ConfigurationService {

  private ParsingConfig parsingConfig;

  public ConfigurationService(ParsingConfig parsingConfig) {
    this.parsingConfig = parsingConfig;
  }
}
