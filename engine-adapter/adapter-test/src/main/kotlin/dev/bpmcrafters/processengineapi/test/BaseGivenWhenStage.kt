package dev.bpmcrafters.processengineapi.test

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.ExpectedScenarioState
import com.tngtech.jgiven.annotation.ScenarioState
import dev.bpmcrafters.processengineapi.PayloadSupplier
import dev.bpmcrafters.processengineapi.process.*
import io.toolisticon.testing.jgiven.JGivenKotlinStage
import io.toolisticon.testing.jgiven.step

@JGivenKotlinStage
class BaseGivenWhenStage : Stage<BaseGivenWhenStage>() {

  @ScenarioState
  lateinit var startProcessApi: StartProcessApi

  @ExpectedScenarioState
  lateinit var instanceId: String

  fun `start process by definition`(definitionKey: String) = step {
    instanceId = startProcessApi.startProcess(
      StartProcessByDefinitionCmd(
        definitionKey = definitionKey,
        payloadSupplier = { emptyMap() }
      )
    ).get().instanceId
  }

  fun `start process by definition with payload`(definitionKey: String, singlePayload: Pair<String, Any>) = step {
    instanceId = startProcessApi.startProcess(
      StartProcessByDefinitionCmd(
        definitionKey = definitionKey,
        payloadSupplier = { mapOf(singlePayload) }
      )
    ).get().instanceId
  }

  fun `start process by message`(messageName: String) = step {
    instanceId = startProcessApi.startProcess(
      StartProcessByMessageCmd(
        messageName = messageName,
        payloadSupplier = { emptyMap() }
      )
    ).get().instanceId
  }

  fun `start process by message with payload`(messageName: String, singlePayload: Pair<String, Any>) = step {
    instanceId = startProcessApi.startProcess(
      StartProcessByMessageCmd(
        messageName = messageName,
        payloadSupplier = { mapOf(singlePayload) }
      )
    ).get().instanceId
  }
}
