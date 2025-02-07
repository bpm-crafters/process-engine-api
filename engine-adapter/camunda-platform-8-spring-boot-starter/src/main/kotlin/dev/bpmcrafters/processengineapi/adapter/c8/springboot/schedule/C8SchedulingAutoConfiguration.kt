package dev.bpmcrafters.processengineapi.adapter.c8.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterAutoConfiguration
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterEnabledCondition
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.UserTaskDeliveryStrategy.SCHEDULED
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.C8AdapterProperties.UserTaskDeliveryStrategy.SUBSCRIPTION_REFRESHING
import dev.bpmcrafters.processengineapi.adapter.c8.springboot.ConditionalOnUserTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.PullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingRefreshingUserTaskDelivery
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

private val logger = KotlinLogging.logger {}

/**
 * Auto-configuration for scheduled delivery.
 */
@Configuration
@EnableScheduling
@EnableAsync
@AutoConfigureAfter(C8AdapterAutoConfiguration::class)
@Conditional(C8AdapterEnabledCondition::class)
class C8SchedulingAutoConfiguration {

  @PostConstruct
  fun report() {
    logger.debug { "PROCESS-ENGINE-C8-202: Scheduling configuration applied." }
  }

  @Bean("c8-task-scheduler")
  @Qualifier("c8-task-scheduler")
  @ConditionalOnMissingBean
  fun taskScheduler(): TaskScheduler {
    val threadPoolTaskScheduler = ThreadPoolTaskScheduler()
    threadPoolTaskScheduler.poolSize = 2 // we have two schedulers, one for user tasks one for service tasks
    threadPoolTaskScheduler.threadNamePrefix = "C8REMOTE-SCHEDULER-"
    return threadPoolTaskScheduler
  }

  @Bean("c8-user-task-delivery-scheduler")
  @ConditionalOnUserTaskDeliveryStrategy(strategy = SCHEDULED)
  fun scheduledUserTaskDeliveryBinding(
    c8AdapterProperties: C8AdapterProperties,
    @Qualifier("c8-task-scheduler")
    c8TaskScheduler: TaskScheduler,
    @Qualifier("c8-user-task-delivery")
    pullUserTaskDelivery: PullUserTaskDelivery
  ): ScheduledUserTaskDeliveryBinding {
    return ScheduledUserTaskDeliveryBinding(
      pullUserTaskDelivery = pullUserTaskDelivery,
      c8AdapterProperties = c8AdapterProperties,
      c8taskScheduler = c8TaskScheduler
    )
  }

  @Bean("c8-user-task-delivery-scheduler")
  @ConditionalOnUserTaskDeliveryStrategy(strategy = SUBSCRIPTION_REFRESHING)
  fun refreshingUserTaskDeliveryBinding(
    @Qualifier("c8-user-task-delivery")
    subscribingRefreshingUserTaskDelivery: SubscribingRefreshingUserTaskDelivery,
    c8AdapterProperties: C8AdapterProperties,
    @Qualifier("c8-task-scheduler")
    c8TaskScheduler: TaskScheduler
  ): RefreshingUserTaskDeliveryBinding {
    return RefreshingUserTaskDeliveryBinding(
      subscribingRefreshingUserTaskDelivery = subscribingRefreshingUserTaskDelivery,
      c8AdapterProperties = c8AdapterProperties,
      c8taskScheduler = c8TaskScheduler
    )
  }


}
