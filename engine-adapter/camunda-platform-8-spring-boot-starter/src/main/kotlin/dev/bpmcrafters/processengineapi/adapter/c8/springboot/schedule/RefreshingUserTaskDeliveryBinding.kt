package dev.bpmcrafters.processengineapi.adapter.c8.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingRefreshingUserTaskDelivery
import mu.KLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import java.time.Duration
import java.time.temporal.ChronoUnit

class RefreshingUserTaskDeliveryBinding(
  private val subscribingRefreshingUserTaskDelivery: SubscribingRefreshingUserTaskDelivery,
  private val c8AdapterProperties: C8AdapterProperties,
  @Qualifier("c8-task-scheduler")
  private val c8taskScheduler: TaskScheduler
) : SchedulingConfigurer {

  companion object : KLogging()

  override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
    taskRegistrar.setScheduler(c8taskScheduler)
    taskRegistrar.addFixedRateTask(
      {
        logger.trace { "PROCESS-ENGINE-C7-REMOTE-104: Refreshing user tasks..." }
        subscribingRefreshingUserTaskDelivery.refresh()
        logger.trace { "PROCESS-ENGINE-C7-REMOTE-105: Refreshed user tasks." }
      },
      Duration.of(c8AdapterProperties.userTasks.scheduleDeliveryFixedRateInSeconds, ChronoUnit.SECONDS)
    )
  }


}
