package dev.bpmcrafters.processengineapi.correlation

import dev.bpmcrafters.processengineapi.MetaInfoAware
import java.util.concurrent.Future

interface CorrelationApi : MetaInfoAware {
  fun correlateMessage(cmd: CorrelateMessageCmd): Future<Void>
}
