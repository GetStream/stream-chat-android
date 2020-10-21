package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName

internal data class BanUserRequest(
    @SerializedName("target_user_id")
    var targetUserId: String,
    var timeout: Int,
    var reason: String,
    @SerializedName("type")
    var channelType: String,
    @SerializedName("id")
    var channelId: String
)
