package com.getstream.sdk.chat.adapter

public data class ChannelItemPayloadDiff(
    val name: Boolean,
    val avatarView: Boolean,
    val lastMessage: Boolean,
    val lastMessageDate: Boolean,
    val readState: Boolean,
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
