package io.getstream.chat.android.livedata.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

@Entity(tableName = "stream_chat_message", indices = [Index(value = ["cid", "createdAt"]), Index(value = ["syncStatus"])])
internal data class MessageEntity(
    @PrimaryKey
    val id: String,
    val cid: String,
    val userId: String,
    /** the message text */
    val text: String = "",
    /** the list of attachments */
    val attachments: List<Attachment> = emptyList(),
    /** message type can be system, regular or ephemeral */
    val type: String = "",
    /** if the message has been synced to the servers, default is synced */
    val syncStatus: SyncStatus = SyncStatus.COMPLETED,
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
    /** the last 5 reactions on this message */
    val latestReactions: List<ReactionEntity> = emptyList(),
    /** the reactions from the current user */
    val ownReactions: List<ReactionEntity> = emptyList(),
    /** the users mentioned in this message */
    val mentionedUsersId: List<String> = emptyList(),
    /** a mapping between reaction type and the count, ie like:10, heart:4 */
    val reactionCounts: Map<String, Int> = emptyMap(),
    /** a mapping between reaction type and the reaction score, ie like:10, heart:4 */
    val reactionScores: Map<String, Int> = emptyMap(),
    /** parent id, used for threads */
    val parentId: String? = null,
    /** slash command like /giphy etc */
    val command: String? = null,
    /** if the message was sent by shadow banned user */
    val shadowed: Boolean = false,
    /** all the custom data provided for this message */
    val extraData: Map<String, Any> = emptyMap()
)
