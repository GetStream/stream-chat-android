package io.getstream.chat.android.client.api.models

internal data class BanUserRequest(
    var targetUserId: String,
    var timeout: Int?,
    var reason: String?,
    var channelType: String,
    var channelId: String,
    val shadow: Boolean,
)
