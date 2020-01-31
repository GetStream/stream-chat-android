package io.getstream.chat.android.client

import com.google.gson.annotations.SerializedName
import java.util.*


class ChannelUserRead : UserEntity {

    @SerializedName("user")
    
    lateinit var user: User

    @SerializedName("last_read")
    
    var lastRead = Date()

    constructor(user: User, lastRead: Date) {
        this.user = user
        this.lastRead = lastRead
    }


    override fun getUserId(): String {
        return user.id
    }
}