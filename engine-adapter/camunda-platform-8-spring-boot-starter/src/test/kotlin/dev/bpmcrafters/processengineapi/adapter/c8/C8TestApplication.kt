package dev.bpmcrafters.processengineapi.adapter.c8

import com.tngtech.jgiven.integration.spring.EnableJGiven
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingRefreshingUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.ExternalTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import io.camunda.tasklist.CamundaTaskListClient
import io.mockk.mockk
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary


@EnableJGiven
@SpringBootApplication
class C8TestApplication {

  @Bean
  fun processTestHelper(
    startProcessApi: StartProcessApi,
    taskSubscriptionApi: TaskSubscriptionApi,
    userTaskDelivery: SubscribingRefreshingUserTaskDelivery,
    subscribingServiceTaskDelivery: SubscribingServiceTaskDelivery,
    userTaskCompletionApi: UserTaskCompletionApi,
    externalTaskCompletionApi: ExternalTaskCompletionApi,
    subscriptionRepository: SubscriptionRepository,
  ): ProcessTestHelper = C8ProcessTestHelper(
    startProcessApi = startProcessApi,
    taskSubscriptionApi = taskSubscriptionApi,
    userTaskDelivery = userTaskDelivery,
    subscribingServiceTaskDelivery = subscribingServiceTaskDelivery,
    userTaskCompletionApi = userTaskCompletionApi,
    externalTaskCompletionApi = externalTaskCompletionApi,
    subscriptionRepository = subscriptionRepository
  )

}
