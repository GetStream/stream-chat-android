package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import java.util.*


class ChannelUserRead : UserEntity {

    lateinit var user: User

    @SerializedName("last_read")
    var lastRead: Date? = null

    override fun getUserId(): String {
        return user.id
    }
}