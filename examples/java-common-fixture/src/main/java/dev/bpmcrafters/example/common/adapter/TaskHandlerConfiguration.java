package dev.bpmcrafters.example.common.adapter;

import dev.bpmcrafters.example.common.adapter.in.process.ExecuteActionTaskHandler;
import dev.bpmcrafters.example.common.adapter.in.process.SendingTaskHandler;
import dev.bpmcrafters.example.common.adapter.out.process.UserTaskAdapter;
import dev.bpmcrafters.processengineapi.CommonRestrictions;
import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport;
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi;
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi;
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskHandlerConfiguration {

  @Bean(initMethod = "register", destroyMethod = "unregister")
  public ExecuteActionTaskHandler doAction1(
      TaskSubscriptionApi taskSubscriptionApi,
      ServiceTaskCompletionApi serviceTaskCompletionApi
  ) {
    return new ExecuteActionTaskHandler(taskSubscriptionApi, serviceTaskCompletionApi);
  }

  @Bean(initMethod = "register", destroyMethod = "unregister")
  public SendingTaskHandler doAction2(TaskSubscriptionApi taskSubscriptionApi, ServiceTaskCompletionApi serviceTaskCompletionApi) {
    return new SendingTaskHandler(taskSubscriptionApi, serviceTaskCompletionApi);
  }

  @Bean
  public UserTaskAdapter userTaskHandler(UserTaskSupport support, UserTaskCompletionApi userTaskCompletionApi) {
    return new UserTaskAdapter(support, userTaskCompletionApi);
  }

  @Bean
  public UserTaskSupport userTaskSupport(TaskSubscriptionApi taskSubscriptionApi) {
    var support= new UserTaskSupport();
    support.subscribe(taskSubscriptionApi, CommonRestrictions.builder().build(), null, null);
    return support;
  }

}
