package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.remote.process.toProcessInformation
import dev.bpmcrafters.processengineapi.process.ProcessInformation
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.RuntimeService

class C7RemoteProcessTestHelper(private val runtimeService: RuntimeService) : ProcessTestHelper {

  override fun getProcessInformation(instanceId: String): ProcessInformation =
    runtimeService
      .createProcessInstanceQuery()
      .processInstanceId(instanceId)
      .singleResult()
      .toProcessInformation()

}
