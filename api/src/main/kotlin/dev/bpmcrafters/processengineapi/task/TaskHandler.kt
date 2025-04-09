package dev.bpmcrafters.processengineapi.task

import java.util.function.BiConsumer

/**
 * Task handler receiving task information and payload.
 * @since 0.0.1
 */
@JvmDefaultWithoutCompatibility
fun interface TaskHandler : BiConsumer<TaskInformation, Map<String, Any>> {

  fun process(parameters: Pair<TaskInformation, Map<String, Any>>): Pair<TaskInformation, Map<String, Any>> {
    accept(parameters.first, parameters.second)
    return parameters
  }
}
