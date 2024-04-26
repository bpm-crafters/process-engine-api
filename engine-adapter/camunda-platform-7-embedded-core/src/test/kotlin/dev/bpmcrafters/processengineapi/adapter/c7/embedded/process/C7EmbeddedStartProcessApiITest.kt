package dev.bpmcrafters.processengineapi.adapter.c7.embedded.process

import com.tngtech.jgiven.annotation.ProvidedScenarioState
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.process.C7EmbeddedStartProcessApiITest.Companion.BPMN
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.test.JGivenBaseIntegrationTest
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.junit5.ProcessEngineExtension
import org.camunda.bpm.engine.test.mock.MockExpressionManager
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension


@Deployment(resources = [BPMN])
class C7EmbeddedStartProcessApiITest : JGivenBaseIntegrationTest() {

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

  @ProvidedScenarioState
  var startProcessApi: StartProcessApi = StartProcessApiImpl(camunda.runtimeService)

  @ProvidedScenarioState
  var processTestHelper: ProcessTestHelper = C7EmbeddedProcessTestHelper(camunda.processEngine)

  @Test
  fun `should start process by definition without payload`() {
    WHEN
      .`start process by definition`(KEY)

    THEN
      .`we should have a running process`()
  }

  @Test
  fun `should start process by definition with payload`() {
    WHEN
      .`start process by definition with payload`(KEY, "key" to "value")

    THEN
      .`we should have a running process`()
  }

  @Test
  fun `should start process via message without payload`() {
    WHEN
      .`start process by message`(START_MESSAGE)

    THEN
      .`we should have a running process`()
  }

  @Test
  fun `should start process via message with payload`() {
    WHEN
      .`start process by message with payload`(START_MESSAGE, "key" to "value")

    THEN
      .`we should have a running process`()
  }

}
