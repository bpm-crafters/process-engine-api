package dev.bpmcrafters.processengineapi

/**
 * APIs capable to describe a meta-aware instance.
 * @since 0.0.1
 */
interface MetaInfoAware {
  /**
   * Provides meta information based on passed instance.
   * @param instance to provide info for.
   * @return meta info.
   */
  fun meta(instance: MetaInfoAware): MetaInfo
}
