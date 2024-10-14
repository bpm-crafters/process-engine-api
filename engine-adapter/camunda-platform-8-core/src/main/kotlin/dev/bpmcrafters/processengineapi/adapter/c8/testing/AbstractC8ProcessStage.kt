package dev.bpmcrafters.processengineapi.adapter.c8.testing

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.As
import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.annotation.Quoted
import com.tngtech.jgiven.annotation.ScenarioState
import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.adapter.c8.deploy.DeploymentApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.process.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.C8ZeebeExternalServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.C8ZeebeUserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c8.task.completion.LinearMemoryFailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.c8.task.delivery.SubscribingRefreshingUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c8.task.subscription.C8TaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.UserTaskSupport
import dev.bpmcrafters.processengineapi.deploy.DeployBundleCommand
import dev.bpmcrafters.processengineapi.deploy.DeploymentApi
import dev.bpmcrafters.processengineapi.deploy.NamedResource.Companion.fromClasspath
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.*
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.process.test.api.ZeebeTestEngine
import io.camunda.zeebe.process.test.filters.RecordStream
import io.camunda.zeebe.protocol.record.Record
import io.camunda.zeebe.protocol.record.RecordType
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent
import io.camunda.zeebe.protocol.record.value.BpmnElementType
import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue
import org.assertj.core.api.Assertions
import org.awaitility.Awaitility
import java.time.Duration
import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * Abstract stage for subtyping of JGiven stages used in process tests for Camunda 8 Engine.
 * @param SUBTYPE type of your stage, subclassing this one.
 */
abstract class AbstractC8ProcessStage<SUBTYPE : AbstractC8ProcessStage<SUBTYPE>> : Stage<SUBTYPE>() {

  @ProvidedScenarioState
  protected lateinit var client: ZeebeClient

  @ProvidedScenarioState
  protected lateinit var engine: ZeebeTestEngine

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  protected lateinit var workerId: String

  @ProvidedScenarioState
  protected lateinit var userTaskSupport: UserTaskSupport

  @ProvidedScenarioState
  protected lateinit var startProcessApi: StartProcessApi

  @ProvidedScenarioState
  protected lateinit var userTaskCompletionApi: UserTaskCompletionApi

  @ProvidedScenarioState
  protected lateinit var serviceTaskCompletionApi: ServiceTaskCompletionApi

  @ProvidedScenarioState
  protected lateinit var taskSubscriptionApi: TaskSubscriptionApi

  @ProvidedScenarioState
  protected lateinit var deploymentApi: DeploymentApi

  @ProvidedScenarioState
  protected lateinit var taskInformation: TaskInformation

  @ProvidedScenarioState
  protected lateinit var activatedJob: ActivatedJob

  @ProvidedScenarioState
  private lateinit var subscribingRefreshingUserTaskDelivery: SubscribingRefreshingUserTaskDelivery


  /**
   * Initializes the engine. should be called from a method of your test marked with `@BeforeEach`
   * to make sure, the engine is initialized early.
   * @param client zeebe client.
   * @param engine zeebe test engine.
   * @param restrictions list of restrictions used in task subscription API. Usually, contains a restriction to the process definition key. Please use `CommonRestrictions` builder.
   */
  open fun initializeEngine(
    client: ZeebeClient,
    engine: ZeebeTestEngine,
    restrictions: Map<String, String>
  ): SUBTYPE {
    this.client = client
    this.engine = engine
    this.workerId = self().javaClass.simpleName

    val subscriptionRepository = InMemSubscriptionRepository()

    startProcessApi = StartProcessApiImpl(this.client)
    deploymentApi = DeploymentApiImpl(this.client)
    userTaskCompletionApi = C8ZeebeUserTaskCompletionApiImpl(this.client, subscriptionRepository)
    serviceTaskCompletionApi = C8ZeebeExternalServiceTaskCompletionApiImpl(
      this.client,
      subscriptionRepository,
      LinearMemoryFailureRetrySupplier(3, 3L)
    )
    subscribingRefreshingUserTaskDelivery = SubscribingRefreshingUserTaskDelivery(
      this.client,
      subscriptionRepository,
      workerId,
      3000
    )

    taskSubscriptionApi = C8TaskSubscriptionApiImpl(
      subscriptionRepository,
      this.subscribingRefreshingUserTaskDelivery
    )

    this.userTaskSupport = UserTaskSupport()
    userTaskSupport.subscribe(
      taskSubscriptionApi,
      restrictions,
      null,
      null
    )

    initialize()

    // activate delivery
    subscribingRefreshingUserTaskDelivery.subscribe()
    return self()
  }

  /**
   * Called after Engine and API is initialized.
   */
  open fun initialize() {
  }

  @As("external task of type \$jobType exists")
  open fun external_task_exists(
    @Quoted jobType: String,
    activityId: String?
  ): SUBTYPE {
    val jobs = client
      .newActivateJobsCommand()
      .jobType(jobType)
      .maxJobsToActivate(Int.MAX_VALUE)
      .workerName(workerId)
      .send()
      .join()
      .jobs

    // Should be only one
    Assertions.assertThat(jobs).describedAs("Job for user task %s does not exist", activityId).isNotEmpty()
    Assertions.assertThat(jobs).describedAs("Only one active job supported, but %d were active", jobs.size)
      .hasSize(1)

    this.activatedJob = jobs[0]

    // Make sure it is the right one
    if (activityId != null) {
      Assertions.assertThat(activatedJob.elementId).isEqualTo(activityId)
    }
    return self()
  }

  @As("external task of type \$jobType is completed")
  open fun external_task_is_completed(
    @Quoted jobType: String,
    payload: Map<String, Any> = mapOf()
  ): SUBTYPE {
    Objects.requireNonNull(
      this.activatedJob,
      "No active external service task found, consider to assert using external_task_exists"
    )
    Assertions.assertThat(activatedJob.type)
      .describedAs("Expected the active job to be a type of %s, but it was %s", jobType, activatedJob.type)
      .isEqualTo(jobType)
    serviceTaskCompletionApi
      .completeTask(
        CompleteTaskCmd("" + activatedJob.key) { payload }
      ).get()
    return self()
  }

  open fun process_has_passed(activityId: String?): SUBTYPE {
    Assertions.assertThat(allProcessEngineEvents.map { record -> record.value.elementId })
      .contains(activityId)
    return self()
  }


  open fun process_is_deployed(bpmn: String): SUBTYPE {
    deploymentApi.deploy(
      DeployBundleCommand(
        listOf(fromClasspath(bpmn)),
        null
      )
    ).get()
    return self()
  }

  open fun process_is_finished(): SUBTYPE {
    Assertions.assertThat(
      allProcessEngineEvents
        .filter { record -> record.value.bpmnElementType == BpmnElementType.PROCESS }
        .findFirst()
    ).isNotEmpty()
    return self()
  }

  open fun process_waits_in(taskDescriptionKey: String): SUBTYPE {
    // try to get the task
    Awaitility.await().untilAsserted {
      val taskIdOption = findTaskByActivityId(taskDescriptionKey)
      Assertions.assertThat(taskIdOption).describedAs("Process is not waiting in user task $taskDescriptionKey", taskDescriptionKey).isNotEmpty()
      taskIdOption.ifPresent { taskId -> this.taskInformation = userTaskSupport.getTaskInformation(taskId) }
    }
    return self()
  }


  open fun task_is_assigned_to_user(assignee: String): SUBTYPE {
    val taskAssignee = Objects.requireNonNull(
      taskInformation,
      "No active user task found, consider to assert using process_waits_in"
    ).meta["assignee"]
    Assertions.assertThat(taskAssignee).isNotNull()
    Assertions.assertThat(taskAssignee).isEqualTo(assignee)
    return self()
  }


  open fun timer_passes(durationInSeconds: Long): SUBTYPE {
    engine.waitForIdleState(Duration.ofSeconds(durationInSeconds))
    Thread.sleep(durationInSeconds * 1000)
    subscribingRefreshingUserTaskDelivery.refresh()
    return self()
  }

  open fun task(): TaskInformation {
    return Objects.requireNonNull(
      taskInformation,
      "No activated job found, consider to assert using process_waits_in"
    )
  }


  private val allProcessEngineEvents: Stream<Record<ProcessInstanceRecordValue>>
    get() {
      val recordStream =
        RecordStream.of(
          engine.recordStreamSource
        )
      return StreamSupport
        .stream(
          recordStream.processInstanceRecords().spliterator(),
          false
        )
        .filter { record -> record.recordType == RecordType.EVENT && record.intent == ProcessInstanceIntent.ELEMENT_COMPLETED }
    }

  private fun findTaskByActivityId(taskDescriptionKey: String): Optional<String> {
    return Optional.ofNullable(
      userTaskSupport.getAllTasks()
        .find { taskInformation -> taskInformation.meta[CommonRestrictions.TASK_DEFINITION_KEY] == taskDescriptionKey }?.taskId
    )
  }

}
