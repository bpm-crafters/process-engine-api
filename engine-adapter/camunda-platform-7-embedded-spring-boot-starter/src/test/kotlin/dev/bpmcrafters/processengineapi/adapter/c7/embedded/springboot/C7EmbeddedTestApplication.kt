package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot

import com.tngtech.jgiven.integration.spring.EnableJGiven
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.ExternalServiceTaskDeliveryStrategy.EMBEDDED_SCHEDULED
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.ServiceTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.RuntimeService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean


@EnableJGiven
@SpringBootApplication
class C7EmbeddedTestApplication {

  @ConditionalOnServiceTaskDeliveryStrategy(
    strategy = EMBEDDED_SCHEDULED
  )
  @Bean
  fun processTestHelper(
      processEngine: ProcessEngine,
      runtimeService: RuntimeService,
      startProcessApi: StartProcessApi,
      taskSubscriptionApi: TaskSubscriptionApi,
      userTaskDelivery: EmbeddedPullUserTaskDelivery,
      externalTaskDelivery: EmbeddedPullServiceTaskDelivery,
      userTaskCompletionApi: UserTaskCompletionApi,
      serviceTaskCompletionApi: ServiceTaskCompletionApi,
      subscriptionRepository: SubscriptionRepository,
  ): ProcessTestHelper = C7EmbeddedSpringProcessTestHelper(
    runtimeService = runtimeService,
    startProcessApi = startProcessApi,
    taskSubscriptionApi = taskSubscriptionApi,
    userTaskDelivery = userTaskDelivery,
    externalTaskDelivery = externalTaskDelivery,
    userTaskCompletionApi = userTaskCompletionApi,
    serviceTaskCompletionApi = serviceTaskCompletionApi,
    subscriptionRepository = subscriptionRepository
  )

}

