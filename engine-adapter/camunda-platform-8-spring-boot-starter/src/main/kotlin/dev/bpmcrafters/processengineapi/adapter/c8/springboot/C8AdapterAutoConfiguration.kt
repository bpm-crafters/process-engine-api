package dev.bpmcrafters.processengineapi.adapter.c8.springboot

import dev.bpmcrafters.processengineapi.adapter.c8.correlation.CorrelationApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.correlation.SignalApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.deploy.DeploymentApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.process.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.SubscribingUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.subscription.C8TaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.correlation.SignalApi
import dev.bpmcrafters.processengineapi.deploy.DeploymentApi
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.spring.client.CamundaAutoConfiguration
import org.springframework.beans.factory.annotation.Autowired
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
class C8AdapterAutoConfiguration {

  @Bean
  fun startProcessApi(zeebeClient: ZeebeClient): StartProcessApi = StartProcessApiImpl(
    zeebeClient = zeebeClient
  )

  @Bean
  fun taskCompletionApi(subscriptionRepository: SubscriptionRepository, @Autowired(required = false) subscribingUserTaskDelivery: SubscribingUserTaskDelivery?): TaskSubscriptionApi = C8TaskSubscriptionApiImpl(
    subscriptionRepository = subscriptionRepository,
    subscribingUserTaskDelivery = subscribingUserTaskDelivery,
  )

  @Bean
  fun correlationApi(zeebeClient: ZeebeClient): CorrelationApi = CorrelationApiImpl(
    zeebeClient = zeebeClient
  )

  @Bean
  fun signalApi(zeebeClient: ZeebeClient): SignalApi = SignalApiImpl(
    zeebeClient = zeebeClient
  )

  @Bean
  fun deploymentApi(zeebeClient: ZeebeClient): DeploymentApi = DeploymentApiImpl(
    zeebeClient = zeebeClient
  )

  @Bean
  fun subscriptionRepository(): SubscriptionRepository = InMemSubscriptionRepository()
}
