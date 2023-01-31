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

import io.getstream.chat.android.models.User
import io.getstream.chat.docs.java.ui.guides.realm.utils.toDate
import io.getstream.chat.docs.java.ui.guides.realm.utils.toRealmInstant
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.getstream.chat.docs.java.ui.guides.realm.utils.toDate
import io.getstream.chat.docs.java.ui.guides.realm.utils.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

internal class UserEntityRealm : RealmObject {
    @PrimaryKey
    var id: String = ""
    var originalId: String = ""
    var name: String = ""
    var image: String = ""
    var role: String = ""
    var createdAt: RealmInstant? = null
    var updatedAt: RealmInstant? = null
    var lastActive: RealmInstant? = null
    var invisible: Boolean = false
    var banned: Boolean = false
    var mutes: RealmList<String> = realmListOf()
}

internal fun User.toRealm(): UserEntityRealm {
    val thisUser = this

    return UserEntityRealm().apply {
        id = thisUser.id
        originalId = thisUser.id
        name = thisUser.name
        image = thisUser.image
        role = thisUser.role
        createdAt = thisUser.createdAt?.toRealmInstant()
        updatedAt = thisUser.updatedAt?.toRealmInstant()
        lastActive = thisUser.lastActive?.toRealmInstant()
        invisible = thisUser.invisible
        banned = thisUser.banned
        mutes = thisUser.mutes.map { mute -> mute.target.id }.toRealmList()
    }
}

internal fun UserEntityRealm.toDomain(): User =
    User(
        id = id,
        role = role,
        name = name,
        image = image,
        invisible = invisible,
        banned = banned,
        createdAt = createdAt?.toDate(),
        updatedAt = updatedAt?.toDate(),
        lastActive = lastActive?.toDate(),
    )
