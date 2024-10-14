package dev.bpmcrafters.processengineapi.adapter.c7.embedded.testing

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.annotation.ScenarioState
import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.deploy.DeploymentApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.process.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion.C7ServiceTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion.C7UserTaskCompletionApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.completion.LinearMemoryFailureRetrySupplier
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.subscription.C7TaskSubscriptionApiImpl
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.UserTaskSupport
import dev.bpmcrafters.processengineapi.deploy.DeployBundleCommand
import dev.bpmcrafters.processengineapi.deploy.DeploymentApi
import dev.bpmcrafters.processengineapi.deploy.NamedResource.Companion.fromClasspath
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.*
import org.assertj.core.api.Assertions
import org.awaitility.Awaitility
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.variable.VariableMap
import java.util.*
import java.util.concurrent.Executors
import java.util.function.Supplier
import kotlin.collections.set

abstract class AbstractC7EmbeddedStage<SUBTYPE : AbstractC7EmbeddedStage<SUBTYPE>> : Stage<SUBTYPE>() {

  @ProvidedScenarioState
  private lateinit var restrictions: Map<String, String>

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
  protected lateinit var processEngineServices: ProcessEngineServices

  @ProvidedScenarioState
  lateinit var embeddedPullUserTaskDelivery: EmbeddedPullUserTaskDelivery

  @ProvidedScenarioState
  private lateinit var embeddedPullServiceTaskDelivery: EmbeddedPullServiceTaskDelivery

  @ProvidedScenarioState
  protected lateinit var processInstanceSupplier: Supplier<ProcessInstance>

  @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
  private lateinit var topicToExternalTaskId: MutableMap<String, String>

  @ProvidedScenarioState
  protected lateinit var taskInformation: TaskInformation


  open fun initializeEngine(
    processEngineServices: ProcessEngineServices, restrictions: Map<String, String>
  ): SUBTYPE {
    this.topicToExternalTaskId = HashMap()
    this.restrictions = restrictions
    this.processEngineServices = processEngineServices
    this.workerId = self().javaClass.simpleName

    val subscriptionRepository = InMemSubscriptionRepository()

    startProcessApi = StartProcessApiImpl(processEngineServices.runtimeService)
    deploymentApi = DeploymentApiImpl(processEngineServices.repositoryService)
    userTaskCompletionApi = C7UserTaskCompletionApiImpl(processEngineServices.taskService, subscriptionRepository)
    serviceTaskCompletionApi = C7ServiceTaskCompletionApiImpl(
      workerId, processEngineServices.externalTaskService, subscriptionRepository, LinearMemoryFailureRetrySupplier(3, 3L)
    )
    embeddedPullUserTaskDelivery = EmbeddedPullUserTaskDelivery(
      processEngineServices.taskService, processEngineServices.repositoryService, subscriptionRepository, Executors.newFixedThreadPool(1)
    )

    embeddedPullServiceTaskDelivery = EmbeddedPullServiceTaskDelivery(
      processEngineServices.externalTaskService, workerId, subscriptionRepository, Int.MAX_VALUE, 1000, 1000, 1, Executors.newFixedThreadPool(1)
    )

    taskSubscriptionApi = C7TaskSubscriptionApiImpl(
      subscriptionRepository
    )

    this.userTaskSupport = UserTaskSupport()
    userTaskSupport.subscribe(
      taskSubscriptionApi, restrictions, null, null
    )

    initialize()

    // activate delivery
    embeddedPullUserTaskDelivery.refresh()
    embeddedPullServiceTaskDelivery.refresh()
    return self()
  }


  /**
   * Called after Process Engine and API are initialized.
   */
  open fun initialize() {
  }

  open fun process_continues(elementId: String): SUBTYPE {
    Awaitility.await().untilAsserted {
      val job = BpmnAwareTests.job(elementId, processInstanceSupplier.get())
      Assertions.assertThat(job).isNotNull()
      BpmnAwareTests.execute(job)
    }
    return self()
  }

  open fun process_waits_in(taskDescriptionKey: String): SUBTYPE {
    // try to get the task
    Awaitility.await().untilAsserted {
      val taskIdOption = findTaskByActivityId(taskDescriptionKey)
      Assertions.assertThat(taskIdOption).describedAs("Process is not waiting in user task $taskDescriptionKey", taskDescriptionKey).isNotEmpty()
      taskIdOption.ifPresent { taskId: String? ->
        this.taskInformation = userTaskSupport.getTaskInformation(taskId!!)
      }
    }
    return self()
  }

  open fun external_task_is_completed(topicName: String, variables: VariableMap): SUBTYPE {
    Objects.requireNonNull(
      topicToExternalTaskId[topicName], "No active external service task found, consider to assert using external_task_exists"
    )
    serviceTaskCompletionApi.completeTask(CompleteTaskCmd(
      topicToExternalTaskId.getValue(topicName)
    ) { variables }).get()
    return self()
  }

  open fun external_task_exists(topicName: String): SUBTYPE {
    taskSubscriptionApi.subscribeForTask(
      SubscribeForTaskCmd(restrictions,
        TaskType.EXTERNAL,
        topicName,
        null,
        { ti, _ -> topicToExternalTaskId[topicName] = ti.taskId },
        { _ -> topicToExternalTaskId.remove(topicName) })
    )
    Awaitility.await().untilAsserted {
      embeddedPullServiceTaskDelivery.refresh()
      Assertions.assertThat(topicToExternalTaskId.containsKey(topicName)).isTrue()
    }
    return self()
  }

  open fun task_is_assigned_to_user(assignee: String): SUBTYPE {
    Assertions.assertThat(task().meta["assignee"]).isEqualTo(assignee)
    return self()
  }

  open fun process_is_finished(): SUBTYPE {
    BpmnAwareTests.assertThat(processInstanceSupplier.get()).isEnded()
    return self()
  }

  open fun process_has_passed(vararg elementIds: String?): SUBTYPE {
    BpmnAwareTests.assertThat(processInstanceSupplier.get()).hasPassed(*elementIds)
    return self()
  }

  open fun process_is_deployed(resource: String): SUBTYPE {
    deploymentApi.deploy(
      DeployBundleCommand(
        listOf(fromClasspath(resource)), null
      )
    ).get()
    return self()
  }

  open fun process_stopped(): SUBTYPE {
    processEngineServices.runtimeService.deleteProcessInstance(processInstanceSupplier.get().processInstanceId, "Stopped", false, true)
    return self()
  }


  open fun task(): TaskInformation {
    return Objects.requireNonNull(taskInformation, "No task found, consider to assert using process_waits_in")
  }


  private fun findTaskByActivityId(taskDescriptionKey: String): Optional<String> {
    embeddedPullUserTaskDelivery.refresh()
    return Optional.ofNullable(
      userTaskSupport
        .getAllTasks()
        .find { ti: TaskInformation -> ti.meta[CommonRestrictions.TASK_DEFINITION_KEY] == taskDescriptionKey }?.taskId
    )
  }
}
