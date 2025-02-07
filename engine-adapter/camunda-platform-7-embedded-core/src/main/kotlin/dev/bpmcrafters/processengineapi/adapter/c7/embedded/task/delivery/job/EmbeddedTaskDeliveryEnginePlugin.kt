package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job

import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl

private val logger = KotlinLogging.logger {}

/**
 * Engine plugin registering a job handler and parse listeners to deliver new tasks via Camunda Job.
 */
open class EmbeddedTaskDeliveryEnginePlugin(
  private val jobHandler: EmbeddedTaskDeliveryJobHandler,
  private val deliverServiceTasks: Boolean,
  private val deliverUserTasks: Boolean,
) : AbstractProcessEnginePlugin() {

  override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl): Unit = with(processEngineConfiguration) {
    if (customJobHandlers == null) {
      customJobHandlers = mutableListOf()
    }
    customJobHandlers.add(jobHandler)

    logger.info { "PROCESS-ENGINE-C7-EMBEDDED-017: Registered custom job handler for task delivery." }

    if (customPostBPMNParseListeners == null) {
      customPostBPMNParseListeners = mutableListOf()
    }

    val parseListener = EmbeddedTaskDeliveryBpmnParseListener(
      jobCreatingServiceTaskListener =
      if (deliverServiceTasks) {
        logger.info { "PROCESS-ENGINE-C7-EMBEDDED-018: Registered parse listener for external service task delivery." }
        JobCreatingExternalServiceTaskListener()
      } else {
        ExecutionListener {}
      },
      jobCreatingUserTaskListener =
      if (deliverUserTasks) {
        logger.info { "PROCESS-ENGINE-C7-EMBEDDED-019: Registered parse listener for user task delivery." }
        JobCreatingUserTaskListener()
      } else {
        TaskListener {}
      }
    )
    customPostBPMNParseListeners.add(parseListener)
  }
}
