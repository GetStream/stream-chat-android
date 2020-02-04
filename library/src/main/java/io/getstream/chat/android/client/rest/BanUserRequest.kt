package io.getstream.chat.android.client.rest

import com.google.gson.annotations.SerializedName


data class BanUserRequest(
    @SerializedName("target_user_id")
    var targetUserId: String,
    var timeout: Int,
    var reason: String,
    @SerializedName("type")
    var channelType: String,
    @SerializedName("id")
    var channelId: String
)