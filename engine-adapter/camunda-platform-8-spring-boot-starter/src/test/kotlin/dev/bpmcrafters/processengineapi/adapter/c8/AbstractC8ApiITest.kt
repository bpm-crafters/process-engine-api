package dev.bpmcrafters.processengineapi.adapter.c8

import dev.bpmcrafters.processengineapi.test.JGivenSpringBaseIntegrationTest
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import io.camunda.tasklist.CamundaTaskListClient
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.DeploymentEvent
import io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat
import io.camunda.zeebe.spring.test.ZeebeSpringTest
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers


@SpringBootTest(
  classes = [C8TestApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ZeebeSpringTest
@ActiveProfiles("itest")
@DirtiesContext
@Testcontainers
abstract class AbstractC8ApiITest(processTestHelperImpl: ProcessTestHelper) : JGivenSpringBaseIntegrationTest(processTestHelperImpl) {

  companion object {
    const val KEY = "simple-process"
    const val START_MESSAGE = "startMessage"
    const val BPMN = "bpmn/$KEY.bpmn"

    const val USER_TASK = "user-perform-task"
    const val EXTERNAL_TASK = "execute-action-external"
  }

  @Autowired
  lateinit var zeebe: ZeebeClient

  /**
   * In tests we have no task list so also no need for the real client
   */
  @MockBean
  lateinit var camundaTaskListClient: CamundaTaskListClient

  @BeforeEach
  fun setUp() {
    val event: DeploymentEvent = zeebe.newDeployResourceCommand()
      .addResourceFromClasspath(BPMN)
      .send()
      .join()

    assertThat(event)
  }

  @AfterEach
  fun tearDown() {
    processTestHelper.clearAllSubscriptions()
  }
}
