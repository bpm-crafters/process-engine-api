package dev.bpmcrafters.processengineapi.impl.task

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.task.SubscribeForTaskCmd
import dev.bpmcrafters.processengineapi.task.TaskExecutionObserver
import dev.bpmcrafters.processengineapi.task.TaskHandler
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
    observers: List<TaskExecutionObserver> = emptyList()
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
  fun `keeps the handler without observers`() {
    val handler = TaskHandler { _, _ -> }

    val subscription = taskSubscriptionApi.subscribeForTask(subscribeCmd(TaskType.EXTERNAL, handler)).get() as TaskSubscriptionHandle

    Assertions.assertThat(subscription.action).isSameAs(handler)
  }

  @Test
  fun `observes handlers of external tasks`() {
    val recordedSteps = mutableListOf<String>()
    val observedApi = MyTaskSubscriptionApiImpl(InMemSubscriptionRepository(), listOf(RecordingObserver("observer", recordedSteps)))

    val subscription = observedApi.subscribeForTask(subscribeCmd(TaskType.EXTERNAL) { _, _ -> recordedSteps.add("handler") }).get() as TaskSubscriptionHandle
    subscription.action.accept(TaskInformation("task-1", mapOf()), mapOf())

    Assertions.assertThat(recordedSteps).containsExactly("observer start task-1", "handler", "observer cleanup task-1")
  }

  @Test
  fun `does not observe handlers of user tasks`() {
    val handler = TaskHandler { _, _ -> }
    val observedApi = MyTaskSubscriptionApiImpl(InMemSubscriptionRepository(), listOf(object : TaskExecutionObserver {}))

    val subscription = observedApi.subscribeForTask(subscribeCmd(TaskType.USER, handler)).get() as TaskSubscriptionHandle

    Assertions.assertThat(subscription.action).isSameAs(handler)
  }

  private fun subscribeCmd(taskType: TaskType, handler: TaskHandler) =
    SubscribeForTaskCmd(CommonRestrictions.builder().build(), taskType, null, null, handler) { _ -> }
}
