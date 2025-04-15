package dev.bpmcrafters.processengineapi.task

import java.util.function.Consumer

/**
 * Handler that is invoked if the task is deleted after the task has been assigned to the task handler.
 * @since 0.0.1
 */
@JvmDefaultWithoutCompatibility
fun interface TaskTerminationHandler : Consumer<TaskInformation> {

  fun process(taskInformation: TaskInformation) : TaskInformation {
    accept(taskInformation)
    return taskInformation
  }
}
