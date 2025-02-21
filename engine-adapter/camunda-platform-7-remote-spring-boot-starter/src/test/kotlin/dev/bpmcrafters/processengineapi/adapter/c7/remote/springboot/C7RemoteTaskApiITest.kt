package dev.bpmcrafters.processengineapi.adapter.c7.remote.springboot

import io.toolisticon.testing.jgiven.GIVEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("itest")
class C7RemoteTaskApiITest : AbstractC7RemoteApiITestBase() {

  @Test
  fun `should get subscribed for user task with pull strategy`() {
    GIVEN
      .`a active user task subscription`(USER_TASK)

    WHEN
      .`start process by definition`(KEY)

    THEN
      .`we should get notified about a new user task with pull strategy`()
  }

  @Test
  fun `should get subscribed for external task with pull strategy`() {
    GIVEN
      .`a active external task subscription`(EXTERNAL_TASK)

    WHEN
      .`start process by definition`(KEY)

    THEN
      .`we should get notified about a new external task`()
  }

  @Test
  fun `should not get subscribed for user task with pull strategy after unsubscribe`() {
    GIVEN
      .`a active user task subscription`(USER_TASK)

    WHEN
      .`unsubscribe user task subscription`()
      .`start process by definition`(KEY)

    THEN
      .`we should not get notified about a new user task with pull strategy`()
  }

  @Test
  fun `should not get subscribed for external task with pull strategy after unsubscribe`() {
    GIVEN
      .`a active external task subscription`(USER_TASK)

    WHEN
      .`unsubscribe external task subscription`()
      .`start process by definition`(KEY)

    THEN
      .`we should not get notified about a new external task`()
  }

  @Test
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
