package me.ialistannen.simplecodetester.backend;

import me.ialistannen.simplecodetester.backend.db.storage.DatabaseStorage;
import me.ialistannen.simplecodetester.backend.services.config.ConfigurationService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {
    FlywayAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    DataSourceAutoConfiguration.class
})
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }

  @Bean
  public DatabaseStorage provideDatabaseStorage(ConfigurationService config) {
    return new DatabaseStorage(config.getDatabaseConfig().getUrl());
  }
}
