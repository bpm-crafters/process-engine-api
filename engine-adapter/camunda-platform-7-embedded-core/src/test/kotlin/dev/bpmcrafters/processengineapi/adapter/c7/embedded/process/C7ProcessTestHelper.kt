package dev.bpmcrafters.processengineapi.adapter.c7.embedded.process

import dev.bpmcrafters.processengineapi.process.ProcessInformation
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import org.camunda.bpm.engine.ProcessEngine

class C7ProcessTestHelper(private val processEngine: ProcessEngine) : ProcessTestHelper {

  override fun getProcessInformation(instanceId: String): ProcessInformation =
    processEngine.runtimeService
      .createProcessInstanceQuery()
      .processInstanceId(instanceId)
      .singleResult()
      .toProcessInformation()

}
