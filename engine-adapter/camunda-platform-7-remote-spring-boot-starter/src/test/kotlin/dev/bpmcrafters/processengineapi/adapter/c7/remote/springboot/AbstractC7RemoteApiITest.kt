package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import dev.bpmcrafters.processengineapi.test.JGivenSpringBaseIntegrationTest
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import org.camunda.bpm.engine.RepositoryService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(
  classes = [C7RemoteTestApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("itest")
@DirtiesContext
@Testcontainers
abstract class AbstractC7RemoteApiITest(processTestHelperImpl: ProcessTestHelper) : JGivenSpringBaseIntegrationTest(processTestHelperImpl) {

  companion object {
    const val KEY = "simple-process"
    const val START_MESSAGE = "startMessage"
    const val BPMN = "bpmn/$KEY.bpmn"

    const val USER_TASK = "user-perform-task"
    const val EXTERNAL_TASK = "execute-action-external"
  }

  @Container
  val camundaContainer = Camunda7RunTestContainer("run-7.21.0")

  @Autowired
  lateinit var repositoryService: RepositoryService

  @BeforeEach
  fun setUp() {
    repositoryService.createDeployment()
      .name("Simple Process")
      .addClasspathResource("bpmn/simple-process.bpmn")
      .deploy()
  }

  @AfterEach
  fun tearDown() {
    processTestHelper.clearAllSubscriptions()
  }
}
