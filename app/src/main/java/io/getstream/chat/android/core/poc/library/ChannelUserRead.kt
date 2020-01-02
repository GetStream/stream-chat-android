package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


class ChannelUserRead : UserEntity {

    @SerializedName("user")
    @Expose
    lateinit var user: User

    @SerializedName("last_read")
    @Expose
    var lastRead: Long = 0

    override fun getUserId(): String {
        return user.id
    }
}