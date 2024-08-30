package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterEnabledCondition
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties.ExternalServiceTaskDeliveryStrategy.REMOTE_SCHEDULED
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull.RemotePullServiceTaskDelivery
import mu.KLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.context.properties.bind.BindResult
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * Dynamic / imperative scheduling configuration using own task scheduler for service tasks.
 */
@EnableScheduling
@Configuration
@Conditional(
  C7RemoteAdapterEnabledCondition::class,
  C7RemoteAdapterServiceTaskSchedulesDeliveryCondition::class
)
@AutoConfigureAfter(C7RemoteSchedulingAutoConfiguration::class)
class C7RemoteServiceTaskPullStrategyAutoConfiguration(
  private val remotePullServiceTaskDelivery: RemotePullServiceTaskDelivery,
  private val c7remoteAdapterProperties: C7RemoteAdapterProperties,
  @Qualifier("c7remote-task-scheduler")
  private val c7taskScheduler: TaskScheduler
) : SchedulingConfigurer {

  companion object : KLogging()

  override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
    taskRegistrar.setScheduler(c7taskScheduler)
    taskRegistrar.addFixedRateTask(
      {
        logger.trace { "PROCESS-ENGINE-C7-REMOTE-105: Delivering external tasks..." }
        remotePullServiceTaskDelivery.refresh()
        logger.trace { "PROCESS-ENGINE-C7-REMOTE-106: Delivered external tasks." }
      },
      Duration.of(c7remoteAdapterProperties.serviceTasks.scheduleDeliveryFixedRateInSeconds, ChronoUnit.SECONDS)
    )
  }

}

/**
 * Condition which returns true if `dev.bpm-crafters.process-api.adapter.c7remote.serviceTasks.deliveryStrategy` has value `REMOTE_SCHEDULED`
 */
class C7RemoteAdapterServiceTaskSchedulesDeliveryCondition : Condition {
  override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
    val propertiesBindResult: BindResult<C7RemoteAdapterProperties> = Binder.get(context.environment)
      .bind(DEFAULT_PREFIX, C7RemoteAdapterProperties::class.java)

    if (propertiesBindResult.isBound) {
      val properties: C7RemoteAdapterProperties = propertiesBindResult.get()
      return properties.serviceTasks.deliveryStrategy == REMOTE_SCHEDULED
    }

    return false
  }
}
