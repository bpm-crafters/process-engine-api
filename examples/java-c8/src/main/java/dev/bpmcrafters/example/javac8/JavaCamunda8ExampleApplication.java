package dev.bpmcrafters.example.javac8;

import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.zeebe.spring.client.properties.CamundaClientProperties;
import io.camunda.zeebe.spring.client.properties.ZeebeClientConfigurationProperties;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import static dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.DEFAULT_PREFIX;

@SpringBootApplication
public class JavaCamunda8ExampleApplication {

  public static void main(String[] args) {
    SpringApplication.run(JavaCamunda8ExampleApplication.class, args);
  }

  @Bean
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = "engines.default.user-tasks.completion-strategy", havingValue = "tasklist")
  @SneakyThrows
  public CamundaTaskListClient camundaTaskListClientSaaS(
    CamundaClientProperties camundaClientProperties,
    C8AdapterProperties c8AdapterProperties
  ) {
    return CamundaTaskListClient
      .builder()
      .taskListUrl(c8AdapterProperties.getEngines().values().stream().findFirst().get().getUserTasks().getTasklistUrl())
      .saaSAuthentication(
        camundaClientProperties.getAuth().getClientId(),
        camundaClientProperties.getAuth().getClientSecret()
      )
      .shouldReturnVariables()
      .build();
  }

}
