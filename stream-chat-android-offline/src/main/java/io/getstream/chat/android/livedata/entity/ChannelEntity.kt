package io.getstream.chat.android.livedata.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
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
internal data class ChannelEntity(
    var type: String,
    var channelId: String,
    val cooldown: Int = 0,
    @PrimaryKey
    var cid: String = "%s:%s".format(type, channelId),

    /** created by user id */
    var createdByUserId: String,

    /** if the channel is frozen or not (new messages wont be allowed) */
    var frozen: Boolean = false,

    /** if the channel is hidden (new messages will cause to reappear) */
    var hidden: Boolean? = null,

    /** hide messages before this date */
    var hideMessagesBefore: Date? = null,

    /** till when the channel is muted */
    var mutedTill: Date? = null,

    /** list of the channel members, can be regular members, moderators or admins */
    var members: MutableMap<String, MemberEntity> = mutableMapOf(),

    /** list of how far each user has read */
    var reads: MutableMap<String, ChannelUserReadEntity> = mutableMapOf(),

    /** denormalize the last message date so we can sort on it */
    var lastMessageAt: Date? = null,

    var lastMessageId: String? = null,

    /** when the channel was created */
    var createdAt: Date? = null,

    /** when the channel was updated */
    var updatedAt: Date? = null,

    /** when the channel was deleted */
    var deletedAt: Date? = null,

    /** all the custom data provided for this channel */
    var extraData: MutableMap<String, Any> = mutableMapOf(),

    /** if the channel has been synced to the servers */
    var syncStatus: SyncStatus = SyncStatus.COMPLETED,
) {
    /** updates last message and lastMessageAt on this channel entity */
    internal fun updateLastMessage(messageEntity: MessageEntity) {
        val createdAt = messageEntity.messageInnerEntity.createdAt ?: messageEntity.messageInnerEntity.createdLocallyAt
        val messageEntityCreatedAt = checkNotNull(createdAt) { "created at cant be null, be sure to set message.createdAt" }

        val updateNeeded = messageEntity.messageInnerEntity.id == lastMessageId
        val newLastMessage = lastMessageAt == null || messageEntityCreatedAt.after(lastMessageAt)
        if (newLastMessage || updateNeeded) {
            lastMessageAt = messageEntityCreatedAt
            lastMessageId = messageEntity.messageInnerEntity.id
        }
    }
}
