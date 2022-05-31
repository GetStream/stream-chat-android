package io.getstream.chat.android.client.models

private const val IN_PROGRESS_AWAIT_ATTACHMENTS_TYPE = 100
private const val FAILED_MODERATION_TYPE = 200

public enum class MessageSyncType(
    public val alias: String,
    public val type: Int,
) {

    IN_PROGRESS_AWAIT_ATTACHMENTS(
        alias = "message.in_progress.await_attachments",
        type = IN_PROGRESS_AWAIT_ATTACHMENTS_TYPE
    ),
    FAILED_MODERATION(
        alias = "message.failed.moderation",
        type = FAILED_MODERATION_TYPE
    );

    public companion object {
        private val map = MessageSyncType.values().associateBy(MessageSyncType::type)
        public fun fromInt(type: Int): MessageSyncType? = map[type]
        public const val TYPE: String = "type"
    }

}