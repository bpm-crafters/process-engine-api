package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.task.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class AbstractTaskSubscriptionApiImplTest {

  private val subscriptionRepository: SubscriptionRepository = InMemSubscriptionRepository()
  private val taskSubscriptionApi = MyTaskSubscriptionApiImpl(subscriptionRepository)

  class MyTaskSubscriptionApiImpl(
    subscriptionRepository: SubscriptionRepository,
    observers: List<TaskExecutionObserver> = emptyList()
  ) : AbstractTaskSubscriptionApiImpl(subscriptionRepository, observers) {

    override fun getSupportedRestrictions(): Set<String> = setOf(
      CommonRestrictions.PROCESS_DEFINITION_KEY,
      CommonRestrictions.TENANT_ID,
      CommonRestrictions.WITHOUT_TENANT_ID,
    )

    override fun meta(instance: MetaInfoAware): MetaInfo {
      TODO("Not yet implemented")
    }
  }

  @Test
  fun `subscribe and unsubscribe`() {

    val x = taskSubscriptionApi.subscribeForTask(
      SubscribeForTaskCmd(
        CommonRestrictions.builder().withProcessDefinitionKey("process-key").build(),
        TaskType.EXTERNAL,
        null,
        setOf("var1"),
        { _, _ -> },
        { _ -> }
      )
    ).get()


    assertThat(x).isNotNull
    assertThat(x).isInstanceOf(TaskSubscriptionHandle::class.java)
    assertThat((x as TaskSubscriptionHandle).taskType).isEqualTo(TaskType.EXTERNAL)
    assertThat(this.subscriptionRepository.getTaskSubscriptions()).containsExactly(x)

    taskSubscriptionApi.unsubscribe(
      UnsubscribeFromTaskCmd(x)
    ).get()

    assertThat(this.subscriptionRepository.getTaskSubscriptions()).isEmpty()
  }

  @Test
  fun `rejects unsupported restrictions before creating a subscription`() {
    val command = subscribeCmd(TaskType.EXTERNAL, TaskHandler { _, _ -> }).copy(
      restrictions = mapOf(CommonRestrictions.BUSINESS_KEY to "business-key")
    )

    assertThatThrownBy { taskSubscriptionApi.subscribeForTask(command) }.isInstanceOf(IllegalArgumentException::class.java)
    assertThat(subscriptionRepository.getTaskSubscriptions()).isEmpty()
  }

  @Test
  fun `rejects tenant and without tenant restrictions together`() {
    val command = subscribeCmd(TaskType.EXTERNAL, TaskHandler { _, _ -> }).copy(
      restrictions = mapOf(
        CommonRestrictions.TENANT_ID to "tenant",
        CommonRestrictions.WITHOUT_TENANT_ID to "true",
      )
    )

    assertThatThrownBy { taskSubscriptionApi.subscribeForTask(command) }
      .isInstanceOf(IllegalArgumentException::class.java)
    assertThat(subscriptionRepository.getTaskSubscriptions()).isEmpty()
  }

  @Test
  fun `keeps the handlers without observers`() {
    val handler = TaskHandler { _, _ -> }
    val termination = TaskTerminationHandler { _ -> }

    val subscription = taskSubscriptionApi.subscribeForTask(subscribeCmd(TaskType.EXTERNAL, handler, termination)).get() as TaskSubscriptionHandle

    assertThat(subscription.action).isSameAs(handler)
    assertThat(subscription.termination).isSameAs(termination)
  }

  @Test
  fun `observes handlers of external tasks`() {
    val recordedSteps = mutableListOf<String>()
    val observedApi = MyTaskSubscriptionApiImpl(InMemSubscriptionRepository(), listOf(RecordingObserver("observer", recordedSteps)))

    val subscription = observedApi.subscribeForTask(subscribeCmd(TaskType.EXTERNAL, { _, _ -> recordedSteps.add("handler") })).get() as TaskSubscriptionHandle
    subscription.action.accept(TaskInformation("task-1", mapOf()), mapOf())

    assertThat(recordedSteps).containsExactly("observer start EXTERNAL task-1", "handler", "observer cleanup task-1")
  }

  @Test
  fun `observes delivery and termination of user tasks`() {
    val recordedSteps = mutableListOf<String>()
    val observedApi = MyTaskSubscriptionApiImpl(InMemSubscriptionRepository(), listOf(RecordingObserver("observer", recordedSteps)))
    val task = TaskInformation("task-1", mapOf())

    val subscription = observedApi.subscribeForTask(
      subscribeCmd(TaskType.USER, { _, _ -> recordedSteps.add("delivered") }, { _ -> recordedSteps.add("terminated") })
    ).get() as TaskSubscriptionHandle
    subscription.action.accept(task, mapOf())
    subscription.termination.accept(task)

    assertThat(recordedSteps).containsExactly(
      "observer start USER task-1",
      "delivered",
      "observer cleanup task-1",
      "observer termination USER task-1",
      "terminated"
    )
  }

  private fun subscribeCmd(taskType: TaskType, handler: TaskHandler, termination: TaskTerminationHandler = TaskTerminationHandler { _ -> }) =
    SubscribeForTaskCmd(CommonRestrictions.builder().build(), taskType, null, null, handler, termination)
}
