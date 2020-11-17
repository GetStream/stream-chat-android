package io.getstream.chat.android.ui.channel.list.adapter.diff

public data class ChannelDiff(
    val nameChanged: Boolean = true,
    val avatarViewChanged: Boolean = true,
    val lastMessageChanged: Boolean = true,
    val readStateChanged: Boolean = true,
) {
    public fun hasDifference(): Boolean = (nameChanged || avatarViewChanged || lastMessageChanged || readStateChanged)
}
