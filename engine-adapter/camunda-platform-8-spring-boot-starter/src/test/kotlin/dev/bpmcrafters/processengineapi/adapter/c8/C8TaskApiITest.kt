package dev.bpmcrafters.processengineapi.adapter.c8

import dev.bpmcrafters.processengineapi.test.ProcessTestHelper
import io.toolisticon.testing.jgiven.GIVEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.junit.Ignore
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class C8TaskApiITest(@Autowired processTestHelperImpl: ProcessTestHelper) : AbstractC8ApiITest(processTestHelperImpl) {

  @Test
  @Disabled("FIXME")
  fun `should get subscribed for user task with pull strategie`() {
    GIVEN
      .`a active user task subscription`(USER_TASK)

    WHEN
      .`start process by definition`(KEY)

    THEN
      .`we should get notified about a new user task with pull strategy`()
  }

  @Test
  fun `should get subscribed for user task with subscribing strategie`() {
    GIVEN
      .`a active user task subscription`(USER_TASK)
      .`subscribe for tasks`()

    WHEN
      .`start process by definition`(KEY)

    THEN
      .`we should get notified about a new user task with subscribing strategy`()
  }

  @Test
  @Disabled("FIXME")
  fun `should not get subscribed for user task with pull strategie after unsubscribe`() {
    GIVEN
      .`a active user task subscription`(USER_TASK)

    WHEN
      .`unsubscribe user task subscription`()
      .`start process by definition`(KEY)

    THEN
      .`we should not get notified about a new user task with pull strategy`()
  }

  @Test
  @Disabled("FIXME")
  fun `should not get subscribed for user task with subscribing strategie after unsubscribe`() {
    GIVEN
      .`a active user task subscription`(USER_TASK)
      .`subscribe for tasks`()

    WHEN
      .`unsubscribe user task subscription`()
      .`start process by definition`(KEY)

    THEN
      .`we should not get notified about a new user task with subscribing strategy`()
  }

  @Test
  fun `should get subscribed for external task with pull strategie`() {
    GIVEN
      .`a active external task subscription`(EXTERNAL_TASK)

    WHEN
      .`start process by definition`(KEY)

    THEN
      .`we should get notified about a new external task`()
  }

  @Test
  @Disabled("FIXME")
  fun `should not get subscribed for external task with pull strategie after unsubscribe`() {
    GIVEN
      .`a active external task subscription`(USER_TASK)

    WHEN
      .`unsubscribe external task subscription`()
      .`start process by definition`(KEY)

    THEN
      .`we should not get notified about a new external task`()
  }

  @Test
  @Disabled("FIXME")
  fun `should complete a user task`() {
    GIVEN
      .`a active user task subscription`(USER_TASK)
      .`start process by definition`(KEY)

    THEN
      .`we should get notified about a new user task with pull strategy`()

    WHEN
      .`complete the user task`()
  }

  @Test
  @Disabled("FIXME")
  fun `should complete a external task`() {
    GIVEN
      .`a active external task subscription`(EXTERNAL_TASK)
      .`start process by definition`(KEY)

    THEN
      .`we should get notified about a new external task`()

    WHEN
      .`complete the external task`()
  }

}
