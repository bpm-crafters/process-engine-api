package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.event

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.impl.task.filterBySubscription
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.toTaskInformation
import dev.bpmcrafters.processengineapi.impl.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.impl.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.task.TaskType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateTask

private val logger = KotlinLogging.logger {}

class EmbeddedEventBasedUserTaskDelivery(
  private val subscriptionRepository: SubscriptionRepository
) : UserTaskDelivery {

  fun userTaskCreated(delegateTask: DelegateTask) {
    val subscriptions = subscriptionRepository.getTaskSubscriptions().filter { s -> s.taskType == TaskType.USER }

    subscriptions
      .firstOrNull { subscription -> subscription.matches(delegateTask) }
      ?.let { activeSubscription ->
        subscriptionRepository.activateSubscriptionForTask(delegateTask.id, activeSubscription)
        val variables = delegateTask.variables.filterBySubscription(activeSubscription)

        try {
          logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-013: delivering user task ${delegateTask.id}." }
          activeSubscription.action.accept(delegateTask.toTaskInformation(), variables)
          logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-014: successfully delivered user task ${delegateTask.id}." }
        } catch (e: Exception) {
          subscriptionRepository.deactivateSubscriptionForTask(taskId = delegateTask.id)
          logger.error { "PROCESS-ENGINE-C7-EMBEDDED-015: Error delivering user task ${delegateTask.id}: ${e.message}" }
        }
      }
  }

  fun userTaskDeleted(delegateTask: DelegateTask) {
    logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-016: deleting user task ${delegateTask.id}." }
    subscriptionRepository.getActiveSubscriptionForTask(delegateTask.id)?.termination?.accept(delegateTask.id)
    logger.debug { "PROCESS-ENGINE-C7-EMBEDDED-017: successfully deleted user task ${delegateTask.id}." }
  }


  private fun TaskSubscriptionHandle.matches(task: DelegateTask): Boolean =
    this.taskType == TaskType.USER && (
      this.taskDescriptionKey == null
        || this.taskDescriptionKey == task.taskDefinitionKey
        || this.taskDescriptionKey == task.id
      ) && this.restrictions.all {
      when (it.key) {
        CommonRestrictions.EXECUTION_ID -> it.value == task.executionId
        CommonRestrictions.TENANT_ID -> it.value == task.tenantId
        CommonRestrictions.ACTIVITY_ID -> it.value == task.taskDefinitionKey
        CommonRestrictions.PROCESS_INSTANCE_ID -> it.value == task.processInstanceId
        CommonRestrictions.PROCESS_DEFINITION_ID -> it.value == task.processDefinitionId
        // FIXME -> process definition key
        else -> false
      }
    }
}
