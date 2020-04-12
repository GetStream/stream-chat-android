package io.getstream.chat.android.livedata.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.*


/**
 * The Message Entity. Text and attachments are the most commonly used fields.
 *
 * You can convert a Message object from the low level client to a MessageEntity like this:
 * val messageEntity = MessageEntity(message)
 * and back:
 * messageEntity.toMessage()
 */
@Entity(tableName = "stream_chat_message")
data class MessageEntity(@PrimaryKey var id: String, var cid: String, var userId: String) {

    /** the message text */
    var text: String = ""

    /** the list of attachments */
    var attachments: MutableList<Attachment> = mutableListOf()

    /** message type can be system, regular or ephemeral */
    var type: String = ""

    /** if the message has been synced to the servers, default is synced */
    var syncStatus: SyncStatus = SyncStatus.SYNCED

    /** tracks when send message was completed */
    var sendMessageCompletedAt: Date? = null

    /** the number of replies */
    var replyCount = 0

    /** when the message was created */
    var createdAt: Date? = null
    /** when the message was updated */
    var updatedAt: Date? = null
    /** when the message was deleted */
    var deletedAt: Date? = null

    /** the last 5 reactions on this message */
    var latestReactions: MutableList<ReactionEntity> = mutableListOf()

    /** the reactions from the current user */
    var ownReactions: MutableList<ReactionEntity> = mutableListOf()

    /** the users mentioned in this message */
    var mentionedUsersId: MutableList<String> = mutableListOf()

    /** a mapping between reaction type and the count, ie like:10, heart:4 */
    var reactionCounts: MutableMap<String, Int> = mutableMapOf()

    /** parent id, used for threads */
    var parentId: String? = null

    /** slash command like /giphy etc */
    var command: String? = null

    /** command info */
    var commandInfo: Map<String, String>? = null

    /** all the custom data provided for this message */
    var extraData = mutableMapOf<String, Any>()

    /** add a reaction to this message. updated the own reactions, latestReactions, reaction Count */
    fun addReaction(reaction: Reaction, isMine: Boolean) {
        val reactionEntity = ReactionEntity(reaction)

        // add to own reactions
        if (isMine) {
            ownReactions.add(reactionEntity)
        }

        // add to latest reactions
        latestReactions.add(reactionEntity)

        // update the count
        val currentCount = reactionCounts.getOrElse(reaction.type) { 0 }
        reactionCounts.set(reaction.type, currentCount + 1)
    }

    // removes this reaction and update the counts
    fun removeReaction(reaction: Reaction, updateCounts: Boolean = false) {
        val reactionEntity = ReactionEntity(reaction)
        val removed1 = ownReactions.remove(reactionEntity)
        val removed2 = latestReactions.remove(reactionEntity)
        if (updateCounts) {
            val shouldDecrement = removed1 || removed2 || latestReactions.size >= 15
            if (shouldDecrement) {
                val currentCount = reactionCounts.getOrElse(reaction.type) { 1 }
                reactionCounts[reaction.type] = currentCount - 1
            }
        }
    }

    /** create a messageEntity from a message object */
    constructor(m: Message) : this(m.id, m.cid, m.getUserId()) {
        text = m.text
        attachments = m.attachments
        syncStatus = m.syncStatus
        type = m.type
        replyCount = m.replyCount
        createdAt = m.createdAt
        updatedAt = m.updatedAt
        deletedAt = m.deletedAt
        parentId = m.parentId
        command = m.command
        commandInfo = m.commandInfo
        extraData = m.extraData
        reactionCounts = m.reactionCounts

        // for these we need a little map
        latestReactions = (m.latestReactions.map { ReactionEntity(it) }).toMutableList()
        ownReactions = (m.ownReactions.map { ReactionEntity(it) }).toMutableList()
        mentionedUsersId = (m.mentionedUsers.map { it.getUserId() }).toMutableList()

    }

    /** converts a message entity into a message object */
    fun toMessage(userMap: Map<String, User>): Message {
        val m = Message()
        m.id = id
        m.cid = cid
        m.user = userMap[userId]
                ?: error("userMap doesnt contain user id $userId for message id ${m.id}")
        m.text = text
        m.attachments = attachments
        m.type = type
        m.replyCount = replyCount
        m.createdAt = createdAt
        m.updatedAt = updatedAt
        m.deletedAt = deletedAt
        m.parentId = parentId
        m.command = command
        m.commandInfo = commandInfo
        m.extraData = extraData
        m.reactionCounts = reactionCounts
        m.syncStatus = syncStatus

        m.latestReactions = (latestReactions.map { it.toReaction(userMap) }).toMutableList()
        m.ownReactions = (ownReactions.map { it.toReaction(userMap) }).toMutableList()
        m.mentionedUsers = mentionedUsersId.map { userMap[it] } as MutableList<User>

        return m

    }


}