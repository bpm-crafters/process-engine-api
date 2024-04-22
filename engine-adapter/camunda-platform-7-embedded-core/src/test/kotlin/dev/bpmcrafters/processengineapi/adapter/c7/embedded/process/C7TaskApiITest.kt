package dev.bpmcrafters.processengineapi.adapter.c7.embedded.process

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.process.C7TaskApiITest.Companion.BPMN
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.subscription.C7TaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.process.StartProcessByDefinitionCmd
import dev.bpmcrafters.processengineapi.task.*
import io.mockk.mockk
import io.mockk.verify
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.junit5.ProcessEngineExtension
import org.camunda.bpm.engine.test.mock.MockExpressionManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@Deployment(resources = [BPMN])
class C7TaskApiITest {

  companion object {
    @RegisterExtension
    val camunda: ProcessEngineExtension = ProcessEngineExtension.builder().useProcessEngine(
      object : StandaloneInMemProcessEngineConfiguration() {
        init {
          history = HISTORY_AUDIT
          databaseSchemaUpdate = DB_SCHEMA_UPDATE_TRUE
          jobExecutorActivate = false
          expressionManager = MockExpressionManager()
        }
      }.buildProcessEngine()
    ).build()

    const val KEY = "simple-process"
    const val START_MESSAGE = "startMessage"
    const val BPMN = "bpmn/$KEY.bpmn"
  }

  private val subscriptionRepository = InMemSubscriptionRepository()
  private val taskSubscriptionApi: TaskSubscriptionApi = C7TaskSubscriptionApiImpl(subscriptionRepository)

  private val actionHandler = mockk<TaskHandler>()
  private val terminationHandler = mockk<TaskTerminationHandler>()

  private lateinit var startProcessApiImpl: StartProcessApi
  private lateinit var embeddedPullUserTaskDelivery: EmbeddedPullUserTaskDelivery

  @BeforeEach
  fun setUp() {
    startProcessApiImpl = StartProcessApiImpl(camunda.runtimeService)
    embeddedPullUserTaskDelivery = EmbeddedPullUserTaskDelivery(camunda.taskService, subscriptionRepository)
  }

  @Test
  fun `should get subscribed user task with pull`() {
    // GIVEN a task subscription
    taskSubscriptionApi.subscribeForTask(
      SubscribeForTaskCmd(
        restrictions = emptyMap(),
        taskType = TaskType.USER,
        taskDescriptionKey = "user-perform-task",
        action = actionHandler,
        termination = terminationHandler // nothing to do
      )
    )

    // WHEN we start a process
    startProcessApiImpl.startProcess(
      StartProcessByDefinitionCmd(
        definitionKey = KEY,
        payloadSupplier = { emptyMap() }
      )
    ).get()

    // AND trigger the pull delivery
    embeddedPullUserTaskDelivery.deliverAll()

    // THEN action handler should be called
    verify { actionHandler.accept(any(), any()) }

    // AND termination handler should NOT be called
    verify(exactly = 0) { terminationHandler.accept(any()) }

  }

}
