package com.getstream.sdk.chat.adapter

public data class ChannelItemPayloadDiff(
    val name: Boolean = true,
    val avatarView: Boolean = true,
    val lastMessage: Boolean = true,
    val lastMessageDate: Boolean = true,
    val readState: Boolean = true
)
