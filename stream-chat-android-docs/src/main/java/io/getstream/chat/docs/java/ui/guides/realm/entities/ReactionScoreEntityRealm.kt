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

package io.getstream.chat.docs.java.ui.guides.realm.entities

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
