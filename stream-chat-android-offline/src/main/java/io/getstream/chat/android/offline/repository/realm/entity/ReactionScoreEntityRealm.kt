package io.getstream.chat.android.offline.repository.realm.entity

import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject

@Suppress("VariableNaming")
internal class ReactionScoreEntityRealm : RealmObject {
    var reaction_id: String = ""
    var score: Int = 0
}

internal fun MutableMap<String, Int>.toReactionScoreRealm(): RealmList<ReactionScoreEntityRealm> =
    this.map { (id, score) ->
        ReactionScoreEntityRealm().apply {
            this.reaction_id = id
            this.score = score
        }
    }.toRealmList()

internal fun RealmList<ReactionScoreEntityRealm>.toDomain(): MutableMap<String, Int> =
    this.associateBy({ entity -> entity.reaction_id }, { entity -> entity.score }).toMutableMap()
