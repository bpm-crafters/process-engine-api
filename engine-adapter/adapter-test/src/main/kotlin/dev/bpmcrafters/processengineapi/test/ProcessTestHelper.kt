package dev.bpmcrafters.processengineapi.test

import dev.bpmcrafters.processengineapi.process.ProcessInformation

interface ProcessTestHelper {

  fun getProcessInformation(instanceId: String): ProcessInformation

}
