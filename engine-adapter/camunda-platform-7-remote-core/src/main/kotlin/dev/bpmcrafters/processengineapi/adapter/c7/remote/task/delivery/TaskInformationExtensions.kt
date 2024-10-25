package dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.task.TaskInformation
import org.camunda.bpm.engine.externaltask.LockedExternalTask
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.client.task.ExternalTask as RemoteExternalTask


fun RemoteExternalTask.toTaskInformation(): TaskInformation = TaskInformation(
  taskId = this.id,
  meta = mapOf(
    CommonRestrictions.TENANT_ID to this.tenantId,
    CommonRestrictions.TASK_DEFINITION_KEY to this.topicName,
    CommonRestrictions.PROCESS_DEFINITION_KEY to this.processDefinitionKey,
    CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceId,
    // FIXME more
  )
)

fun LockedExternalTask.toTaskInformation(): TaskInformation = TaskInformation(
  taskId = this.id,
  meta = mapOf(
    CommonRestrictions.TENANT_ID to this.tenantId,
    CommonRestrictions.TASK_DEFINITION_KEY to this.topicName,
    CommonRestrictions.PROCESS_DEFINITION_KEY to this.processDefinitionKey,
    CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceId,
    // FIXME more
  )
)

fun Task.toTaskInformation() =
  TaskInformation(
    taskId = this.id,
    meta = mapOf(
      CommonRestrictions.TASK_DEFINITION_KEY to this.taskDefinitionKey,
      CommonRestrictions.TENANT_ID to this.tenantId,
      CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceId,
      "processDefinitionId" to this.processDefinitionId,
      "taskName" to this.name,
      "taskDescription" to this.description,
      "assignee" to this.assignee,
      "creationDate" to this.createTime.toString(), // FIXME -> to zoned iso 8601
      "followUpDate" to (this.followUpDate?.toString() ?: ""), // FIXME -> to zoned iso 8601
      "dueDate" to (this.dueDate?.toString() ?: ""), // FIXME -> to zoned iso 8601
      "formKey" to this.formKey
    )
  )

