package io.getstream.chat.ui.sample.feature.channel.add

import io.getstream.chat.android.client.models.User

sealed class UserListItem {
    val id: String
        get() = when (this) {
            is Separator -> letter.toString()
            is UserItem -> userInfo.user.id
        }

    data class Separator(val letter: Char) : UserListItem()
    data class UserItem(val userInfo: UserInfo) : UserListItem()
}

data class UserInfo(val user: User, val isSelected: Boolean)
