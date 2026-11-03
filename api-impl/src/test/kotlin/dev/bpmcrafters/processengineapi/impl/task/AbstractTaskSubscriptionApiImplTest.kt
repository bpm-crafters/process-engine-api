package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.task.SubscribeForTaskCmd
import dev.bpmcrafters.processengineapi.task.TaskHandlerContext
import dev.bpmcrafters.processengineapi.task.TaskHandlerObserver
import dev.bpmcrafters.processengineapi.task.TaskHandler
import dev.bpmcrafters.processengineapi.task.TaskHandlerOutcome
import dev.bpmcrafters.processengineapi.task.TaskInformation
import dev.bpmcrafters.processengineapi.task.TaskType
import dev.bpmcrafters.processengineapi.task.UnsubscribeFromTaskCmd
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class AbstractTaskSubscriptionApiImplTest {

  private val subscriptionRepository: SubscriptionRepository = InMemSubscriptionRepository()
  private val taskSubscriptionApi = MyTaskSubscriptionApiImpl(subscriptionRepository)
  class MyTaskSubscriptionApiImpl(
    subscriptionRepository: SubscriptionRepository,
    observers: List<TaskHandlerObserver<*>> = emptyList()
  ) : AbstractTaskSubscriptionApiImpl(subscriptionRepository, observers) {

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


    Assertions.assertThat(x).isNotNull
    Assertions.assertThat(x).isInstanceOf(TaskSubscriptionHandle::class.java)
    Assertions.assertThat((x as TaskSubscriptionHandle).taskType).isEqualTo(TaskType.EXTERNAL)
    Assertions.assertThat(this.subscriptionRepository.getTaskSubscriptions()).containsExactly(x)

    taskSubscriptionApi.unsubscribe(
        UnsubscribeFromTaskCmd(x)
    ).get()

    Assertions.assertThat(this.subscriptionRepository.getTaskSubscriptions()).isEmpty()
  }

  @Test
  fun `keeps constructor without observers for existing adapters`() {
    Assertions.assertThat(AbstractTaskSubscriptionApiImpl::class.java.getDeclaredConstructor(SubscriptionRepository::class.java)).isNotNull
  }

  @Test
  fun `invokes action when no observers are configured`() {
    var invocations = 0
    val action = TaskHandler { _, _ -> invocations++ }

    val handle = taskSubscriptionApi.subscribeForTask(
        SubscribeForTaskCmd(
            CommonRestrictions.builder().withProcessDefinitionKey("process-key").build(),
            TaskType.EXTERNAL,
            null,
            setOf("var1"),
            action,
            { _ -> }
        )
    ).get() as TaskSubscriptionHandle

    handle.action.accept(TaskInformation("task-id", emptyMap()), emptyMap())

    Assertions.assertThat(invocations).isEqualTo(1)
  }

  @Test
  fun `wraps action so observer runs on delivery and unsubscribe still works`() {
    val observer = TracingObserver()
    val api = MyTaskSubscriptionApiImpl(subscriptionRepository, listOf(observer))
    val action = TaskHandler { _, _ -> observer.trace += "action" }

    val handle = api.subscribeForTask(
        SubscribeForTaskCmd(
            CommonRestrictions.builder().withProcessDefinitionKey("process-key").build(),
            TaskType.EXTERNAL,
            null,
            setOf("var1"),
            action,
            { _ -> }
        )
    ).get() as TaskSubscriptionHandle

    Assertions.assertThat(handle.action).isNotSameAs(action)
    handle.action.accept(TaskInformation("task-id", emptyMap()), emptyMap())
    Assertions.assertThat(observer.trace).containsExactly("before", "action", "after")

    api.unsubscribe(UnsubscribeFromTaskCmd(handle)).get()
    Assertions.assertThat(subscriptionRepository.getTaskSubscriptions()).isEmpty()
  }

  private class TracingObserver : TaskHandlerObserver<Unit> {
    val trace = mutableListOf<String>()

    override fun before(context: TaskHandlerContext) {
      trace += "before"
    }

    override fun after(context: TaskHandlerContext, state: Unit, outcome: TaskHandlerOutcome, error: Throwable?) {
      trace += "after"
    }
  }
}
