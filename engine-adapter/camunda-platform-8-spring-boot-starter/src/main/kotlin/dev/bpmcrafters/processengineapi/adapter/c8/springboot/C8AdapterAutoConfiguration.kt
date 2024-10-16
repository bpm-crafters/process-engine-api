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
import io.camunda.zeebe.spring.client.configuration.CamundaAutoConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration

@Configuration
@AutoConfigureAfter(
  CamundaAutoConfiguration::class
)
@EnableConfigurationProperties(value = [C8AdapterProperties::class])
@Conditional(C8AdapterEnabledCondition::class)
class C8AdapterAutoConfiguration {

  @Bean("c8-start-process-api")
  @Qualifier("c8-start-process-api")
  fun startProcessApi(zeebeClient: ZeebeClient): StartProcessApi = StartProcessApiImpl(
    zeebeClient = zeebeClient
  )

  @Bean("c8-task-completion-api")
  @Qualifier("c8-task-completion-api")
  fun taskCompletionApi(
    subscriptionRepository: SubscriptionRepository,
    @Autowired(required = false) subscribingUserTaskDelivery: SubscribingUserTaskDelivery?
  ): TaskSubscriptionApi = C8TaskSubscriptionApiImpl(
    subscriptionRepository = subscriptionRepository,
    subscribingUserTaskDelivery = subscribingUserTaskDelivery,
  )

  @Bean("c8-correlation-api")
  @Qualifier("c8-correlation-api")
  fun correlationApi(zeebeClient: ZeebeClient): CorrelationApi = CorrelationApiImpl(
    zeebeClient = zeebeClient
  )

  @Bean("c8-signal-api")
  @Qualifier("c8-signal-api")
  fun signalApi(zeebeClient: ZeebeClient): SignalApi = SignalApiImpl(
    zeebeClient = zeebeClient
  )

  @Bean("c8-deploy-api")
  @Qualifier("c8-deploy-api")
  fun deploymentApi(zeebeClient: ZeebeClient): DeploymentApi = DeploymentApiImpl(
    zeebeClient = zeebeClient
  )

  @Bean
  @ConditionalOnMissingBean
  fun subscriptionRepository(): SubscriptionRepository = InMemSubscriptionRepository()

}
