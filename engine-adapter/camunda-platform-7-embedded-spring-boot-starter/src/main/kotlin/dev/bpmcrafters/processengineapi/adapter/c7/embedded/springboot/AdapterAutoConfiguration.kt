package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot

import com.fasterxml.jackson.databind.ObjectMapper
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.correlation.CorrelationApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.correlation.SignalApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.process.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.C7TaskApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion.ExternalTaskCompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion.UserTaskCompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.commons.task.CompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.ParsingHelper
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.correlation.SignalApi
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.TaskApi
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableConfigurationProperties(value = [C7AdapterProperties::class])
class AdapterAutoConfiguration {

  @Bean
  fun startProcessApi(runtimeService: RuntimeService): StartProcessApi = StartProcessApiImpl(
    runtimeService = runtimeService
  )

  @Bean
  fun taskApi(subscriptionRepository: SubscriptionRepository, completionStrategies: List<CompletionStrategy>): TaskApi = C7TaskApiImpl(
    completionStrategies = completionStrategies,
    subscriptionRepository = subscriptionRepository
  )

  @Bean
  fun correlationApi(runtimeService: RuntimeService, parsingHelper: ParsingHelper): CorrelationApi = CorrelationApiImpl(
    runtimeService = runtimeService,
    parsingHelper = parsingHelper
  )

  @Bean
  fun jacksonparsingHelper(objectMapper: ObjectMapper): ParsingHelper {
    return object: ParsingHelper {
      override fun parseJsonString(value: String): Map<String, Any> {
        return objectMapper.readValue(value, objectMapper.typeFactory.constructMapType(Map::class.java, String::class.java, Any::class.java))
      }
    }
  }

  @Bean
  fun signalApi(runtimeService: RuntimeService): SignalApi = SignalApiImpl(
    runtimeService = runtimeService
  )

  @Bean
  fun subscriptionRepository(): SubscriptionRepository = InMemSubscriptionRepository()

  @Bean
  fun externalTaskCompletionStrategy(
    externalTaskService: ExternalTaskService,
    subscriptionRepository: SubscriptionRepository,
    c7AdapterProperties: C7AdapterProperties
  ): CompletionStrategy =
    ExternalTaskCompletionStrategy(
      workerId = c7AdapterProperties.externalServiceTasks.workerId,
      externalTaskService = externalTaskService,
      subscriptionRepository = subscriptionRepository
    )

  @Bean
  fun userTaskCompletionStrategy(
    taskService: TaskService,
    subscriptionRepository: SubscriptionRepository
  ): CompletionStrategy =
    UserTaskCompletionStrategy(
      taskService = taskService,
      subscriptionRepository = subscriptionRepository
    )
}
