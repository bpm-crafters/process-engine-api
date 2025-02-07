package dev.bpmcrafters.example.common;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@Slf4j
public class CommonFixtureAutoconfiguration {

  @PostConstruct
  public void report() {
    log.info("[EXAMPLE] Started common example fixture actor configuration");
  }
}
