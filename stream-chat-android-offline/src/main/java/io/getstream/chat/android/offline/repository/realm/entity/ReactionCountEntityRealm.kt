package io.getstream.chat.android.offline.repository.realm.entity

import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject

@Suppress("VariableNaming")
internal class ReactionCountEntityRealm : RealmObject {
    var reaction_id: String = ""
    var count: Int = 0
}

internal fun MutableMap<String, Int>.toReactionCountRealm(): RealmList<ReactionCountEntityRealm> =
    this.map { (id, count) ->
        ReactionCountEntityRealm().apply {
            this.reaction_id = id
            this.count = count
        }
    }.toRealmList()

internal fun RealmList<ReactionCountEntityRealm>.toDomain(): MutableMap<String, Int> =
    this.associateBy({ entity -> entity.reaction_id }, { entity -> entity.count }).toMutableMap()
