package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion.ExternalTaskCompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion.UserTaskCompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_CREATE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_DELETE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_MODIFY
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.TYPE_SERVICE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.TYPE_USER
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.toTaskInformation
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
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
  private val lockTimeInSecconds: Long
) : JobHandler<EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration> {

  companion object {
    const val TYPE = "dev.bpm-crafters.processengineapi.EmbeddedTaskDeliveryJobHandler"
  }

  override fun execute(
          configuration: EmbeddedTaskDeliveryJobHandlerConfiguration,
          execution: ExecutionEntity?,
          commandContext: CommandContext,
          tenantId: String?
  ) {
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
                val variables = if (activeSubscription.payloadDescription.isEmpty()) {
                  userTask.variables
                } else {
                  userTask.variables.filterKeys { key -> activeSubscription.payloadDescription.contains(key) }
                }
                activeSubscription.action.accept(userTask.toTaskInformation(), variables)
              }
          }

          TYPE_SERVICE -> {
            val tasks = commandContext.externalTaskManager.findExternalTasksByExecutionId(configuration.id)
            if (tasks != null) {
              val task = tasks.first() // FIXME? can it really happen?
              subscriptions
                .firstOrNull { subscription -> subscription.matches(task) }
                ?.let { activeSubscription ->
                  task.lock(workerId, lockTimeInSecconds) // lock external task
                  subscriptionRepository.activateSubscriptionForTask(task.id, activeSubscription)
                  val variables = if (activeSubscription.payloadDescription.isEmpty()) {
                    task.execution.variables
                  } else {
                    task.execution.variables.filterKeys { key -> activeSubscription.payloadDescription.contains(key) }
                  }
                  activeSubscription.action.accept(task.toTaskInformation(), variables)
                }
            }
          }
        }

      OPERATION_MODIFY -> {
        subscriptionRepository.getActiveSubscriptionForTask(configuration.id)?.apply {
          val (taskInformation, taskVariables) = when (configuration.type) {
            TYPE_USER -> {
              val userTask = commandContext.taskManager.findTaskById(configuration.id)
              val variables = if (this.payloadDescription.isEmpty()) {
                userTask.variables
              } else {
                userTask.variables.filterKeys { key -> this.payloadDescription.contains(key) }
              }
              userTask.toTaskInformation() to variables
            }

            TYPE_SERVICE -> {
              val tasks = commandContext.externalTaskManager.findExternalTasksByExecutionId(configuration.id)
              if (tasks != null) {
                val task = tasks.first() // FIXME? can it really happen?
                val variables = if (this.payloadDescription.isEmpty()) {
                  task.execution.variables
                } else {
                  task.execution.variables.filterKeys { key -> this.payloadDescription.contains(key) }
                }
                task.toTaskInformation() to variables
              } else {
                null to null
              }
            }

            else -> null to null
          }
          // if task is found and variables are loaded, notify about modification
          if (taskInformation != null && taskVariables != null) {
            this.modification.modified(taskInformation, taskVariables)
          }
        }
      }

      OPERATION_DELETE -> subscriptionRepository.getActiveSubscriptionForTask(configuration.id)?.apply {
        modification.terminated(configuration.id)
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
    UserTaskCompletionStrategy.supports(this.restrictions)
      && (this.taskDescriptionKey == null || this.taskDescriptionKey == taskEntity.taskDefinitionKey || this.taskDescriptionKey == taskEntity.id)

  private fun TaskSubscriptionHandle.matches(taskEntity: ExternalTaskEntity): Boolean =
    ExternalTaskCompletionStrategy.supports(this.restrictions) &&
      (this.taskDescriptionKey == null || this.taskDescriptionKey == taskEntity.topicName)


  data class EmbeddedTaskDeliveryJobHandlerConfiguration(
    val id: String,
    val type: String,
    val operation: String
  ) : JobHandlerConfiguration {

    companion object {
      const val SEPARATOR = "#"

      const val OPERATION_CREATE = "create"
      const val OPERATION_DELETE = "delete"
      const val OPERATION_MODIFY = "modify"

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

