package dev.bpmcrafters.processengineapi.adapter.c8.task.delivery

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.task.TaskInformation
import io.camunda.tasklist.dto.Task
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.protocol.Protocol

fun ActivatedJob.toTaskInformation(): TaskInformation = TaskInformation(
  taskId = "${this.key}",
  meta = mapOf(
    CommonRestrictions.TENANT_ID to this.tenantId,
    CommonRestrictions.TASK_DEFINITION_KEY to this.elementId,
    CommonRestrictions.PROCESS_DEFINITION_KEY to this.bpmnProcessId,
    CommonRestrictions.PROCESS_DEFINITION_ID to "${this.processDefinitionKey}",
    CommonRestrictions.PROCESS_INSTANCE_ID to "${this.processInstanceKey}",
    "formKey" to this.customHeaders.getOrDefault(Protocol.USER_TASK_FORM_KEY_HEADER_NAME, null),
    "assignee" to this.customHeaders.getOrDefault(Protocol.USER_TASK_ASSIGNEE_HEADER_NAME, null),
    "dueDate" to this.customHeaders.getOrDefault(Protocol.USER_TASK_DUE_DATE_HEADER_NAME, null),
    "candidateUsers" to this.customHeaders.getOrDefault(Protocol.USER_TASK_CANDIDATE_USERS_HEADER_NAME, null),
    "candidateGroups" to this.customHeaders.getOrDefault(Protocol.USER_TASK_CANDIDATE_GROUPS_HEADER_NAME, null),
    "followUpDate" to this.customHeaders.getOrDefault(Protocol.USER_TASK_FOLLOW_UP_DATE_HEADER_NAME, null),
  )
)

fun Task.toTaskInformation(): TaskInformation = TaskInformation(
  taskId = this.id,
  meta = mapOf(
    CommonRestrictions.TASK_DEFINITION_KEY to this.taskDefinitionId,
    CommonRestrictions.PROCESS_DEFINITION_KEY to this.processDefinitionKey,
    CommonRestrictions.PROCESS_INSTANCE_ID to this.processInstanceKey,
    CommonRestrictions.TENANT_ID to this.tenantId,
    "assignee" to this.assignee,
    "candidateUsers" to (this.candidateUsers?.joinToString(",") ?: ""),
    "candidateGroups" to (this.candidateGroups?.joinToString(",") ?: ""),
    "formKey" to this.formKey,
    "followUpDate" to (this.followUpDate?.toString() ?: ""),
    "dueDate" to (this.dueDate?.toString() ?: ""),

    // currently only resolvable via task list
    "processName" to this.processName,
    "taskName" to this.name,
    "formId" to this.formId,
    "formVersion" to "${this.formVersion}",
    "creationDate" to this.creationDate.toString(),
    "taskState" to this.taskState.name,
  )
)
