package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.remote.correlation.CorrelationApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.correlation.SignalApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.deploy.DeploymentApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.process.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.C7RemoteTaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.correlation.SignalApi
import dev.bpmcrafters.processengineapi.deploy.DeploymentApi
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.community.rest.EnableCamundaRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Provide all services for the users.
 */
@Configuration
@AutoConfigureAfter(C7RemoteAdapterAutoConfiguration::class)
@EnableCamundaRestClient
@ConditionalOnProperty(prefix = C7RemoteAdapterProperties.DEFAULT_PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = true)
class C7RemoteServiceApiAutoConfiguration {

  @Bean("c7remote-task-subscription-api")
  @Qualifier("c7remote-task-subscription-api")
  fun taskSubscriptionApi(subscriptionRepository: SubscriptionRepository): TaskSubscriptionApi = C7RemoteTaskSubscriptionApiImpl(
    subscriptionRepository = subscriptionRepository
  )

  @Bean("c7remote-start-process-api")
  @Qualifier("c7remote-start-process-api")
  fun startProcessApi(@Qualifier("remote") runtimeService: RuntimeService): StartProcessApi = StartProcessApiImpl(
    runtimeService = runtimeService
  )

  @Bean("c7remote-correlation-api")
  @Qualifier("c7remote-correlation-api")
  fun correlationApi(@Qualifier("remote") runtimeService: RuntimeService): CorrelationApi = CorrelationApiImpl(
    runtimeService = runtimeService
  )

  @Bean("c7remote-signal-api")
  @Qualifier("c7remote-signal-api")
  fun signalApi(@Qualifier("remote") runtimeService: RuntimeService): SignalApi = SignalApiImpl(
    runtimeService = runtimeService
  )

  @Bean("c7remote-deploy-api")
  @Qualifier("c7remote-deploy-api")
  fun deployApi(@Qualifier("remote") repositoryService: RepositoryService): DeploymentApi = DeploymentApiImpl(
    repositoryService = repositoryService
  )

}
