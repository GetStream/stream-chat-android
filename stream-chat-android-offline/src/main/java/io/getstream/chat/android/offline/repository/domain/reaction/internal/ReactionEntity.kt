/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.offline.repository.domain.reaction.internal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageInnerEntity
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
    tableName = REACTION_ENTITY_TABLE_NAME,
    indices = [
        Index(
            value = ["messageId", "userId", "type"],
            unique = true,
        ), Index(value = ["syncStatus"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = MessageInnerEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE,
            deferred = true,
        ),
    ],
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
    /** when the reaction was created locally */
    val createdLocallyAt: Date? = null,
    /** when the reaction was updated */
    val updatedAt: Date? = null,
    /** when the reaction was deleted, this field is only stored in the local db */
    val deletedAt: Date? = null,
    /**
     * If new reaction should replace all reactions the user has on this message.
     * Note: This is not part of the reaction object returned by the server, it is used internally when syncing offline
     * reaction.
     */
    val enforceUnique: Boolean = false,
    /**
     * If sending a push notification should be skipped for this reaction.
     * Note: This is not part of the reaction object returned by the server, it is used internally when syncing offline
     * reaction.
     */
    val skipPush: Boolean = false,
    /** optional emoji to be shown in the push notification delivered for the reaction (instead of :type:). */
    val emojiCode: String? = null,
    /** all the custom data provided for this reaction */
    val extraData: Map<String, Any>,
    /** if the reaction has been synced to the servers */
    val syncStatus: SyncStatus,
) {
    @PrimaryKey
    var id = messageId.hashCode() + userId.hashCode() + type.hashCode()
}

internal const val REACTION_ENTITY_TABLE_NAME = "stream_chat_reaction"
