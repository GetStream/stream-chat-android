package io.getstream.chat.android.livedata.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
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
    /** if new reaction should replace all reactions the user has on this message */
    var enforceUnique: Boolean = false
    /** all the custom data provided for this reaction */
    var extraData = mutableMapOf<String, Any>()

    /** if the reaction has been synced to the servers */
    var syncStatus: SyncStatus = SyncStatus.COMPLETED
}
