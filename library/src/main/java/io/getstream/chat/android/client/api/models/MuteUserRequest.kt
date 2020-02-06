package io.getstream.chat.android.client.api.models

data class MuteUserRequest(
    val target_id: String,
    val user_id: String
)