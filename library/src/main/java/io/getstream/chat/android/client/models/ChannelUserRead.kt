package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.UserEntity
import io.getstream.chat.android.client.utils.UndefinedDate


class ChannelUserRead : UserEntity {

    lateinit var user: User

    @SerializedName("last_read")
    var lastRead = UndefinedDate

    override fun getUserId(): String {
        return user.id
    }
}