package io.getstream.chat.android.offline.repository.domain.message.internal

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.models.MessageSyncType
import io.getstream.chat.android.models.SyncStatus
import java.util.Date

@Entity(tableName = REPLY_MESSAGE_ENTITY_TABLE_NAME)
internal data class ReplyMessageEntity(
    @PrimaryKey
    val id: String,
    val cid: String,
    val userId: String,
    /** the message text */
    val text: String = "",
    /** the message text formatted as html **/
    val html: String = "",
    /** message type can be system, regular or ephemeral */
    val type: String = "",
    /** if the message has been synced to the servers, default is synced */
    val syncStatus: SyncStatus = SyncStatus.COMPLETED,

    val syncType: MessageSyncType? = null,

    // val syncContent: MessageSyncContentEntity? = null,

    /** the number of replies */
    val replyCount: Int = 0,
    /** when the message was created */
    val createdAt: Date? = null,
    /** when the message was created locally */
    val createdLocallyAt: Date? = null,
    /** when the message was updated */
    val updatedAt: Date? = null,
    /** when the message was updated locally */
    val updatedLocallyAt: Date? = null,
    /** when the message was deleted */
    val deletedAt: Date? = null,
    /** the users mentioned in this message */
    val remoteMentionedUserIds: List<String> = emptyList(),
    /** the users to be mentioned in this message */
    val mentionedUsersId: List<String> = emptyList(),
    /** parent id, used for threads */
    val parentId: String? = null,
    /** slash command like /giphy etc */
    val command: String? = null,
    /** if the message was sent by shadow banned user */
    val shadowed: Boolean = false,
    /** if the message is also shown in the channel **/
    val showInChannel: Boolean = false,
    // @Embedded(prefix = "channel_info")
    // val channelInfo: ChannelInfoEntity? = null,
    /** if the message is silent  **/
    val silent: Boolean = false,
    // /** all the custom data provided for this message */
    // val extraData: Map<String, Any> = emptyMap(),
    /** whether message is pinned or not **/
    val pinned: Boolean,
    /** date when the message got pinned **/
    val pinnedAt: Date? = null,
    /** date when pinned message expires **/
    val pinExpires: Date? = null,
    /** the ID of the user who pinned the message **/
    val pinnedByUserId: String?,
    /** participants of thread replies */
    val threadParticipantsIds: List<String> = emptyList(),
)

internal const val REPLY_MESSAGE_ENTITY_TABLE_NAME = "stream_chat_reply_message"
