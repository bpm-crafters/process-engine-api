package dev.bpmcrafters.processengineapi.adapter.c8

import dev.bpmcrafters.processengineapi.adapter.c8.process.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.C8ZeebeExternalServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.C8ZeebeUserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.LinearMemoryFailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.PullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingRefreshingUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.subscription.C8TaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.test.JGivenSpringBaseIntegrationTest
import io.camunda.tasklist.CamundaTaskListClient
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.DeploymentEvent
import io.camunda.zeebe.process.test.extension.testcontainer.ZeebeProcessTest
import io.toolisticon.testing.jgiven.GIVEN
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles


@SpringBootTest(
  classes = [C8TestApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ZeebeProcessTest
@ActiveProfiles("itest")
@DirtiesContext
abstract class AbstractC8ApiITest : JGivenSpringBaseIntegrationTest() {

  companion object {
    const val KEY = "simple-process"
    const val START_MESSAGE = "startMessage"
    const val BPMN = "bpmn/$KEY.bpmn"

    const val USER_TASK = "user-perform-task"
    const val EXTERNAL_TASK = "execute-action-external"
  }

  lateinit var client: ZeebeClient

  /*
   * We have no task list in test, so there is no need for the real client either
   */
  @MockBean
  lateinit var camundaTaskListClient: CamundaTaskListClient

  @BeforeEach
  fun setUp() {
    val workerId = this.javaClass.simpleName
    val subscriptionRepository = InMemSubscriptionRepository()
    val userTaskDelivery = SubscribingRefreshingUserTaskDelivery(
      this.client,
      subscriptionRepository,
      workerId,
      3000
    )

    this.processTestHelper = C8ProcessTestHelper(

      startProcessApi = StartProcessApiImpl(zeebeClient = client),
      userTaskCompletionApi = C8ZeebeUserTaskCompletionApiImpl(this.client, subscriptionRepository),
      serviceTaskCompletionApi = C8ZeebeExternalServiceTaskCompletionApiImpl(
        this.client,
        subscriptionRepository,
        LinearMemoryFailureRetrySupplier(3, 3L)
      ),
      taskSubscriptionApi = C8TaskSubscriptionApiImpl(subscriptionRepository, userTaskDelivery),
      subscribingServiceTaskDelivery = SubscribingServiceTaskDelivery(
        client, subscriptionRepository, workerId
      ),
      pullUserTaskDelivery = PullUserTaskDelivery(taskListClient = camundaTaskListClient, subscriptionRepository = subscriptionRepository),
      subscribingUserTaskDelivery = userTaskDelivery,
      subscriptionRepository = subscriptionRepository
    )

    val event: DeploymentEvent = client.newDeployResourceCommand()
      .addResourceFromClasspath(BPMN)
      .send()
      .join()
    assertThat(event).isNotNull


    GIVEN
      .`process helper`(processTestHelper)
  }

  @AfterEach
  fun tearDown() {
    processTestHelper.clearAllSubscriptions()
  }
}
