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

package io.getstream.chat.docs.kotlin.ui.guides.realm.entities

import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.docs.kotlin.ui.guides.realm.utils.toDate
import io.getstream.chat.docs.kotlin.ui.guides.realm.utils.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Suppress("VariableNaming")
internal class ReactionEntityRealm : RealmObject {
    var message_id: String = ""
    var user_id: String = ""
    var type: String = ""
    var score: Int = 1
    var user: UserEntityRealm? = null
    var created_at: RealmInstant? = null
    var updated_at: RealmInstant? = null
    var deleted_at: RealmInstant? = null
    var enforce_unique: Boolean = false
    var sync_status: Int = SyncStatus.COMPLETED.status

    @PrimaryKey
    var id: Int = message_id.hashCode() + user_id.hashCode() + type.hashCode()
}

internal fun ReactionEntityRealm.toDomain(): Reaction =
    Reaction(
        messageId = message_id,
        type = type,
        score = score,
        user = user?.toDomain(),
        userId = user_id,
        createdAt = created_at?.toDate(),
        updatedAt = updated_at?.toDate(),
        deletedAt = deleted_at?.toDate(),
        syncStatus = sync_status.toDomain(),
        enforceUnique = enforce_unique,
    )

internal fun Reaction.toRealm(): ReactionEntityRealm {
    val thisReaction = this

    return ReactionEntityRealm().apply {
        this.message_id = thisReaction.messageId
        this.type = thisReaction.type
        this.score = thisReaction.score
        this.user = thisReaction.user?.toRealm()
        this.user_id = thisReaction.userId
        this.created_at = thisReaction.createdAt?.toRealmInstant()
        this.updated_at = thisReaction.updatedAt?.toRealmInstant()
        this.deleted_at = thisReaction.deletedAt?.toRealmInstant()
        this.sync_status = thisReaction.syncStatus.toRealm()
        this.enforce_unique = thisReaction.enforceUnique
        this.id = message_id.hashCode() + user_id.hashCode() + type.hashCode()
    }
}
