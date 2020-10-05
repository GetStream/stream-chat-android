package io.getstream.chat.android.livedata.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

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
}
