package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_CREATE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_DELETE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.TYPE_USER
import mu.KLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.TaskListener

/**
 * Task listener writing a job to notify delivery.
 */
open class JobCreatingUserTaskListener : TaskListener {

  companion object : KLogging()

  override fun notify(delegateTask: DelegateTask) {
    when (delegateTask.eventName) {
      TaskListener.EVENTNAME_CREATE -> createJob(
        EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration(
          executionId = delegateTask.id,
          type = TYPE_USER,
          operation = OPERATION_CREATE,
          processDefinitionId = delegateTask.processDefinitionId,
          processInstanceId = delegateTask.processInstanceId
        )
      )

      TaskListener.EVENTNAME_DELETE, TaskListener.EVENTNAME_TIMEOUT, TaskListener.EVENTNAME_COMPLETE -> createJob(
        EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration(
          executionId = delegateTask.id,
          type = TYPE_USER,
          operation = OPERATION_DELETE,
          processDefinitionId = delegateTask.processDefinitionId,
          processInstanceId = delegateTask.processInstanceId
        )
      )
    }
  }
}
