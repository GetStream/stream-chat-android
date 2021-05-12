package io.getstream.chat.android.offline.repository.domain.reaction

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.repository.domain.message.MessageInnerEntity
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
    ],
    foreignKeys = [
        ForeignKey(
            entity = MessageInnerEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE,
            deferred = true
        )
    ]
)
internal data class ReactionEntity(
    @ColumnInfo(index = true)
    val messageId: String,
    val userId: String,
    val type: String,
    /** the score, used if you want to allow users to clap/like etc multiple times */
    val score: Int = 1,
    /** when the reaction was created */
    val createdAt: Date? = null,
    /** when the reaction was updated */
    val updatedAt: Date? = null,
    /** when the reaction was deleted, this field is only stored in the local db */
    val deletedAt: Date? = null,
    /** if new reaction should replace all reactions the user has on this message */
    val enforceUnique: Boolean = false,
    /** all the custom data provided for this reaction */
    val extraData: Map<String, Any>,
    /** if the reaction has been synced to the servers */
    val syncStatus: SyncStatus,
) {
    @PrimaryKey
    var id = messageId.hashCode() + userId.hashCode() + type.hashCode()
}
