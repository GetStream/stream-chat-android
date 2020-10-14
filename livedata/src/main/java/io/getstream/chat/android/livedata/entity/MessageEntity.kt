package io.getstream.chat.android.livedata.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

/**
 * The Message Entity. Text and attachments are the most commonly used fields.
 *
 * You can convert a Message object from the low level client to a MessageEntity like this:
 * val messageEntity = MessageEntity(message)
 * and back:
 * messageEntity.toMessage()
 */
@Entity(tableName = "stream_chat_message")
data class MessageEntity(
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
    /** tracks when send message was completed */
    val sendMessageCompletedAt: Date? = null,
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
    /** all the custom data provided for this message */
    val extraData: Map<String, Any> = emptyMap()
) {
    companion object {
        /** create a messageEntity from a message object */
        fun newEntity(m: Message) = MessageEntity(
            id = m.id, cid = m.cid, userId = m.user.id,
            text = m.text,
            attachments = m.attachments,
            syncStatus = m.syncStatus ?: SyncStatus.COMPLETED,
            type = m.type,
            replyCount = m.replyCount,
            createdAt = m.createdAt,
            createdLocallyAt = m.createdLocallyAt,
            updatedAt = m.updatedAt,
            updatedLocallyAt = m.updatedLocallyAt,
            deletedAt = m.deletedAt,
            parentId = m.parentId,
            command = m.command,
            extraData = m.extraData,
            reactionCounts = m.reactionCounts ?: mutableMapOf(),
            reactionScores = m.reactionScores ?: mutableMapOf(),
            sendMessageCompletedAt = if (m.syncStatus == SyncStatus.COMPLETED) Date() else null,
            // for these we need a little map,
            latestReactions = (m.latestReactions.map { ReactionEntity(it) }).toMutableList(),
            ownReactions = (m.ownReactions.map { ReactionEntity(it) }).toMutableList(),
            mentionedUsersId = (m.mentionedUsers.map { it.id }).toMutableList()
        )
    }

    /** converts a message entity into a message object */
    fun toMessage(userMap: Map<String, User>): Message {
        val m = Message()
        m.id = id
        m.cid = cid
        m.user = userMap[userId]
            ?: error("userMap doesnt contain user id $userId for message id ${m.id}")
        m.text = text
        m.attachments = attachments.toMutableList()
        m.type = type
        m.replyCount = replyCount
        m.createdAt = createdAt
        m.createdLocallyAt = createdLocallyAt
        m.updatedAt = updatedAt
        m.updatedLocallyAt = updatedLocallyAt
        m.deletedAt = deletedAt
        m.parentId = parentId
        m.command = command
        m.extraData = extraData.toMutableMap()
        m.reactionCounts = reactionCounts.toMutableMap()
        m.reactionScores = reactionScores.toMutableMap()
        m.syncStatus = syncStatus ?: SyncStatus.COMPLETED
        m.latestReactions = (latestReactions.map { it.toReaction(userMap) }).toMutableList()
        m.ownReactions = (ownReactions.map { it.toReaction(userMap) }).toMutableList()
        m.mentionedUsers = mentionedUsersId.mapNotNull { userMap[it] }.toMutableList()
        return m
    }
}
