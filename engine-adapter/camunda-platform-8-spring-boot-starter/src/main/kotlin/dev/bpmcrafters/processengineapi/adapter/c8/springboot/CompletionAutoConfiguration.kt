package dev.bpmcrafters.processengineapi.adapter.c8.springboot

import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.C8TaskListClientUserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.C8ZeebeExternalServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.C8ZeebeUserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.task.ExternalTaskCompletionApi
import dev.bpmcrafters.processengineapi.task.UserTaskCompletionApi
import io.camunda.tasklist.CamundaTaskListClient
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.spring.client.properties.ZeebeClientConfigurationProperties
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration for task completion.
 */
@Configuration
@AutoConfigureAfter(C8AdapterAutoConfiguration::class)
class CompletionAutoConfiguration {
  @Bean
  fun externalTaskCompletionStrategy(
    zeebeClient: ZeebeClient,
    subscriptionRepository: SubscriptionRepository,
  ): ExternalTaskCompletionApi =
    C8ZeebeExternalServiceTaskCompletionApiImpl(
      zeebeClient = zeebeClient,
      subscriptionRepository = subscriptionRepository
    )

  @Bean
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.completion-strategy"], havingValue = "tasklist")
  fun camundaTaskListClientSaaS(
    zeebeClientCloudConfigurationProperties: ZeebeClientConfigurationProperties,
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
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.completion-strategy"], havingValue = "tasklist")
  fun tasklistUserTaskCompletionStrategy(
    taskListClient: CamundaTaskListClient,
    subscriptionRepository: SubscriptionRepository
  ): UserTaskCompletionApi =
    C8TaskListClientUserTaskCompletionApiImpl(
      taskListClient = taskListClient,
      subscriptionRepository = subscriptionRepository
    )

  @Bean
  @ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["user-tasks.completion-strategy"], havingValue = "job")
  fun zeebeUserTaskCompletionStrategy(
    zeebeClient: ZeebeClient,
    subscriptionRepository: SubscriptionRepository
  ): UserTaskCompletionApi =
    C8ZeebeUserTaskCompletionApiImpl(
      zeebeClient = zeebeClient,
      subscriptionRepository = subscriptionRepository
    )

}
