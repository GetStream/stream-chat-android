package io.getstream.chat.android.core.poc.library.rest

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class BanUserRequest(
    @SerializedName("target_user_id")
    @Expose
    var targetUserId: String,

    @SerializedName("timeout")
    @Expose
    var timeout: Int? = null,

    @SerializedName("reason")
    @Expose
    var reason: String? = null,

    @Expose
    @SerializedName("type")
    var channelType: String? = null,

    @SerializedName("id")
    var channelId: String? = null
)