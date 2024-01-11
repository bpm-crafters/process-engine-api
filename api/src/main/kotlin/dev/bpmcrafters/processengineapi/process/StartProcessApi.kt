package dev.bpmcrafters.processengineapi.process

import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import java.util.concurrent.Future

interface StartProcessApi : MetaInfoAware {
  fun startProcess(cmd: StartProcessCommand): Future<StartProcessResponse>
}
