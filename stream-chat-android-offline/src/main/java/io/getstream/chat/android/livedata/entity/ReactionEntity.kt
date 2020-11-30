package io.getstream.chat.android.livedata.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

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
@Entity(
    tableName = "stream_chat_reaction",
    indices = [
        Index(
            value = ["messageId", "userId", "type"],
            unique = true
        ), Index(value = ["syncStatus"])
    ]
)
internal data class ReactionEntity(@PrimaryKey var messageId: String, var userId: String, var type: String) {

    /** the score, used if you want to allow users to clap/like etc multiple times */
    var score: Int = 1
    /** when the reaction was created */
    var createdAt: Date? = null
    /** when the reaction was updated */
    var updatedAt: Date? = null
    /** when the reaction was deleted, this field is only stored in the local db */
    var deletedAt: Date? = null
    /** all the custom data provided for this reaction */
    var extraData = mutableMapOf<String, Any>()

    /** if the reaction has been synced to the servers */
    var syncStatus: SyncStatus = SyncStatus.COMPLETED

    /** create a reactionEntity from a reaction object */
    @Suppress("USELESS_ELVIS")
    constructor(r: Reaction) : this(r.messageId, r.fetchUserId(), r.type) {
        score = r.score
        createdAt = r.createdAt
        updatedAt = r.updatedAt
        // defend against GSON unsafe decoding/encoding
        extraData = r.extraData ?: mutableMapOf()
        syncStatus = r.syncStatus ?: SyncStatus.COMPLETED
    }

    /** converts a reaction entity into a Reaction */
    @Suppress("USELESS_ELVIS")
    fun toReaction(userMap: Map<String, User>): Reaction {
        val r = Reaction(messageId, type, score)
        r.userId = userId
        r.user = userMap[userId] ?: error("userMap is missing the user for this reaction")
        r.extraData = extraData ?: mutableMapOf()
        r.createdAt = createdAt
        r.updatedAt = updatedAt
        r.syncStatus = syncStatus ?: SyncStatus.COMPLETED

        return r
    }
}
