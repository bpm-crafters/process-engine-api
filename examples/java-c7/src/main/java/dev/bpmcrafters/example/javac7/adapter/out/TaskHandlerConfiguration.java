package dev.bpmcrafters.example.javac7.adapter.out;

import dev.bpmcrafters.processengineapi.task.TaskApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskHandlerConfiguration {

  @Bean(initMethod = "register", destroyMethod = "unregister")
  public DoAction1TaskHandler doAction1(TaskApi taskApi) {
    return new DoAction1TaskHandler(taskApi);
  }

  @Bean(initMethod = "register", destroyMethod = "unregister")
  public DoAction2TaskHandler doAction2(TaskApi taskApi) {
    return new DoAction2TaskHandler(taskApi);
  }
}
