package io.getstream.chat.android.offline.repository.realm.entity

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.repository.realm.utils.toDate
import io.getstream.chat.android.offline.repository.realm.utils.toRealmInstant
import io.getstream.chat.ui.sample.realm.entity.toDomain
import io.getstream.chat.ui.sample.realm.entity.toRealm
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

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
    var extra_data: Map<String, Any> = emptyMap()
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
        userId = user_id ?: "",
        createdAt = created_at?.toDate(),
        updatedAt = updated_at?.toDate(),
        deletedAt = deleted_at?.toDate(),
        syncStatus = sync_status.toDomain(),
        extraData = extra_data.toMutableMap(),
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
        this.extra_data = thisReaction.extraData
        this.enforce_unique = thisReaction.enforceUnique
        this.id = message_id.hashCode() + user_id.hashCode() + type.hashCode()
    }
}
