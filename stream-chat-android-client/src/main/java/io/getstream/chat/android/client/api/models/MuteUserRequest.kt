package io.getstream.chat.android.client.api.models

internal data class MuteUserRequest(
    val targetId: String,
    val userId: String,
    val timeout: Int?,
)
