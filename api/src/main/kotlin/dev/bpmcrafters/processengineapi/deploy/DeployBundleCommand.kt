package dev.bpmcrafters.processengineapi.deploy

/**
 * Command to deploy a bundle of resources.
 * @since 0.0.1
 */
data class DeployBundleCommand(
  /**
   * Resources to deploy.
   */
  val resources: List<NamedResource>,
  /**
   * Optional tenant id.
   */
  val tenantId: String? = null
)
