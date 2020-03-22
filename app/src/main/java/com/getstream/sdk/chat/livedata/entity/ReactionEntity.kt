package com.getstream.sdk.chat.livedata.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.getstream.sdk.chat.livedata.SyncStatus
import io.getstream.chat.android.client.models.Reaction
import java.util.*

/**
 * The ReactionEntity
 *
 * message id, user id and type are required
 * created at and score are optional. score allows you to implement
 * reactions where one user can like/clap something multiple times
 *
 * You can convert a Reaction object from the low level client to a ReactionEntity like this:
 * val reactionEntity = ReactionEntity(reaction)
 * and back:
 * reactionEntity.toUser()
 */
@Entity(tableName = "stream_chat_reaction")
data class ReactionEntity(@PrimaryKey var messageId: String, var userId: String, var type: String) {


    /** the score, used if you want to allow users to clap/like etc multiple times */
    var score: Int? = null
    /** when the reaction was created */
    var createdAt: Date? = null
    /** when the reaction was updated */
    var updatedAt: Date? = null
    /** all the custom data provided for this reaction */
    var extraData = mutableMapOf<String, Any>()

    /** if the reaction has been synced to the servers */
    var syncStatus: SyncStatus? = null

    /** create a reactionEntity from a reaction object */
    constructor(r: Reaction): this(r.messageId, r.userId, r.type) {
        score = r.score
        createdAt = r.createdAt
        extraData = r.extraData
        // TODO: do we not have updated at?
    }

    /** converts a user entity into a user */
    fun toReaction(): Reaction {
        val r = Reaction(messageId)
        r.userId = userId
        r.type = type
        // TODO: have a look at score and created at
        //r.score = score
        r.extraData = extraData
        //r.createdAt = createdAt

        return r

    }
}
