package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.Watcher
import io.getstream.chat.android.livedata.entity.MessageEntity
import java.util.*

/**
 * A class that only stores the channel data and not all the other channel state
 * Using this prevents code bugs and issues caused by confusing the channel data vs the full channel object
 */
data class ChannelData(var type: String, var channelId: String) {
    var cid: String = "%s:%s".format(type, channelId)

    /** created by user */
    lateinit var createdBy: User

    /** if the channel is frozen or not (new messages wont be allowed) */
    var frozen: Boolean = false

    /** denormalize the last message date so we can sort on it */
    var lastMessageAt: Date? = null

    /** when the channel was created */
    var createdAt: Date? = null
    /** when the channel was updated */
    var updatedAt: Date? = null
    /** when the channel was deleted */
    var deletedAt: Date? = null
    /** all the custom data provided for this channel */
    var extraData = mutableMapOf<String, Any>()

    /** create a ChannelData object from a Channel object */
    constructor(c: Channel) : this(c.type, c.id) {
        frozen = c.frozen
        createdAt = c.createdAt
        updatedAt = c.updatedAt
        deletedAt = c.deletedAt
        extraData = c.extraData

        lastMessageAt = c.lastMessageAt
        createdBy = c.createdBy
    }

    /** convert a channelEntity into a channel object */
    fun toChannel(messages: List<Message>, members: List<Member>, reads: List<ChannelUserRead>, watchers: List<User>, watcherCount: Int): Channel {
        val c = Channel()
        c.type = type
        c.id = channelId
        c.cid = cid
        c.frozen = frozen
        c.createdAt = createdAt
        c.updatedAt = updatedAt
        c.deletedAt = deletedAt
        c.extraData = extraData
        c.lastMessageAt = lastMessageAt
        c.createdBy = createdBy

        c.messages = messages
        c.members = members
        c.watchers = watchers.map { Watcher(it.id, it, null) }
        c.watcherCount = watcherCount

        c.read = reads

        return c
    }

    /** updates last message and lastmessagedate on this channel entity */
    fun addMessage(messageEntity: MessageEntity) {
        checkNotNull(messageEntity.createdAt) { "created at cant be null, be sure to set message.createdAt" }

        if (lastMessageAt == null || messageEntity.createdAt!!.after(lastMessageAt)) {
            lastMessageAt = messageEntity.createdAt
        }
    }
}
