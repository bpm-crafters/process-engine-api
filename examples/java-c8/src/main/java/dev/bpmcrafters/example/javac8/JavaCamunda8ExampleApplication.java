package dev.bpmcrafters.example.javac8;

import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties;
import io.camunda.common.auth.Product;
import io.camunda.common.auth.SimpleAuthentication;
import io.camunda.common.auth.SimpleConfig;
import io.camunda.common.auth.SimpleCredential;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.zeebe.spring.client.properties.CamundaClientProperties;
import io.camunda.zeebe.spring.client.properties.ZeebeClientConfigurationProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import static dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.DEFAULT_PREFIX;

@SpringBootApplication
@Slf4j
public class JavaCamunda8ExampleApplication {

  public static void main(String[] args) {
    SpringApplication.run(JavaCamunda8ExampleApplication.class, args);
  }

  @Bean
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = "user-tasks.completion-strategy", havingValue = "tasklist")
  @SneakyThrows
  public CamundaTaskListClient camundaTaskListClientSaaS(
    CamundaClientProperties clientProperties,
    C8AdapterProperties c8AdapterProperties
  ) {

    var builder = CamundaTaskListClient.builder()
      .taskListUrl(c8AdapterProperties.getUserTasks().getTasklistUrl())
      .shouldReturnVariables()
    ;

    switch (clientProperties.getMode()) {
      case saas -> {
        builder = builder.saaSAuthentication(
          clientProperties.getAuth().getClientId(),
          clientProperties.getAuth().getClientSecret()
        );
      }
      case oidc -> {
        builder = builder.selfManagedAuthentication(
          clientProperties.getAuth().getClientId(),
          clientProperties.getAuth().getClientSecret(),
          clientProperties.getAuth().getIssuer()
        );
      }
      case simple -> {
        val config = new SimpleConfig();
        config.addProduct(Product.TASKLIST, new SimpleCredential(
            c8AdapterProperties.getUserTasks().getTasklistUrl(),
            clientProperties.getAuth().getUsername(),
            clientProperties.getAuth().getPassword()
          )
        );
        builder = builder.authentication(
          SimpleAuthentication.builder().withSimpleConfig(config).build()
        );
      }
    }

    return builder.build();
  }

}
