package io.getstream.chat.android.offline.repository.realm.entity

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.repository.realm.utils.toDate
import io.getstream.chat.android.offline.repository.realm.utils.toRealmInstant
import io.realm.kotlin.types.RealmInstant
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
    var mutes: MutableList<String> = mutableListOf()
    var extraData: MutableMap<String, Any> = mutableMapOf()
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
        mutes = thisUser.mutes.map { mute -> mute.target.id }.toMutableList()
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
