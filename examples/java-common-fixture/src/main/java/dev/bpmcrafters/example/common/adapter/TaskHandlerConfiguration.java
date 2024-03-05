package dev.bpmcrafters.example.common.adapter;

import dev.bpmcrafters.example.common.adapter.in.process.ExecuteActionTaskHandler;
import dev.bpmcrafters.example.common.adapter.in.process.SendingTaskHandler;
import dev.bpmcrafters.example.common.adapter.out.process.InMemUserTaskAdapter;
import dev.bpmcrafters.processengineapi.task.TaskApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskHandlerConfiguration {

  @Bean(initMethod = "register", destroyMethod = "unregister")
  public ExecuteActionTaskHandler doAction1(TaskApi taskApi) {
    return new ExecuteActionTaskHandler(taskApi);
  }

  @Bean(initMethod = "register", destroyMethod = "unregister")
  public SendingTaskHandler doAction2(TaskApi taskApi) {
    return new SendingTaskHandler(taskApi);
  }

  @Bean(initMethod = "register")
  public InMemUserTaskAdapter userTaskHandler(TaskApi taskApi) {
    return new InMemUserTaskAdapter(taskApi);
  }
}
