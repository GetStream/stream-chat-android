package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName

data class MuteUserRequest(
    @SerializedName("target_id")
    val targetId: String,
    @SerializedName("user_id")
    val userId: String
)