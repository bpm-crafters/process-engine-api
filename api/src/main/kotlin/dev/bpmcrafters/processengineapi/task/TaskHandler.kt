package dev.bpmcrafters.processengineapi.task

import java.util.function.BiConsumer

/**
 * Task handler receiving task information and payload.
 * @since 0.0.1
 */
fun interface TaskHandler : BiConsumer<TaskInformation, Map<String, Any>>
