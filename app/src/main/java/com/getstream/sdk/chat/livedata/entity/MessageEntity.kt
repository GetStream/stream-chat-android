package com.getstream.sdk.chat.livedata.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.getstream.sdk.chat.livedata.SyncStatus
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
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
    var text: String? = null

    /** the list of attachments */
    var attachments: MutableList<Attachment>? = null

    /** message type can be system, regular or ephemeral */
    var type: String? = null

    /** if the message has been synced to the servers */
    var syncStatus: SyncStatus? = null

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



    fun addReaction(reaction: Reaction) {
        val reactionEntity = ReactionEntity(reaction)

        // add to own reactions
        ownReactions.add(reactionEntity)

        // add to latest reactions
        latestReactions.add(reactionEntity)

        // update the count
        val currentCount = reactionCounts.getOrDefault(reaction.type, 0)
        reactionCounts.set(reaction.type, currentCount + 1)
    }

    /** create a messageEntity from a message object */
    constructor(m: Message): this(m.id, m.cid, m.getUserId()) {
        // TODO: Implement this
    }

    /** converts a message entity into a message object */
    fun toMessage(): Message {
        val m = Message()
        m.id = id
        m.cid = cid
        // TODO: implement me

        return m

    }
}