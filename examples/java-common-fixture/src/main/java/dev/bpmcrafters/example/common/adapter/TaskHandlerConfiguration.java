package dev.bpmcrafters.example.common.adapter;

import dev.bpmcrafters.example.common.adapter.in.process.ExecuteActionTaskHandler;
import dev.bpmcrafters.example.common.adapter.in.process.SendingTaskHandler;
import dev.bpmcrafters.example.common.adapter.out.process.InMemUserTaskAdapter;
import dev.bpmcrafters.processengineapi.task.ExternalTaskCompletionApi;
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskHandlerConfiguration {

  @Bean(initMethod = "register", destroyMethod = "unregister")
  public ExecuteActionTaskHandler doAction1(
    TaskSubscriptionApi taskSubscriptionApi,
    ExternalTaskCompletionApi externalTaskCompletionApi
  ) {
    return new ExecuteActionTaskHandler(taskSubscriptionApi, externalTaskCompletionApi);
  }

  @Bean(initMethod = "register", destroyMethod = "unregister")
  public SendingTaskHandler doAction2(TaskSubscriptionApi taskSubscriptionApi, ExternalTaskCompletionApi externalTaskCompletionApi) {
    return new SendingTaskHandler(taskSubscriptionApi, externalTaskCompletionApi);
  }

  @Bean(initMethod = "register")
  public InMemUserTaskAdapter userTaskHandler(TaskSubscriptionApi taskSubscriptionApi) {
    return new InMemUserTaskAdapter(taskSubscriptionApi);
  }
}
