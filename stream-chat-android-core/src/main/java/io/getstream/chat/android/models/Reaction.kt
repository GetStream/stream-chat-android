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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable
import java.util.Date

/**
 * Model representing a message reaction.
 *
 * @param messageId The id of the message.
 * @param type The type of the reaction.
 * @param score The score(count) of the reaction, used if you want to allow users to clap/like etc multiple times.
 * @param user The user who sent the reaction.
 * @param userId The id of the user who sent the reaction.
 * @param createdAt The date when the reaction was created.
 * @param createdLocallyAt The date when the reaction was created locally.
 * @param updatedAt The date when the reaction was updated.
 * @param deletedAt The date when the reaction was deleted.
 * @param syncStatus The synchronization status of the reaction.
 * @param enforceUnique If true, only one reaction of this type is allowed per user.
 * IMPORTANT: Don't set this manually when creating the reaction as it will be ignored. This field is used internally
 * for offline sync and will be overridden. To enforceUnique reactions, use the parameter on relevant method instead.
 * @param skipPush If true, sending a push notification will be skipped for this reaction.
 * IMPORTANT: Don't set this manually when creating the reaction as it will be ignored. This field is used internally
 * for offline sync and will be overridden. To skip push for reactions, use the parameter on relevant method instead.
 * @param emojiCode Optional emoji to be shown in the push notification delivered for the reaction (instead of :type:).
 */
@Immutable
public data class Reaction(
    val messageId: String = "",
    val type: String = "",
    val score: Int = 0,
    val user: User? = null,
    val userId: String = "",
    val createdAt: Date? = null,
    val createdLocallyAt: Date? = null,
    val updatedAt: Date? = null,
    val deletedAt: Date? = null,
    val syncStatus: SyncStatus = SyncStatus.COMPLETED,
    override val extraData: Map<String, Any> = mapOf(),
    val enforceUnique: Boolean = false,
    val skipPush: Boolean = false,
    val emojiCode: String? = null,
) : CustomObject {

    /**
     * The unique identifier of the reaction.
     */
    val id: String
        get() = messageId + type + score + fetchUserId()

    // this is a workaround around a backend issue
    // for some reason we sometimes only get the user id and not the user object
    // this needs more investigation on the backend side of things
    public fun fetchUserId(): String {
        return user?.id ?: userId
    }

    @SinceKotlin("99999.9")
    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    public fun newBuilder(): Builder = Builder(this)

    public class Builder() {
        private var messageId: String = ""
        private var type: String = ""
        private var score: Int = 0
        private var user: User? = null
        private var userId: String = ""
        private var createdAt: Date? = null
        private var createdLocallyAt: Date? = null
        private var updatedAt: Date? = null
        private var deletedAt: Date? = null
        private var syncStatus: SyncStatus = SyncStatus.COMPLETED
        private var extraData: Map<String, Any> = mapOf()
        private var enforceUnique: Boolean = false
        private var emojiCode: String? = null

        public constructor(reaction: Reaction) : this() {
            messageId = reaction.messageId
            type = reaction.type
            score = reaction.score
            user = reaction.user
            userId = reaction.userId
            createdAt = reaction.createdAt
            createdLocallyAt = reaction.createdLocallyAt
            updatedAt = reaction.updatedAt
            deletedAt = reaction.deletedAt
            syncStatus = reaction.syncStatus
            extraData = reaction.extraData
            enforceUnique = reaction.enforceUnique
            emojiCode = reaction.emojiCode
        }

        public fun messageId(messageId: String): Builder = apply { this.messageId = messageId }
        public fun withMessageId(messageId: String): Builder = apply { this.messageId = messageId }
        public fun withType(type: String): Builder = apply { this.type = type }
        public fun withScore(score: Int): Builder = apply { this.score = score }
        public fun withUser(user: User?): Builder = apply { this.user = user }
        public fun withUserId(userId: String): Builder = apply { this.userId = userId }
        public fun withCreatedAt(createdAt: Date?): Builder = apply { this.createdAt = createdAt }
        public fun withCreatedLocallyAt(createdLocallyAt: Date?): Builder = apply {
            this.createdLocallyAt = createdLocallyAt
        }

        public fun withUpdatedAt(updatedAt: Date?): Builder = apply { this.updatedAt = updatedAt }
        public fun withDeletedAt(deletedAt: Date?): Builder = apply { this.deletedAt = deletedAt }
        public fun withSyncStatus(syncStatus: SyncStatus): Builder = apply { this.syncStatus = syncStatus }
        public fun withExtraData(extraData: Map<String, Any>): Builder = apply { this.extraData = extraData }
        public fun withEnforceUnique(enforceUnique: Boolean): Builder = apply { this.enforceUnique = enforceUnique }
        public fun withEmojiCode(emojiCode: String?): Builder = apply { this.emojiCode = emojiCode }

        public fun build(): Reaction {
            return Reaction(
                messageId = messageId,
                type = type,
                score = score,
                user = user,
                userId = userId,
                createdAt = createdAt,
                createdLocallyAt = createdLocallyAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt,
                syncStatus = syncStatus,
                extraData = extraData.toMutableMap(),
                enforceUnique = enforceUnique,
                emojiCode = emojiCode,
            )
        }
    }
}
