package io.getstream.chat.android.offline.repository.domain.user

import androidx.room.ColumnInfo
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
internal data class UserEntity(
    @PrimaryKey val id: String,
    /** used for storing the current user */
    val originalId: String = "",
    @ColumnInfo(index = true)
    val name: String,
    /** the user's role */
    val role: String = "",
    /** when the user was created */
    val createdAt: Date? = null,
    /** when the user was updated */
    val updatedAt: Date? = null,
    /** last active date */
    val lastActive: Date? = null,
    /** only provided for the current user, invisible marks the user as offline for other users */
    val invisible: Boolean = false,
    /** if the current user id banned */
    val banned: Boolean = false,
    /** only provided for the current user, list of users you've muted */
    val mutes: List<String> = emptyList(),
    /** all the custom data provided for this user */
    val extraData: Map<String, Any> = emptyMap()
)
