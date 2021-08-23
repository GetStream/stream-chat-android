package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import java.util.Date

public data class ChannelMute(
    val user: User,
    val channel: Channel,
    @SerializedName("created_at")
    val createdAt: Date,
    @SerializedName("updated_at")
    var updatedAt: Date,
    val expires: Date?,
)
