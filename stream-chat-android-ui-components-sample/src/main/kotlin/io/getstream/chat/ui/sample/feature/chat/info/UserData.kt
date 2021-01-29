package io.getstream.chat.ui.sample.feature.chat.info

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name
import java.io.Serializable
import java.util.Date

data class UserData(
    val id: String,
    val name: String,
    val image: String,
    val online: Boolean,
    val createdAt: Date?,
    val lastActive: Date?,
) : Serializable

fun UserData.toUser(): User = User().also { user ->
    user.id = id
    user.name = name
    user.image = image
    user.online = online
    user.createdAt = createdAt
    user.lastActive = lastActive
}

fun User.toUserData() = UserData(
    id = id,
    name = name,
    image = image,
    online = online,
    createdAt = createdAt,
    lastActive = lastActive,
)
