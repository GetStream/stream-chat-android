package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Watcher(
    val id: String,
    override var user: User,
    @SerializedName("created_at")
    var createdAt: Date?
) : UserEntity
