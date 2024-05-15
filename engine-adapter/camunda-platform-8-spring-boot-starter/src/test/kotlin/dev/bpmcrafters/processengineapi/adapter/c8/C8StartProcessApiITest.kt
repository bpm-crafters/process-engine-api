package dev.bpmcrafters.processengineapi.adapter.c8

import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class C8StartProcessApiITest(@Autowired processTestHelperImpl: ProcessTestHelper) : AbstractC8ApiITest(processTestHelperImpl) {

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
