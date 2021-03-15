package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import java.util.Date

public data class BannedUser(
    val user: User,
    @SerializedName("banned_by")
    val bannedBy: User?,
    val channel: Channel?,
    @SerializedName("created_at")
    val createdAt: Date?,
    val expires: Date?,
    val shadow: Boolean,
    val reason: String?,
)
