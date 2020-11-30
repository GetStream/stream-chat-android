package com.getstream.sdk.chat.adapter

public data class MessageListItemPayloadDiff(
    val text: Boolean,
    val reactions: Boolean,
    val attachments: Boolean,
    val replies: Boolean,
    val syncStatus: Boolean,
    val deleted: Boolean,
    val positions: Boolean,
    val readBy: Boolean,
) {
    public operator fun plus(other: MessageListItemPayloadDiff): MessageListItemPayloadDiff =
        copy(
            text = text || other.text,
            reactions = reactions || other.reactions,
            attachments = attachments || other.attachments,
            replies = replies || other.replies,
            syncStatus = syncStatus || other.syncStatus,
            deleted = deleted || other.deleted,
            positions = positions || other.positions,
            readBy = readBy || other.readBy,
        )
}
