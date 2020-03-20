package com.getstream.sdk.chat.livedata.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import java.util.*


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
@Entity(tableName = "stream_chat_channel_state")
data class ChannelStateEntity(var type: String, var channelId: String) {
    @PrimaryKey
    var cid: String = "%s:%s".format(type, channelId)

    /** created by user id */
    var createdByUserId: String? = null

    /** if the channel is frozen or not (new messages wont be allowed) */
    var frozen: Boolean = false

    // TODO: channel configs should be stored at the channel type level

    // TODO: perhaps create a member entity?
    /** list of the channel members, can be regular members, moderators or admins */
    var members: List<Member>? = null

    /** list of how far each user has read */
    // TODO: this should perhaps be an entity for easier storage optimization
    var reads: List<ChannelUserRead>? = null

    /** denormalized copy of the last message */
    @Embedded(prefix = "last_message_")
    var lastMessage: MessageEntity? = null
    /** denormalize the last message date so we can sort on it */
    var lastMessageDate: Date? = null

    /** when the channel was created */
    var createdAt: Date? = null
    /** when the channel was updated */
    var updatedAt: Date? = null
    /** when the channel was deleted */
    var deletedAt: Date? = null
    /** all the custom data provided for this channel */
    var extraData = mutableMapOf<String, Any>()

    /** if the channel has been synced to the servers */
    var syncStatus: Int? = null

    /** create a ChannelStateEntity from a Channel object */
    constructor(c: Channel): this(c.type, c.id) {
        if (c.messages.isNotEmpty()) {
            lastMessage = MessageEntity(c.messages.last())
        }


        // TODO: Implement me
    }


}