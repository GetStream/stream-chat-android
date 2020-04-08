package io.getstream.chat.android.client.models

interface UserEntity {

    var user: User

    fun getUserId(): String {
        return user.id
    }
}