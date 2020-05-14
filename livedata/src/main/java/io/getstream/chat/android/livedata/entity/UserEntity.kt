package io.getstream.chat.android.livedata.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.models.User
import java.util.*

/**
 * The UserEntity, id is a required field
 * All other fields are optional and can be null
 *
 * You can convert a User object from the low level client to a UserEntity like this
 * val userEntity = UserEntity(user)
 * and back:
 * userEntity.toUser()
 */
@Entity(tableName = "stream_chat_user")
data class UserEntity(@PrimaryKey var id: String) {
    /** used for storing the current user */
    var originalId: String = ""

    /** the user's role */
    var role: String = ""
    /** when the user was created */
    var createdAt: Date? = null
    /** when the user was updated */
    var updatedAt: Date? = null
    /** last active date */
    var lastActive: Date? = null

    /** only provided for the current user, invisible marks the user as offline for other users */
    var invisible: Boolean = false

    /** if the current user id banned */
    var banned: Boolean = false

    /** only provided for the current user, list of users you've muted */
    var mutes: List<String> = mutableListOf()

    /** all the custom data provided for this user */
    var extraData = mutableMapOf<String, Any>()

    /** create a userEntity from a user object */
    constructor(user: User) : this(user.id) {
        role = user.role
        createdAt = user.createdAt
        updatedAt = user.updatedAt
        lastActive = user.lastActive
        invisible = user.invisible
        banned = user.banned
        extraData = user.extraData
        val muteList = user.mutes ?: mutableListOf()
        mutes = muteList.map { it.target.id }
    }

    /** converts a user entity into a user */
    fun toUser(): User {
        val u = User(id = this.id)
        u.role = role
        u.createdAt = createdAt
        u.updatedAt = updatedAt
        u.lastActive = lastActive
        u.invisible = invisible
        u.extraData = extraData
        u.banned = banned
        return u
    }
}
