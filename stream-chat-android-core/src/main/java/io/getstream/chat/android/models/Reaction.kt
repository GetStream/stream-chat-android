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

import java.util.Date

public data class Reaction(
    val messageId: String = "",
    val type: String = "",
    val score: Int = 0,
    val user: User? = null,
    val userId: String = "",
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val deletedAt: Date? = null,
    val syncStatus: SyncStatus = SyncStatus.COMPLETED,
    override val extraData: Map<String, Any> = mapOf(),
    val enforceUnique: Boolean = false,
) : CustomObject {

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
        private var updatedAt: Date? = null
        private var deletedAt: Date? = null
        private var syncStatus: SyncStatus = SyncStatus.COMPLETED
        private var extraData: Map<String, Any> = mapOf()
        private var enforceUnique: Boolean = false

        public constructor(reaction: Reaction) : this() {
            messageId = reaction.messageId
            type = reaction.type
            score = reaction.score
            user = reaction.user
            userId = reaction.userId
            createdAt = reaction.createdAt
            updatedAt = reaction.updatedAt
            deletedAt = reaction.deletedAt
            syncStatus = reaction.syncStatus
            extraData = reaction.extraData
            enforceUnique = reaction.enforceUnique
        }
        public fun messageId(messageId: String): Builder = apply { this.messageId = messageId }
        public fun withMessageId(messageId: String): Builder = apply { this.messageId = messageId }
        public fun withType(type: String): Builder = apply { this.type = type }
        public fun withScore(score: Int): Builder = apply { this.score = score }
        public fun withUser(user: User?): Builder = apply { this.user = user }
        public fun withUserId(userId: String): Builder = apply { this.userId = userId }
        public fun withCreatedAt(createdAt: Date?): Builder = apply { this.createdAt = createdAt }
        public fun withUpdatedAt(updatedAt: Date?): Builder = apply { this.updatedAt = updatedAt }
        public fun withDeletedAt(deletedAt: Date?): Builder = apply { this.deletedAt = deletedAt }
        public fun withSyncStatus(syncStatus: SyncStatus): Builder = apply { this.syncStatus = syncStatus }
        public fun withExtraData(extraData: Map<String, Any>): Builder = apply { this.extraData = extraData }
        public fun withEnforceUnique(enforceUnique: Boolean): Builder = apply { this.enforceUnique = enforceUnique }

        public fun build(): Reaction {
            return Reaction(
                messageId = messageId,
                type = type,
                score = score,
                user = user,
                userId = userId,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt,
                syncStatus = syncStatus,
                extraData = extraData.toMutableMap(),
                enforceUnique = enforceUnique,
            )
        }
    }
}
