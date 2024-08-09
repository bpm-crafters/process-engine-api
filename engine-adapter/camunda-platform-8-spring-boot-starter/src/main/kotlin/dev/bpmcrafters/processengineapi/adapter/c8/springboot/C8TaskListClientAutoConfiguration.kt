package dev.bpmcrafters.processengineapi.adapter.c8.springboot

import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.C8TaskListClientUserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.PullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import io.camunda.tasklist.CamundaTaskListClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration using camunda task list client.
 */
@Configuration
@AutoConfigureAfter(C8AdapterAutoConfiguration::class)
@ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = true)
class C8TaskListClientAutoConfiguration {

  @Bean("c8-user-task-completion")
  @Qualifier("c8-user-task-completion")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.completion-strategy"], havingValue = "tasklist")
  fun tasklistUserTaskCompletionStrategy(
    taskListClient: CamundaTaskListClient,
    subscriptionRepository: SubscriptionRepository
  ): UserTaskCompletionApi {
    return C8TaskListClientUserTaskCompletionApiImpl(
      taskListClient = taskListClient,
      subscriptionRepository = subscriptionRepository
    )
  }

  @Bean("c8-user-task-delivery")
  @Qualifier("c8-user-task-delivery")
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.delivery-strategy"], havingValue = "scheduled")
  fun scheduledUserTaskDelivery(
    subscriptionRepository: SubscriptionRepository,
    taskListClient: CamundaTaskListClient,
  ): PullUserTaskDelivery {
    return PullUserTaskDelivery(
      subscriptionRepository = subscriptionRepository,
      taskListClient = taskListClient
    )
  }

}
