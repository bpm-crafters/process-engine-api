package dev.bpmcrafters.processengineapi.adapter.c8

import io.toolisticon.testing.jgiven.GIVEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class C8StartProcessApiITest : AbstractC8ApiITest() {

  @Test
  fun `should start process by definition without payload`() {
    WHEN
      .`start process by definition`(KEY)
    THEN
      .`we should have a running process`()
  }

  @Test
  fun `should start process by definition with payload`() {
    WHEN
      .`start process by definition with payload`(KEY, "key" to "value")

    THEN
      .`we should have a running process`()
  }

}
