package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_CREATE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_DELETE
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.OPERATION_MODIFY
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration.Companion.TYPE_USER
import mu.KLogging
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl

/**
 * Task listener writing a job to notify delivery.
 */
open class JobCreatingUserTaskListener(
  private val processEngineConfigurationImpl: ProcessEngineConfigurationImpl
) : TaskListener {

  companion object : KLogging()

  override fun notify(delegateTask: DelegateTask) {
    when (delegateTask.eventName) {
      TaskListener.EVENTNAME_CREATE -> processEngineConfigurationImpl.createJob(
              EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration(
                      id = delegateTask.id,
                      type = TYPE_USER,
                      operation = OPERATION_CREATE
              )
      )

      TaskListener.EVENTNAME_ASSIGNMENT, TaskListener.EVENTNAME_UPDATE -> processEngineConfigurationImpl.createJob(
              EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration(
                      id = delegateTask.id,
                      type = TYPE_USER,
                      operation = OPERATION_MODIFY
              )
      )

      TaskListener.EVENTNAME_DELETE, TaskListener.EVENTNAME_TIMEOUT -> processEngineConfigurationImpl.createJob(
              EmbeddedTaskDeliveryJobHandler.EmbeddedTaskDeliveryJobHandlerConfiguration(
                      id = delegateTask.id,
                      type = TYPE_USER,
                      operation = OPERATION_DELETE
              )
      )
    }
  }
}
