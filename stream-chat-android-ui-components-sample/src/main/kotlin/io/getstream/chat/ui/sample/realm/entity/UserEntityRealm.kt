package io.getstream.chat.ui.sample.realm.entity

import io.getstream.chat.android.client.models.User
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.*

internal class UserEntityRealm : RealmObject {
  @PrimaryKey
  var id: String = ""
  var originalId: String = ""
  var name: String = ""
  var image: String = ""
  var role: String = ""
  var createdAt: Date? = null
  var updatedAt: Date? = null
  var lastActive: Date? = null
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
    createdAt = thisUser.createdAt
    updatedAt = thisUser.updatedAt
    lastActive = thisUser.lastActive
    invisible = thisUser.invisible
    banned = thisUser.banned
    mutes = thisUser.mutes.map { mute -> mute.target.id }.toMutableList()
  }
}

internal fun UserEntityRealm.toModel(): User =
  User(
    id = id,
    role = role,
    name = name,
    image = image,
    invisible = invisible,
    banned = banned,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastActive = lastActive,
  )
