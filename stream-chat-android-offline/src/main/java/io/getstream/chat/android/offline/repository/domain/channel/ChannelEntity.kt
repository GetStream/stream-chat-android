package io.getstream.chat.android.offline.repository.domain.channel

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.repository.domain.channel.member.MemberEntity
import io.getstream.chat.android.offline.repository.domain.channel.userread.ChannelUserReadEntity
import java.util.Date

/**
 * ChannelEntity stores both the channel information as well as references
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
    val type: String,
    val channelId: String,
    val cooldown: Int,
    /** created by user id */
    val createdByUserId: String,
    /** if the channel is frozen or not (new messages wont be allowed) */
    val frozen: Boolean,
    /** if the channel is hidden (new messages will cause to reappear) */
    val hidden: Boolean?,
    /** hide messages before this date */
    val hideMessagesBefore: Date?,
    /** till when the channel is muted */
    val members: Map<String, MemberEntity>,
    /** list of how far each user has read */
    val reads: Map<String, ChannelUserReadEntity>,
    /** denormalize the last message date so we can sort on it */
    val lastMessageAt: Date?,
    val lastMessageId: String?,
    /** when the channel was created */
    val createdAt: Date?,
    /** when the channel was updated */
    val updatedAt: Date?,
    /** when the channel was deleted */
    val deletedAt: Date?,
    /** all the custom data provided for this channel */
    val extraData: Map<String, Any>,
    /** if the channel has been synced to the servers */
    val syncStatus: SyncStatus,
    /** channel's team */
    val team: String,
) {
    @PrimaryKey
    var cid: String = "%s:%s".format(type, channelId)
}
