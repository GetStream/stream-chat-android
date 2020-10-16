package io.getstream.chat.android.livedata.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

/**
 * ChannelStateEntity stores both the channel information as well as references
 * to all of the channel's state
 *
 * note that we don't store channel watchers or watcher_count.
 * as that information is likely to go stale when you go offline.
 *
 * messages are stored on their own table for easier pagination and updates
 *
 */
@Entity(tableName = "stream_chat_channel_state", indices = [Index(value = ["syncStatus"])])
data class ChannelEntity(var type: String, var channelId: String) {
    @PrimaryKey
    var cid: String = "%s:%s".format(type, channelId)

    /** created by user id */
    var createdByUserId: String? = null

    /** if the channel is frozen or not (new messages wont be allowed) */
    var frozen: Boolean = false

    /** if the channel is hidden (new messages will cause to reappear) */
    var hidden: Boolean = false

    /** hide messages before this date */
    var hideMessagesBefore: Date? = null

    /** till when the channel is muted */
    var mutedTill: Date? = null

    /** list of the channel members, can be regular members, moderators or admins */
    var members: MutableMap<String, MemberEntity> = mutableMapOf()

    /** list of how far each user has read */
    var reads: MutableMap<String, ChannelUserReadEntity> = mutableMapOf()

    /** denormalize the last message date so we can sort on it */
    var lastMessageAt: Date? = null

    /** denormalize the last message to optimise read performance for channel list showing the last message */
    @Embedded(prefix = "last_message")
    var lastMessage: MessageEntity? = null

    /** when the channel was created */
    var createdAt: Date? = null
    /** when the channel was updated */
    var updatedAt: Date? = null
    /** when the channel was deleted */
    var deletedAt: Date? = null
    /** all the custom data provided for this channel */
    var extraData = mutableMapOf<String, Any>()

    /** if the channel has been synced to the servers */
    var syncStatus: SyncStatus = SyncStatus.COMPLETED

    /** create a ChannelStateEntity from a Channel object */
    constructor(c: Channel) : this(c.type, c.id) {
        frozen = c.frozen
        createdAt = c.createdAt
        updatedAt = c.updatedAt
        deletedAt = c.deletedAt
        extraData = c.extraData
        syncStatus = c.syncStatus

        members = mutableMapOf()
        for (m in c.members) {
            members[m.getUserId()] = MemberEntity(m)
        }
        reads = mutableMapOf()
        for (r in c.read) {
            reads[r.getUserId()] = ChannelUserReadEntity(r)
        }
        c.messages.lastOrNull()?.let { message ->
            lastMessage = MessageEntity(message)
            lastMessageAt = message.createdAt
        }
        createdByUserId = c.createdBy.id
    }

    /** convert a channelEntity into a channel object */
    fun toChannel(userMap: Map<String, User>): Channel {
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
        c.syncStatus = syncStatus

        c.members = members.values.mapNotNull { it.toMember(userMap) }

        lastMessage?.let {
            c.messages = listOf(it.toMessage(userMap))
        }

        c.read = reads.values.map { it.toChannelUserRead(userMap) }

        c.createdBy = userMap[createdByUserId]
            ?: error("userMap doesnt contain the user $createdByUserId for the channel.created_by channel $cid")

        return c
    }

    /** updates last message and lastMessageAt on this channel entity */
    fun updateLastMessage(messageEntity: MessageEntity) {
        val createdAt = messageEntity.createdAt ?: messageEntity.createdLocallyAt
        val messageEntityCreatedAt = checkNotNull(createdAt) { "created at cant be null, be sure to set message.createdAt" }

        val updateNeeded = messageEntity.id == lastMessage?.id
        val newLastMessage = lastMessageAt == null || messageEntityCreatedAt.after(lastMessageAt)
        if (newLastMessage || updateNeeded) {
            lastMessageAt = messageEntityCreatedAt
            lastMessage = messageEntity
        }
    }

    /** updates last message and lastmessagedate on this channel entity */
    fun updateReads(read: ChannelUserRead) {
        val readEntity = ChannelUserReadEntity(read)
        reads[read.getUserId()] = readEntity
    }

    fun setMember(userId: String, member: Member?) {
        if (member == null) {
            members.remove(userId)
        } else {
            members[userId] = MemberEntity(member)
        }
    }

    private fun max(date: Date?, otherDate: Date?): Date? =
        date?.takeIf { otherDate == null || it.after(otherDate) } ?: otherDate
}
