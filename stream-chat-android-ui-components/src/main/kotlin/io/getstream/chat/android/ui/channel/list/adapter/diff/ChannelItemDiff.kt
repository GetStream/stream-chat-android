package io.getstream.chat.android.ui.channel.list.adapter.diff

public data class ChannelItemDiff(
    val cidChanged: Boolean = true,
    val nameChanged: Boolean = true,
    val avatarViewChanged: Boolean = true,
    val lastMessageChanged: Boolean = true,
    val readStateChanged: Boolean = true,
)
