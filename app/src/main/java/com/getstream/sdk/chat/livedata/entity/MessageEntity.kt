package com.getstream.sdk.chat.livedata.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
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
@Entity(tableName = "stream_chat_reaction")
data class MessageEntity(@PrimaryKey var id: String, var cid: String, var userId: String) {

    /** the message text */
    var text: String? = null

    /** the list of attachments */
    var attachments: List<Attachment>? = null

    /** message type can be system, regular or ephemeral */
    var type: String? = null

    /** if the message has been synced to the servers */
    var syncStatus: Int? = null

    /** the number of replies */
    var replyCount = 0

    /** when the message was created */
    var createdAt: Date? = null
    /** when the message was updated */
    var updatedAt: Date? = null
    /** when the message was deleted */
    var deletedAt: Date? = null

    /** the last 5 reactions on this message */
    var latestReactions: List<Reaction>? = null

    /** the reactions from the current user */
    var ownReactions: List<Reaction>? = null

    /** the users mentioned in this message */
    val mentionedUsersId: List<String>? = null

    /** a mapping between reaction type and the count, ie like:10, heart:4 */
    val reactionCounts: Map<String, Int>? = null

    /** parent id, used for threads */
    val parentId: String? = null

    /** slash command like /giphy etc */
    val command: String? = null

    /** command info */
    val commandInfo: Map<String, String>? = null

    /** all the custom data provided for this message */
    var extraData = mutableMapOf<String, Any>()

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