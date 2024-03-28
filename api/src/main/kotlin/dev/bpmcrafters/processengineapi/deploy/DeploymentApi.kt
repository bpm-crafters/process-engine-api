package dev.bpmcrafters.processengineapi.deploy

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfoAware
import java.util.concurrent.Future

/**
 * Deploys bundles of resources into a process engine.
 * @since 0.0.1
 */
interface DeploymentApi: MetaInfoAware {

  fun deploy(cmd: DeployBundleCommand): Future<Empty>
}
