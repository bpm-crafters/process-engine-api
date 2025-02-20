package dev.bpmcrafters.processengineapi.test

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.ExpectedScenarioState
import io.github.oshai.kotlinlogging.KotlinLogging
import io.toolisticon.testing.jgiven.JGivenKotlinStage
import io.toolisticon.testing.jgiven.step
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await

@JGivenKotlinStage
class BaseThenStage : Stage<BaseThenStage>() {

  @ExpectedScenarioState
  lateinit var instanceId: String

  @ExpectedScenarioState
  var userTaskId: String? = null

  @ExpectedScenarioState
  var externalTaskId: String? = null

  @ExpectedScenarioState
  lateinit var processTestHelper: ProcessTestHelper

  fun `we should have a running process`() = step {
    val process = processTestHelper.getProcessInformation(instanceId)
    assertThat(process).isNotNull()
  }

  fun `we should get notified about a new user task with pull strategy`() = step {
    processTestHelper.triggerPullingUserTaskDeliveryManually()

    await().untilAsserted { assertThat(userTaskId).isNotEmpty() }
  }

  fun `we should get notified about a new user task with subscribing strategy`() = step {
    await().untilAsserted { assertThat(userTaskId).isNotEmpty() }
  }

  fun `we should not get notified about a new user task with pull strategy`() = step {
    processTestHelper.triggerPullingUserTaskDeliveryManually()

    await().untilAsserted { assertThat(userTaskId).isNull() }
  }

  fun `we should not get notified about a new user task with subscribing strategy`() = step {
    await().untilAsserted { assertThat(userTaskId).isNull() }
  }

  fun `we should get notified about a new external task`() = step {
    processTestHelper.triggerExternalTaskDeliveryManually()

    await().untilAsserted { assertThat(externalTaskId).isNotEmpty() }
  }

  fun `we should not get notified about a new external task`() = step {
    processTestHelper.triggerExternalTaskDeliveryManually()

    await().untilAsserted { assertThat(externalTaskId).isNull() }
  }

}
