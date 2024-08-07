package dev.bpmcrafters.processengineapi.adapter.c8.springboot

import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.PullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingRefreshingUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.RefreshableDelivery
import mu.KLogging
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executor

@EnableScheduling
@Configuration
@AutoConfigureAfter(C8AdapterAutoConfiguration::class)
class C8DynamicSchedulingConfigurer(
  private val taskExecutor: Executor,
  private val deliveries: Map<String, RefreshableDelivery>,
  private val c8AdapterProperties: C8AdapterProperties
) : SchedulingConfigurer {

  companion object: KLogging()

  override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
    taskRegistrar.setScheduler(taskExecutor)
    deliveries.filter { (name, _) ->
      name.endsWith("-user-task-delivery") && c8AdapterProperties.engines.containsKey(name.removeSuffix("-user-task-delivery"))
    }.forEach { (name, delivery) ->
      val engineConfiguration =
        requireNotNull(c8AdapterProperties.engines[name.removeSuffix("-user-task-delivery")]) { "Could not find engine configuration for $name" }
      when (delivery) {
        is PullUserTaskDelivery -> {
          taskRegistrar.addFixedRateTask(
            {
              logger.debug { "Refreshing tasks for $name..." }
              delivery.refresh()
              logger.debug { "Refreshed tasks for $name." }
            }, Duration.of(engineConfiguration.userTasks.fixedRateScheduleRate, ChronoUnit.MILLIS)
          )
        }

        is SubscribingRefreshingUserTaskDelivery -> {
          taskRegistrar.addFixedRateTask(
            {
              logger.debug { "Refreshing tasks for $name..." }
              delivery.refresh()
              logger.debug { "Refreshed tasks for $name." }
            }, Duration.of(engineConfiguration.userTasks.fixedRateScheduleRate, ChronoUnit.MILLIS)
          )
        }
      }
    }
  }
}
