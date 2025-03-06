package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.task.TaskInformation
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.externaltask.LockedExternalTask
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity
import org.camunda.bpm.engine.task.IdentityLink
import org.camunda.bpm.engine.task.Task

fun Task.toTaskInformation(candidates: List<IdentityLink>, processDefinitionKey: String? = null) =
  TaskInformation(
    taskId = this.id,
    meta = mapOf(
      CommonRestrictions.PROCESS_DEFINITION_ID to this.processDefinitionId,
      CommonRestrictions.ACTIVITY_ID to this.taskDefinitionKey,
      CommonRestrictions.TENANT_ID to this.tenantId,
      CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceId,
      "taskName" to this.name,
      "taskDescription" to this.description,
      "assignee" to this.assignee,
      "creationDate" to this.createTime.toString(), // FIXME -> to zoned iso 8601
      "followUpDate" to (this.followUpDate?.toString() ?: ""), // FIXME -> to zoned iso 8601
      "dueDate" to (this.dueDate?.toString() ?: ""), // FIXME -> to zoned iso 8601
      "formKey" to this.formKey,
      "candidateUsers" to candidates.mapNotNull { it.userId }.joinToString(","),
      "candidateGroups" to candidates.mapNotNull { it.groupId }.joinToString(",")
    ).let {
      if (processDefinitionKey != null) {
        it + (CommonRestrictions.PROCESS_DEFINITION_KEY to processDefinitionKey)
      } else {
        it
      }
    }
  )

fun TaskEntity.toTaskInformation() =
  TaskInformation(
    taskId = this.id,
    meta = mapOf(
      CommonRestrictions.PROCESS_DEFINITION_ID to this.processDefinitionId,
      CommonRestrictions.ACTIVITY_ID to this.taskDefinitionKey,
      CommonRestrictions.TENANT_ID to this.tenantId,
      CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceId,
      "taskName" to this.name,
      "taskDescription" to this.description,
      "assignee" to this.assignee,
      "creationDate" to this.createTime.toString(), // FIXME -> to zoned iso 8601
      "followUpDate" to (this.followUpDate?.toString() ?: ""), // FIXME -> to zoned iso 8601
      "dueDate" to (this.dueDate?.toString() ?: ""), // FIXME -> to zoned iso 8601
      "formKey" to this.formKey,
      "candidateUsers" to this.candidates.mapNotNull { it.userId }.joinToString(","),
      "candidateGroups" to this.candidates.mapNotNull { it.groupId }.joinToString(",")
    )
  )

fun DelegateTask.toTaskInformation() =
  TaskInformation(
    taskId = this.id,
    meta = mapOf(
      CommonRestrictions.PROCESS_DEFINITION_ID to this.processDefinitionId,
      CommonRestrictions.ACTIVITY_ID to this.taskDefinitionKey,
      CommonRestrictions.TENANT_ID to this.tenantId,
      CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceId,
      "taskName" to this.name,
      "taskDescription" to this.description,
      "assignee" to this.assignee,
      "creationDate" to this.createTime.toString(), // FIXME -> to zoned iso 8601
      "followUpDate" to (this.followUpDate?.toString() ?: ""), // FIXME -> to zoned iso 8601
      "dueDate" to (this.dueDate?.toString() ?: ""), // FIXME -> to zoned iso 8601,
      "candidateUsers" to this.candidates.mapNotNull { it.userId }.joinToString(","),
      "candidateGroups" to this.candidates.mapNotNull { it.groupId }.joinToString(",")
    )
  )

fun LockedExternalTask.toTaskInformation(): TaskInformation {
  return TaskInformation(
    taskId = this.id,
    meta = mapOf(
      CommonRestrictions.PROCESS_DEFINITION_ID to this.processDefinitionId,
      CommonRestrictions.PROCESS_DEFINITION_KEY to this.processDefinitionKey,
      CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceId,
      CommonRestrictions.TENANT_ID to this.tenantId,
      CommonRestrictions.ACTIVITY_ID to this.activityId,
      "topicName" to this.topicName,
    )
  )
}

fun ExternalTaskEntity.toTaskInformation(): TaskInformation {
  return TaskInformation(
    taskId = this.id,
    meta = mapOf(
      CommonRestrictions.PROCESS_DEFINITION_ID to this.processDefinitionId,
      CommonRestrictions.PROCESS_DEFINITION_KEY to this.processDefinitionKey,
      CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceId,
      CommonRestrictions.TENANT_ID to this.tenantId,
      CommonRestrictions.ACTIVITY_ID to this.activityId,
      "topicName" to this.topicName,
    )
  )
}

