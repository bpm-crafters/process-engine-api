package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_CREATE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_DELETE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.TYPE_SERVICE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.TYPE_USER
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery.Companion.logger
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.toTaskInformation
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.task.TaskType
import mu.KLogging
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration
import org.camunda.bpm.engine.impl.persistence.entity.*
import java.time.Instant
import java.util.*

/**
 * Job handler delivering task creating, modification and deletion.
 */
class EmbeddedTaskDeliveryJobHandler(
  private val subscriptionRepository: SubscriptionRepository,
  private val workerId: String,
  private val lockTimeInSeconds: Long
) : JobHandler<EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration> {

  companion object: KLogging() {
    const val TYPE = "dev.bpm-crafters.processengineapi.EmbeddedTaskDeliveryJobHandler"
  }

  override fun execute(
    configuration: EmbeddedTaskDeliveryJobHandlerConfiguration,
    execution: ExecutionEntity?,
    commandContext: CommandContext,
    tenantId: String?
  ) {

    logger.info { "Executing c7embedded task via JOB: $configuration" }

    val subscriptions = subscriptionRepository.getTaskSubscriptions()

    when (configuration.operation) {
      OPERATION_CREATE ->
        when (configuration.type) {
          TYPE_USER -> {
            val userTask = commandContext.taskManager.findTaskById(configuration.id)
            subscriptions
              .firstOrNull { subscription -> subscription.matches(userTask) }
              ?.let { activeSubscription ->
                subscriptionRepository.activateSubscriptionForTask(userTask.id, activeSubscription)
                val variables = if (activeSubscription.payloadDescription == null) {
                  userTask.variables
                } else {
                  if (activeSubscription.payloadDescription!!.isEmpty()) {
                    mapOf()
                  } else {
                    userTask.variables.filterKeys { key -> activeSubscription.payloadDescription!!.contains(key) }
                  }
                }
                try {
                  activeSubscription.action.accept(userTask.toTaskInformation(), variables)
                } catch (e: Exception) {
                  logger.error { "[PROCESS-ENGINE-C7-EMBEDDED]: Error delivering task ${userTask.id}: ${e.message}" }
                  subscriptionRepository.deactivateSubscriptionForTask(taskId = userTask.id)
                }
              }
          }

          TYPE_SERVICE -> {
            val tasks = commandContext.externalTaskManager.findExternalTasksByExecutionId(configuration.id)
            if (tasks != null) {
              val task = tasks.first() // FIXME? can it really happen?
              subscriptions
                .firstOrNull { subscription -> subscription.matches(task) }
                ?.let { activeSubscription ->
                  task.lock(workerId, lockTimeInSeconds) // lock external task
                  // FIXME -> check if already active for other subscription and notify it (delete)
                  subscriptionRepository.activateSubscriptionForTask(task.id, activeSubscription)
                  val variables = if (activeSubscription.payloadDescription == null) {
                    task.execution.variables
                  } else {
                    if (activeSubscription.payloadDescription!!.isEmpty()) {
                      mapOf()
                    } else {
                      task.execution.variables.filterKeys { key -> activeSubscription.payloadDescription!!.contains(key) }
                    }
                  }
                  try {
                    activeSubscription.action.accept(task.toTaskInformation(), variables)
                  } catch (e: Exception) {
                    logger.error { "[PROCESS-ENGINE-C7-EMBEDDED]: Error delivering task ${task.id}: ${e.message}" }
                    subscriptionRepository.deactivateSubscriptionForTask(taskId = task.id)
                  }
                }
            }
          }
        }

      OPERATION_DELETE -> subscriptionRepository.getActiveSubscriptionForTask(configuration.id)?.apply {
        termination.accept(configuration.id)
      }
    }
  }

  override fun onDelete(configuration: EmbeddedTaskDeliveryJobHandlerConfiguration, jobEntity: JobEntity) {
    // no op?
  }

  override fun newConfiguration(canonicalString: String): EmbeddedTaskDeliveryJobHandlerConfiguration =
    EmbeddedTaskDeliveryJobHandlerConfiguration.new(canonicalString)

  override fun getType(): String = TYPE

  private fun TaskSubscriptionHandle.matches(taskEntity: TaskEntity): Boolean =
    this.taskType == TaskType.USER && (this.taskDescriptionKey == null || this.taskDescriptionKey == taskEntity.taskDefinitionKey || this.taskDescriptionKey == taskEntity.id)

  private fun TaskSubscriptionHandle.matches(taskEntity: ExternalTaskEntity): Boolean =
    this.taskType == TaskType.EXTERNAL && (this.taskDescriptionKey == null || this.taskDescriptionKey == taskEntity.topicName)

  data class EmbeddedTaskDeliveryJobHandlerConfiguration(
    val id: String,
    val type: String,
    val operation: String
  ) : JobHandlerConfiguration {

    companion object {
      const val SEPARATOR = "#"

      const val OPERATION_CREATE = "create"
      const val OPERATION_DELETE = "delete"

      const val TYPE_USER = "user"
      const val TYPE_SERVICE = "service"

      @JvmStatic
      fun new(canonicalString: String) = EmbeddedTaskDeliveryJobHandlerConfiguration(
        id = canonicalString.split(SEPARATOR)[0],
        type = canonicalString.split(SEPARATOR)[1],
        operation = canonicalString.split(SEPARATOR)[2]
      )
    }

    override fun toCanonicalString(): String = "${id}$SEPARATOR${type}$SEPARATOR${operation}"
  }
}

fun ProcessEngineConfigurationImpl.createJob(configuration: EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration) {
  this.commandExecutorTxRequired.execute { context ->

    val job = MessageEntity().apply {
      jobHandlerConfigurationRaw = configuration.toCanonicalString()
      jobHandlerType = EmbeddedTaskDeliveryJobHandler.TYPE
      duedate = Date.from(Instant.now())
      // we don't want to retry the delivery for the moment.
      setRetriesFromPersistence(1) // FIXME -> move to properties?
    }
    // send / store job.
    context.jobManager.send(job)
  }
}

