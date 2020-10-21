package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import java.util.Date

public data class ChannelUserRead(
    override var user: User,
    @SerializedName("last_read")
    var lastRead: Date? = null,
    @SerializedName("unread_messages")
    var unreadMessages: Int = 0
) : UserEntity
