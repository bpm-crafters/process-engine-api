package dev.bpmcrafters.processengineapi

interface MetaInfoAware {
  fun meta(instance: MetaInfoAware): MetaInfo
}
