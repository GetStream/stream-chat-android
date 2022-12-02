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

package io.getstream.realm.repository

import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.SyncStatus
import io.getstream.realm.entity.ReactionEntityRealm
import io.getstream.realm.entity.toDomain
import io.getstream.realm.entity.toRealm
import io.getstream.realm.utils.toRealmInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import java.util.Date

internal class RealmReactionRepository(private val realm: Realm) : ReactionRepository {

    override suspend fun insertReaction(reaction: Reaction) {
        realm.writeBlocking {
            copyToRealm(reaction.toRealm(), UpdatePolicy.ALL)
        }
    }

    override suspend fun updateReactionsForMessageByDeletedDate(userId: String, messageId: String, deletedAt: Date) {
        val query = "message_id == '$messageId' AND user_id == '$userId' AND deleted_at == $0"
        val entities = realm.query<ReactionEntityRealm>(query, deletedAt.toRealmInstant()).find()

        realm.writeBlocking {
            entities.forEach { reactionEntity ->
                reactionEntity.deleted_at = deletedAt.toRealmInstant()
                copyToRealm(reactionEntity, UpdatePolicy.ALL)
            }
        }
    }

    override suspend fun selectReactionById(id: Int): Reaction? =
        realm.query<ReactionEntityRealm>("id == '$id'")
            .first()
            .find()
            ?.toDomain()

    override suspend fun selectReactionsByIds(ids: List<Int>): List<Reaction> {
        val idsList = ids.joinToString(prefix = "{ ", postfix = " }")

        return realm.query<ReactionEntityRealm>("id IN $idsList")
            .find()
            .map { reactionEntity -> reactionEntity.toDomain() }
    }

    override suspend fun selectReactionIdsBySyncStatus(syncStatus: SyncStatus): List<Int> =
        realm.query<ReactionEntityRealm>("sync_status == ${syncStatus.status}")
            .find()
            .map { reactionEntity -> reactionEntity.id }

    override suspend fun selectReactionsBySyncStatus(syncStatus: SyncStatus): List<Reaction> =
        realm.query<ReactionEntityRealm>("sync_status == ${syncStatus.status}")
            .find()
            .map { reactionEntity -> reactionEntity.toDomain() }

    override suspend fun selectUserReactionToMessage(
        reactionType: String,
        messageId: String,
        userId: String,
    ): Reaction? {
        val query = "type == '$reactionType' AND message_id == '$messageId' AND user_id == '$userId'"

        return realm.query<ReactionEntityRealm>(query)
            .first()
            .find()
            ?.toDomain()
    }

    override suspend fun selectUserReactionsToMessage(messageId: String, userId: String): List<Reaction> {
        val query = "message_id == '$messageId' AND user_id == '$userId'"

        return realm.query<ReactionEntityRealm>(query)
            .find()
            .map { reactionEntity -> reactionEntity.toDomain() }
    }

    override suspend fun clear() {
        val allReactions = realm.query<ReactionEntityRealm>().find()

        realm.write {
            delete(allReactions)
        }
    }
}
