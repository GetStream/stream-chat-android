package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName

internal data class MuteUserRequest(
    @SerializedName("target_id")
    val targetId: String,
    @SerializedName("user_id")
    val userId: String,
    val timeout: Int?,
)
