package io.getstream.chat.android.ui.messages.adapter

public data class MessageListItemPayloadDiff(
    val text: Boolean,
    val reactions: Boolean,
    val attachments: Boolean,
    val replies: Boolean,
    val syncStatus: Boolean,
    val deleted: Boolean,
    val positions: Boolean,
) {
    public operator fun plus(other: MessageListItemPayloadDiff): MessageListItemPayloadDiff {
        return MessageListItemPayloadDiff(
            text = text || other.text,
            reactions = reactions || other.reactions,
            attachments = attachments || other.attachments,
            replies = replies || other.replies,
            syncStatus = syncStatus || other.syncStatus,
            deleted = deleted || other.deleted,
            positions = positions || other.positions,
        )
    }
}
