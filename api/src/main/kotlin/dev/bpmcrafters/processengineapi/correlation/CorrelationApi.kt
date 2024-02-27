package dev.bpmcrafters.processengineapi.correlation

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.RestrictionAware
import java.util.concurrent.Future

/**
 * API to correlate messages or signals with running process instances.
 * @since 0.0.1
 */
interface CorrelationApi : MetaInfoAware, RestrictionAware {
  /**
   * Correlates message.
   * @param cmd command to correlate.
   * @return future to indicate completion.
   */
  fun correlateMessage(cmd: CorrelateMessageCmd): Future<Empty>

  /**
   * Delivers a signal event to process engine.
   * @param cmd command to deliver.
   * @return future to indicate completion.
   */
  fun sendSignal(cmd: SendSignalCmd): Future<Empty>
}
