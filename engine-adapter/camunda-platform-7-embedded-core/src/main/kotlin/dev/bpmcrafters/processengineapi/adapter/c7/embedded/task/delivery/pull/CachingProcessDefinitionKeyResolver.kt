package dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull

import org.camunda.bpm.engine.RepositoryService

data class CachingProcessDefinitionKeyResolver(
  val repositoryService: RepositoryService,
  private val keys: MutableMap<String, String> = mutableMapOf()
) {

  /**
   * Resolves process definition key from repository service and uses an in-mem cache.
   * @param processDefinitionId process definition id.
   * @return corresponding key.
   */
  fun getProcessDefinitionKey(processDefinitionId: String?): String? {
    return if (processDefinitionId == null) {
      null
    } else {
      return keys.getOrPut(processDefinitionId) {
        repositoryService
          .createProcessDefinitionQuery()
          .processDefinitionId(processDefinitionId)
          .singleResult()
          .key
      }
    }
  }
}
