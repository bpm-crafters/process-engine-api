package dev.bpmcrafters.processengineapi.adapter.c7

import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.task.*
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery
import org.springframework.scheduling.annotation.Scheduled
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class TaskApiImpl(private val externalTaskService: ExternalTaskService) : TaskApi {

  private val subscriptions: MutableList<TaskSubscriptionHandle> = mutableListOf()
  private val workerId: String = UUID.randomUUID().toString()

  override fun subscribeForTask(cmd: SubscribeForTaskCmd): Future<TaskSubscription> {
    return TaskSubscriptionHandle(
      topic = cmd.taskDescriptionKey,
      payloadDescription = cmd.payloadDescription,
      action = cmd.action
    ).let {
      subscriptions.add(it)
      CompletableFuture.completedFuture(it)
    }
  }

  override fun unsubscribe(cmd: UnsubscribeFromTaskCmd): Future<Unit> {
    subscriptions.remove(cmd.subscription)
    return CompletableFuture.completedFuture(null)
  }

  override fun completeTask(cmd: CompleteTaskCmd): Future<Unit> {
    externalTaskService.complete(cmd.taskId, workerId, cmd.get())
    return CompletableFuture.completedFuture(null)
  }

  override fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Unit> {
    TODO("Not yet implemented")
  }

  override fun meta(instance: MetaInfoAware): MetaInfo {
    TODO("Not yet implemented")
  }

  @Scheduled(fixedRate = 15_000L)
  fun supply() {
    externalTaskService
      .fetchAndLock(10, workerId)
      .apply {
        subscriptions.forEach { subscription ->
          this.topic(subscription.topic, 10)
        }
      }
      .execute()
      .forEach { lockedTask ->
        subscriptions.firstOrNull { subscription -> subscription.topic == lockedTask.topicName }?.let { activeSubscription ->
          val variables = if (activeSubscription.payloadDescription.isEmpty()) {
            lockedTask.variables
          } else {
            lockedTask.variables.filter { activeSubscription.payloadDescription.contains(it.key) }
          }
          activeSubscription.action.accept(lockedTask.id, variables)
        }
      }
  }

  override fun getSupportedRestrictions(): Set<String> = setOf()

  private fun ExternalTaskQuery.createSubscription(cmd: SubscribeForTaskCmd): ExternalTaskQuery = this.apply {

  }

  data class TaskSubscriptionHandle(
    val topic: String,
    val payloadDescription: Set<String>,
    val action: TaskHandler
  ) : TaskSubscription

}
