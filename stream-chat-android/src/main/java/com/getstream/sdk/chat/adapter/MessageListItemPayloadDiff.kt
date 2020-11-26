package com.getstream.sdk.chat.adapter

public data class MessageListItemPayloadDiff(
    val text: Boolean = false,
    val reactions: Boolean = false,
    val attachments: Boolean = false,
    val replies: Boolean = false,
    val syncStatus: Boolean = false,
    val deleted: Boolean = false,
    val positions: Boolean = false,
    val readBy: Boolean = false,
)
