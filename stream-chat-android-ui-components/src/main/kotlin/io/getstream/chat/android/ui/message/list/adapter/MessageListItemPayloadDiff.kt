package io.getstream.chat.android.ui.message.list.adapter

public data class MessageListItemPayloadDiff(
    val text: Boolean,
    val reactions: Boolean,
    val attachments: Boolean,
    val replies: Boolean,
    val syncStatus: Boolean,
    val deleted: Boolean,
    val positions: Boolean,
    val pinned: Boolean,
    val user: Boolean,
    val mentions: Boolean,
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
            pinned = pinned || other.pinned,
            user = user || other.user,
            mentions = mentions || other.mentions
        )
    }
}
