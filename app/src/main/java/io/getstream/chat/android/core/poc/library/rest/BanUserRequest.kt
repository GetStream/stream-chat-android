package io.getstream.chat.android.core.poc.library.rest


class BanUserRequest(
    val target_user_id: String,
    val timeout: Int,
    var reason: String,
    var channelType: String,
    var channelId: String
)