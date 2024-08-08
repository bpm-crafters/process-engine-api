package dev.bpmcrafters.processengineapi.adapter.c8.springboot

import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import io.camunda.tasklist.CamundaTaskListClient
import io.camunda.zeebe.spring.client.CamundaAutoConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.ConfigurationPropertySources
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.ConfigurableEnvironment

@Configuration
@AutoConfigureAfter(
  CamundaAutoConfiguration::class
)
@EnableConfigurationProperties(value = [C8AdapterProperties::class])
class C8AdapterAutoConfiguration {

  @Bean
  fun c8EnginesBeanPostProcessor(
    environment: ConfigurableEnvironment,
    subscriptionRepository: SubscriptionRepository,
    @Autowired(required = false)
    taskListClient: CamundaTaskListClient?,
  ): C8EnginesBeanDefinitionPostProcessor? {
    val binder = Binder(ConfigurationPropertySources.get(environment))
    val enabled = binder.bind("$DEFAULT_PREFIX.enabled", Boolean::class.java)
      .orElse(false)
    return if (enabled) {
      val properties = binder.bind(DEFAULT_PREFIX, C8AdapterProperties::class.java)
        .orElseThrow { IllegalStateException("Could not bind C8 configuration properties") }
      C8EnginesBeanDefinitionPostProcessor(
        engines = properties.engines,
        subscriptionRepository = subscriptionRepository,
        taskListClient = taskListClient
      )
    } else {
      null
    }
  }

  @Bean
  @ConditionalOnMissingBean
  fun subscriptionRepository(): SubscriptionRepository = InMemSubscriptionRepository()

}
