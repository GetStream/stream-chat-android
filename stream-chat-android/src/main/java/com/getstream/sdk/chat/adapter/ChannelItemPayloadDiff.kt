package com.getstream.sdk.chat.adapter

public data class ChannelItemPayloadDiff(
    val name: Boolean = true,
    val avatarView: Boolean = true,
    val lastMessage: Boolean = true,
    val lastMessageDate: Boolean = true,
    val readState: Boolean = true
) {
    public operator fun plus(other: ChannelItemPayloadDiff): ChannelItemPayloadDiff =
        copy(
            name = name || other.name,
            avatarView = avatarView || other.avatarView,
            lastMessage = lastMessage || other.lastMessage,
            lastMessageDate = lastMessageDate || other.lastMessageDate,
            readState = readState || other.readState,
        )
}
