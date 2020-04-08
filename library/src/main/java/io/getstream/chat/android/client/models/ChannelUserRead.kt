package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import java.util.*


data class ChannelUserRead(
    override var user: User,
    @SerializedName("last_read")
    var lastRead: Date? = null
) : UserEntity