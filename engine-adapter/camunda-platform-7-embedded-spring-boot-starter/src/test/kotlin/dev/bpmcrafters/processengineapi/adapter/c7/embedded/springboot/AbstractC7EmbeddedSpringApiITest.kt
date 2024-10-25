package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot

import dev.bpmcrafters.processengineapi.test.JGivenBaseIntegrationTest
import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import org.camunda.bpm.engine.RepositoryService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
  classes = [C7EmbeddedTestApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("itest")
@DirtiesContext
abstract class AbstractC7EmbeddedSpringApiITest(override val processTestHelper: ProcessTestHelper) : JGivenBaseIntegrationTest(processTestHelper) {

  companion object {
    const val KEY = "simple-process"
    const val START_MESSAGE = "startMessage"
    const val BPMN = "bpmn/$KEY.bpmn"

    const val USER_TASK = "user-perform-task"
    const val EXTERNAL_TASK = "execute-action-external"

    /** val processEngine: ProcessEngine = object : StandaloneInMemProcessEngineConfiguration() {
    init {
    history = ProcessEngineConfiguration.HISTORY_AUDIT
    databaseSchemaUpdate = ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE
    jobExecutorActivate = false
    expressionManager = MockExpressionManager()
    }
    }.buildProcessEngine() **/
  }

  @Autowired
  lateinit var repositoryService: RepositoryService

  @BeforeEach
  fun setUp() {
    repositoryService.createDeployment()
      .name("Simple Process")
      .addClasspathResource(BPMN)
      .deploy()
  }

  @AfterEach
  fun tearDown() {
    processTestHelper.clearAllSubscriptions()
  }

}
