package dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull

import org.camunda.bpm.engine.RepositoryService

/**
 * Simple in-memory caching resolver for process definition for a given process definition id.
 */
data class CachingProcessDefinitionKeyResolver(
  val repositoryService: RepositoryService,
  private val keys: MutableMap<String, String> = mutableMapOf(),
  private val versionTags: MutableMap<String, String> = mutableMapOf()
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

  /**
   * Resolves process definition version tag from repository service and uses an in-mem cache.
   * @param processDefinitionId process definition id.
   * @return corresponding version tag.
   */
  fun getProcessDefinitionVersionTag(processDefinitionId: String?): String? {
    return if (processDefinitionId == null) {
      null
    } else {
      return versionTags.getOrPut(processDefinitionId) {
        repositoryService
          .createProcessDefinitionQuery()
          .processDefinitionId(processDefinitionId)
          .singleResult()
          .versionTag
      }
    }
  }
}
