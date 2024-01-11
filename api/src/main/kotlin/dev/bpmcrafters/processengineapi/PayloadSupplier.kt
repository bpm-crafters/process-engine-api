package dev.bpmcrafters.processengineapi

interface PayloadSupplier {
  fun payload(): () -> Map<String, Any>
}
