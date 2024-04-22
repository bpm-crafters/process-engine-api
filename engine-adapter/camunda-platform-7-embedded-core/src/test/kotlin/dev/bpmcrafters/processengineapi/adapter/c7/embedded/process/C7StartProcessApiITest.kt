package dev.bpmcrafters.processengineapi.adapter.c7.embedded.process

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.process.C7StartProcessApiITest.Companion.BPMN
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.process.StartProcessByDefinitionCmd
import dev.bpmcrafters.processengineapi.process.StartProcessByMessageCmd
import dev.bpmcrafters.processengineapi.process.StartProcessCommand
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.junit5.ProcessEngineExtension
import org.camunda.bpm.engine.test.mock.MockExpressionManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat as camundaAssertThat

@Deployment(resources = [BPMN])
class C7StartProcessApiITest {

  companion object {
    @RegisterExtension
    val camunda: ProcessEngineExtension = ProcessEngineExtension.builder().useProcessEngine(
      object : StandaloneInMemProcessEngineConfiguration() {
        init {
          history = ProcessEngineConfiguration.HISTORY_AUDIT
          databaseSchemaUpdate = ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE
          jobExecutorActivate = false
          expressionManager = MockExpressionManager()
        }
      }.buildProcessEngine()
    ).build()

    const val KEY = "simple-process"
    const val START_MESSAGE = "startMessage"
    const val BPMN = "bpmn/$KEY.bpmn"
  }

  private lateinit var startProcessApiImpl: StartProcessApi

  @BeforeEach
  fun setUp() {
    startProcessApiImpl = StartProcessApiImpl(camunda.runtimeService)
  }

  @Test
  fun `should start process by definition without payload`() {
    // GIVEN our start process command
    val command: StartProcessCommand = StartProcessByDefinitionCmd(
      definitionKey = KEY,
      payloadSupplier = { emptyMap() }
    )

    // WHEN we start a process
    val processInformation = startProcessApiImpl.startProcess(command)

    // THEN we should have a running process
    val processId = processInformation.get().instanceId
    val process = camunda.runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult()
    camundaAssertThat(process).isStarted;
  }

  @Test
  fun `should start process by definition with payload`() {
    // GIVEN our start process command
    val command: StartProcessCommand = StartProcessByDefinitionCmd(
      definitionKey = KEY,
      payloadSupplier = { mapOf("key" to "value") }
    )

    // WHEN we start a process
    val processInformation = startProcessApiImpl.startProcess(command)

    // THEN we should have a running process
    val processId = processInformation.get().instanceId
    val process = camunda.runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult()
    camundaAssertThat(process).isStarted
    assertThat(camunda.runtimeService.getVariables(processId)).containsEntry("key", "value")
  }

  @Test
  fun `should start process via message without payload`() {
    // GIVEN our start process command
    val command: StartProcessCommand = StartProcessByMessageCmd(
      messageName = START_MESSAGE,
      payloadSupplier = { emptyMap() }
    )

    // WHEN we start a process
    val processInformation = startProcessApiImpl.startProcess(command)

    // THEN we should have a running process
    val processId = processInformation.get().instanceId
    val process = camunda.runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult()
    camundaAssertThat(process).isStarted;
  }

  @Test
  fun `should start process via message with payload`() {
    // GIVEN our start process command
    val command: StartProcessCommand = StartProcessByMessageCmd(
      messageName = START_MESSAGE,
      payloadSupplier = { mapOf("key" to "value") }
    )

    // WHEN we start a process
    val processInformation = startProcessApiImpl.startProcess(command)

    // THEN we should have a running process
    val processId = processInformation.get().instanceId
    val process = camunda.runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult()
    camundaAssertThat(process).isStarted;
    assertThat(camunda.runtimeService.getVariables(processId)).containsEntry("key", "value")
  }
}
