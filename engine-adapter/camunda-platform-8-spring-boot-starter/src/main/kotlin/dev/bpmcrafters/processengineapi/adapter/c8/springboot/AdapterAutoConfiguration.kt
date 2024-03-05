package dev.bpmcrafters.processengineapi.adapter.c8.springboot

import dev.bpmcrafters.processengineapi.adapter.c8.correlation.CorrelationApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.process.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.C8TaskApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.ServiceTaskCompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.UserTaskCompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.commons.task.CompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.TaskApi
import io.camunda.tasklist.CamundaTaskListClient
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.spring.client.CamundaAutoConfiguration
import io.camunda.zeebe.spring.client.properties.ZeebeClientConfigurationProperties
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@Configuration
@AutoConfigureAfter(
  CamundaAutoConfiguration::class
)
@EnableConfigurationProperties(value = [C8AdapterProperties::class])
class AdapterAutoConfiguration {


  @Bean
  fun camundaTaskListClientSaaS(zeebeClientCloudConfigurationProperties: ZeebeClientConfigurationProperties,
                                c8AdapterProperties: C8AdapterProperties
  ): CamundaTaskListClient {
    /*
    val jwtConfig = JwtConfig()
    jwtConfig.addProduct(Product.TASKLIST,
      JwtCredential(
        zeebeClientCloudConfigurationProperties.cloud.clientId,
        zeebeClientCloudConfigurationProperties.cloud.clientSecret,
        "tasklist.camunda.io",
        "https://login.cloud.camunda.io/oauth/token")
    )
    val authentication = SaaSAuthentication.builder().jwtConfig(jwtConfig).build()
    */

    return CamundaTaskListClient
      .builder()
      .taskListUrl(c8AdapterProperties.userTasks.tasklistUrl)
      .saaSAuthentication(
        zeebeClientCloudConfigurationProperties.cloud.clientId,
        zeebeClientCloudConfigurationProperties.cloud.clientSecret,
      )
      .shouldReturnVariables()
      // .authentication(authentication) // produces NPE
      .build()
  }

  @Bean
  fun startProcessApi(zeebeClient: ZeebeClient): StartProcessApi = StartProcessApiImpl(
    zeebeClient = zeebeClient
  )

  @Bean
  fun taskApi(subscriptionRepository: SubscriptionRepository, completionStrategies: List<CompletionStrategy>): TaskApi = C8TaskApiImpl(
    completionStrategies = completionStrategies,
    subscriptionRepository = subscriptionRepository
  )

  @Bean
  fun correlationApi(zeebeClient: ZeebeClient): CorrelationApi = CorrelationApiImpl(
    zeebeClient = zeebeClient
  )

  @Bean
  fun subscriptionRepository(): SubscriptionRepository = InMemSubscriptionRepository()

  @Bean
  fun externalTaskCompletionStrategy(
    zeebeClient: ZeebeClient,
    subscriptionRepository: SubscriptionRepository,
  ): CompletionStrategy =
    ServiceTaskCompletionStrategy(
      zeebeClient = zeebeClient,
      subscriptionRepository = subscriptionRepository
    )

  @Bean
  fun userTaskCompletionStrategy(
    taskListClient: CamundaTaskListClient,
    subscriptionRepository: SubscriptionRepository
  ): CompletionStrategy =
    UserTaskCompletionStrategy(
      taskListClient = taskListClient,
      subscriptionRepository = subscriptionRepository
    )
}
