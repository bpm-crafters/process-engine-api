package dev.bpmcrafters.processengineapi.process

import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.PayloadSupplier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class StartProcessByMessageAtElementCmdTest {

  @Test
  fun `should create command with payload supplier and restrictions`() {
    val payload = mapOf<String, Any?>("order" to "4711")
    val restrictions = mapOf(CommonRestrictions.TENANT_ID to "myTenant")
    val cmd = StartProcessByMessageAtElementCmd(
      messageName = "Msg_OrderReceived",
      elementId = "Activity_ProcessOrder",
      payloadSupplier = PayloadSupplier { payload },
      restrictions = restrictions
    )

    assertThat(cmd.messageName).isEqualTo("Msg_OrderReceived")
    assertThat(cmd.elementId).isEqualTo("Activity_ProcessOrder")
    assertThat(cmd.get()).isEqualTo(payload)
    assertThat(cmd.restrictions).isEqualTo(restrictions)
  }

  @Test
  fun `should create command from payload and restrictions`() {
    val payload = mapOf<String, Any?>("order" to "4711")
    val restrictions = mapOf(CommonRestrictions.TENANT_ID to "myTenant")
    val cmd = StartProcessByMessageAtElementCmd("Msg_OrderReceived", "Activity_ProcessOrder", payload, restrictions)

    assertThat(cmd.messageName).isEqualTo("Msg_OrderReceived")
    assertThat(cmd.elementId).isEqualTo("Activity_ProcessOrder")
    assertThat(cmd.get()).isEqualTo(payload)
    assertThat(cmd.restrictions).isEqualTo(restrictions)
  }

  @Test
  fun `should create command from payload with empty restrictions`() {
    val payload = mapOf<String, Any?>("order" to "4711")
    val cmd = StartProcessByMessageAtElementCmd("Msg_OrderReceived", "Activity_ProcessOrder", payload)

    assertThat(cmd.messageName).isEqualTo("Msg_OrderReceived")
    assertThat(cmd.elementId).isEqualTo("Activity_ProcessOrder")
    assertThat(cmd.get()).isEqualTo(payload)
    assertThat(cmd.restrictions).isEmpty()
  }

  @Test
  fun `should create command without payload and restrictions`() {
    val cmd = StartProcessByMessageAtElementCmd("Msg_OrderReceived", "Activity_ProcessOrder")

    assertThat(cmd.messageName).isEqualTo("Msg_OrderReceived")
    assertThat(cmd.elementId).isEqualTo("Activity_ProcessOrder")
    assertThat(cmd.get()).isEmpty()
    assertThat(cmd.restrictions).isEmpty()
  }
}
