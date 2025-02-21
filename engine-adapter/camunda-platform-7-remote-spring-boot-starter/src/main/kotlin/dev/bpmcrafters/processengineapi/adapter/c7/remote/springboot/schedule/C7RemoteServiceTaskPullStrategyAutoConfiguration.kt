package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.*
import dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot.C7RemoteAdapterProperties.ExternalServiceTaskDeliveryStrategy.REMOTE_SCHEDULED
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull.RemotePullServiceTaskDelivery
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnJava
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import java.time.Duration
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

/**
 * Dynamic / imperative scheduling configuration using own task scheduler for service tasks.
 */
@EnableScheduling
@Configuration
@ConditionalOnServiceTaskDeliveryStrategy(
  strategy = REMOTE_SCHEDULED
)
@AutoConfigureAfter(C7RemoteSchedulingAutoConfiguration::class)
class C7RemoteServiceTaskPullStrategyAutoConfiguration(
  private val remotePullServiceTaskDelivery: RemotePullServiceTaskDelivery,
  private val c7remoteAdapterProperties: C7RemoteAdapterProperties,
  @Qualifier("c7remote-task-scheduler")
  private val c7taskScheduler: TaskScheduler
) : SchedulingConfigurer {

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

