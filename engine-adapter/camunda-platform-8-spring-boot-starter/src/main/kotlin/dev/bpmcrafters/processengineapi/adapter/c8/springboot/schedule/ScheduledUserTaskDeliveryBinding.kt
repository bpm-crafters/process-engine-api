package dev.bpmcrafters.processengineapi.adapter.c8.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.PullUserTaskDelivery
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import java.time.Duration
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

open class ScheduledUserTaskDeliveryBinding(
    private val pullUserTaskDelivery: PullUserTaskDelivery,
    private val c8AdapterProperties: C8AdapterProperties,
    @Qualifier("c8-task-scheduler")
    private val c8taskScheduler: TaskScheduler
) : SchedulingConfigurer {

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        taskRegistrar.setScheduler(c8taskScheduler)
        taskRegistrar.addFixedRateTask(
            {
                logger.trace { "PROCESS-ENGINE-C7-REMOTE-106: Delivering user tasks..." }
                pullUserTaskDelivery.refresh()
                logger.trace { "PROCESS-ENGINE-C7-REMOTE-107: Delivered user tasks." }
            },
            Duration.of(c8AdapterProperties.userTasks.scheduleDeliveryFixedRateInSeconds, ChronoUnit.SECONDS)
        )
    }
}
