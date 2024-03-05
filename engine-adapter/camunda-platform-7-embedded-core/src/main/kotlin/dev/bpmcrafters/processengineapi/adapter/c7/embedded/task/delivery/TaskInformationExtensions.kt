package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.task.TaskInformation
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.externaltask.LockedExternalTask
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity
import org.camunda.bpm.engine.task.Task

fun Task.toTaskInformation() =
  TaskInformation(
    taskId = this.id,
    meta = mapOf(
      CommonRestrictions.TASK_TYPE to "user",
      CommonRestrictions.TASK_DEFINITION_KEY to this.taskDefinitionKey,
      CommonRestrictions.TENANT_ID to this.tenantId,
      CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceId,
      "taskName" to this.name,
      "taskDescription" to this.description,
      "assignee" to this.assignee
    )
  )

fun TaskEntity.toTaskInformation() =
  TaskInformation(
    taskId = this.id,
    meta = mapOf(
      CommonRestrictions.TASK_TYPE to "user",
      CommonRestrictions.TASK_DEFINITION_KEY to this.taskDefinitionKey,
      CommonRestrictions.TENANT_ID to this.tenantId,
      CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceId,
      "taskName" to this.name,
      "taskDescription" to this.description,
      "assignee" to this.assignee
    )
  )

fun DelegateTask.toTaskInformation() =
  TaskInformation(
    taskId = this.id,
    meta = mapOf(
      CommonRestrictions.TASK_TYPE to "user",
      CommonRestrictions.TASK_DEFINITION_KEY to this.taskDefinitionKey,
      CommonRestrictions.TENANT_ID to this.tenantId,
      CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceId,
      "taskName" to this.name,
      "taskDescription" to this.description,
      "assignee" to this.assignee
    )
  )

fun LockedExternalTask.toTaskInformation(): TaskInformation {
  return TaskInformation(
    this.id,
    mapOf(
      CommonRestrictions.PROCESS_DEFINITION_KEY to this.processDefinitionKey,
      CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceId,
      CommonRestrictions.TENANT_ID to this.tenantId,
      CommonRestrictions.TASK_TYPE to "service",
      CommonRestrictions.ACTIVITY_ID to this.activityId
    )
  )
}

fun ExternalTaskEntity.toTaskInformation(): TaskInformation {
  return TaskInformation(
    this.id,
    mapOf(
      CommonRestrictions.PROCESS_DEFINITION_KEY to this.processDefinitionKey,
      CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceId,
      CommonRestrictions.TENANT_ID to this.tenantId,
      CommonRestrictions.TASK_TYPE to "service",
      CommonRestrictions.ACTIVITY_ID to this.activityId
    )
  )
}

