package dev.bpmcrafters.processengineapi.task

import dev.bpmcrafters.processengineapi.PayloadSupplier
import java.util.function.Supplier

/**
 * Change payload of the task command.
 * @since 1.3
 */
abstract class ChangePayloadModifyTaskCmd(
    /**
     * Unique task id.
     */
    override val taskId: String,
) : ModifyTaskCmd {

    /**
     * Sets payload for the task.
     * @since 1.3
     */
    class UpdatePayloadTaskCmd(
        override val taskId: String,
        /**
         * Payload supplier.
         */
        private val payloadSupplier: PayloadSupplier
    ) : ChangePayloadModifyTaskCmd(
        taskId = taskId,
    ), PayloadSupplier by payloadSupplier {
        /**
         * Constructs a change payload command by task id and payload.
         * @param taskId id of the task to modify.
         * @param payload payload to use.
         */
        constructor(taskId: String, payload: Map<String, Any?>) : this(
            taskId = taskId,
            payloadSupplier = PayloadSupplier { payload },
        )
    }

    /**
     * Deletes parts of the payload of the task.
     * @since 1.3
     */
    class DeletePayloadTaskCmd(
        override val taskId: String,
        /**
         * List of keys to delete.
         */
        val payloadKeysSupplier: Supplier<List<String>>
    ) : ChangePayloadModifyTaskCmd(
        taskId = taskId
    ), Supplier<List<String>> by payloadKeysSupplier {
        /**
         * Constructs a delete payload command by task id and payload keys.
         * @param taskId id of the task to modify.
         * @param payloadKeys payload keys to delete.
         */
        constructor(taskId: String, payloadKeys: List<String>) : this(
            taskId = taskId,
            payloadKeysSupplier = Supplier { payloadKeys }
        )
    }

    /**
     * Clears the payload of the task.
     * @since 1.3
     */
    class ClearPayloadTaskCmd(
        override val taskId: String,
    ) : ChangePayloadModifyTaskCmd(
        taskId = taskId
    )

}
