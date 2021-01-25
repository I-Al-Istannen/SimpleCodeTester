package me.ialistannen.simplecodetester.backend.services.config;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class ConfigurationService {

  private final ParsingConfig parsingConfig;
  private final DatabaseConfig databaseConfig;

  public ConfigurationService(ParsingConfig parsingConfig, DatabaseConfig databaseConfig) {
    this.parsingConfig = parsingConfig;
    this.databaseConfig = databaseConfig;
  }
}
