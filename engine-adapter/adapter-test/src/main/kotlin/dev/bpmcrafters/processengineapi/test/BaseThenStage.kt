package dev.bpmcrafters.processengineapi.test

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.ExpectedScenarioState
import io.toolisticon.testing.jgiven.JGivenKotlinStage
import org.assertj.core.api.Assertions.assertThat

@JGivenKotlinStage
class BaseThenStage : Stage<BaseThenStage>() {

  @ExpectedScenarioState
  lateinit var instanceId: String

  @ExpectedScenarioState
  lateinit var processTestHelper: ProcessTestHelper

  fun `we should have a running process`() {
    val process = processTestHelper.getProcessInformation(instanceId)
    assertThat(process).isNotNull()
  }

}
