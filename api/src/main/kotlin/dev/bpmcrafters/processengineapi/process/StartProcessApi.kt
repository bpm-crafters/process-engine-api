package dev.bpmcrafters.processengineapi.process

import dev.bpmcrafters.processengineapi.MetaInfoAware
import java.util.concurrent.Future

/**
 * API to start new process instances.
 * @since 0.0.1
 */
interface StartProcessApi : MetaInfoAware {
  /**
   * Starts a new process instance.
   * @param cmd command describing the start.
   * @return future indicating completion and containing the reference to started instance.
   */
  fun startProcess(cmd: StartProcessCommand): Future<ProcessInformation>
}
