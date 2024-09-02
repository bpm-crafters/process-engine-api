package dev.bpmcrafters.example.common;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@ConditionalOnProperty(prefix = "dev.bpm-crafters.process-api", name = "adapter")
public class CommonFixtureAutoconfiguration {
}
