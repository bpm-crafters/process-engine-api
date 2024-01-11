package dev.bpmcrafters.processengineapi.task

import dev.bpmcrafters.processengineapi.PayloadSupplier

open class CompleteTaskCmd(
  val taskId: String,
  val payload: () -> Map<String, Any>
) : PayloadSupplier {
  override fun payload(): () -> Map<String, Any> = payload
}
