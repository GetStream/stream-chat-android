package io.getstream.chat.android.ui.channel.list.adapter

public data class ChannelListPayloadDiff(
    val nameChanged: Boolean,
    val avatarViewChanged: Boolean,
    val lastMessageChanged: Boolean,
    val readStateChanged: Boolean,
    val unreadCountChanged: Boolean,
) {
    public fun hasDifference(): Boolean {
        return nameChanged || avatarViewChanged || lastMessageChanged || readStateChanged || unreadCountChanged
    }

    public operator fun plus(other: ChannelListPayloadDiff): ChannelListPayloadDiff =
        copy(
            nameChanged = nameChanged || other.nameChanged,
            avatarViewChanged = avatarViewChanged || other.avatarViewChanged,
            lastMessageChanged = lastMessageChanged || other.lastMessageChanged,
            readStateChanged = readStateChanged || other.readStateChanged,
            unreadCountChanged = unreadCountChanged || other.unreadCountChanged,
        )
}
