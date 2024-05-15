package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job

import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl
import org.camunda.bpm.engine.impl.task.TaskDefinition
import org.camunda.bpm.engine.impl.util.xml.Element

/**
 * Parse listener adding user and service task listeners to the parsed BPMN.
 */
class EmbeddedTaskDeliveryBpmnParseListener(
  private val jobCreatingUserTaskListener: TaskListener,
  private val jobCreatingServiceTaskListener: ExecutionListener
) : AbstractBpmnParseListener() {


  override fun parseUserTask(userTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
    val activityBehavior = activity.activityBehavior as UserTaskActivityBehavior
    val taskDefinition = activityBehavior.taskDefinition
    addExecutionCreateListeners(taskDefinition)
    addTaskCompleteListeners(taskDefinition)
    addTaskDeleteListeners(taskDefinition)
    addTaskTimeoutListeners(taskDefinition)
  }

  override fun parseSendTask(sendTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
    addExecutionCreateListeners(activity)
    addExecutionEndListeners(activity)
  }

  override fun parseIntermediateThrowEvent(intermediateEventElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
    addExecutionCreateListeners(activity)
    addExecutionEndListeners(activity)
  }

  override fun parseServiceTask(serviceTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
    addExecutionCreateListeners(activity)
    addExecutionEndListeners(activity)
  }

  private fun addExecutionCreateListeners(activity: ActivityImpl) {
    activity.addBuiltInListener(ExecutionListener.EVENTNAME_START, jobCreatingServiceTaskListener)
  }

  private fun addExecutionEndListeners(activity: ActivityImpl) {
    activity.addBuiltInListener(ExecutionListener.EVENTNAME_END, jobCreatingServiceTaskListener)
  }

  private fun addExecutionCreateListeners(taskDefinition: TaskDefinition) {
    taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_CREATE, jobCreatingUserTaskListener)
  }

  private fun addTaskCompleteListeners(taskDefinition: TaskDefinition) {
    taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_COMPLETE, jobCreatingUserTaskListener)
  }

  private fun addTaskDeleteListeners(taskDefinition: TaskDefinition) {
    taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_DELETE, jobCreatingUserTaskListener)
  }

  private fun addTaskTimeoutListeners(taskDefinition: TaskDefinition) {
    taskDefinition.addBuiltInTaskListener(TaskListener.EVENTNAME_TIMEOUT, jobCreatingUserTaskListener)
  }

}
