package dev.bpmcrafters.processengineapi.task

import dev.bpmcrafters.processengineapi.MetaInfoAware
import java.util.concurrent.Future

interface TaskApi : MetaInfoAware {

  fun subscribeForTask(cmd: SubscribeForTaskCmd): Future<TaskSubscription>
  fun unsubscribe(cmd: UnsubscribeFromTaskCmd): Future<Unit>
  fun completeTask(cmd: CompleteTaskCmd): Future<Unit>
  fun completeTaskByError(cmd: CompleteTaskByErrorCmd): Future<Unit>

}
