package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import java.util.Date

public data class Mute(
    var user: User,
    var target: User,
    @SerializedName("created_at")
    var createdAt: Date,
    @SerializedName("updated_at")
    var updatedAt: Date,
    val expires: Date?,
)
