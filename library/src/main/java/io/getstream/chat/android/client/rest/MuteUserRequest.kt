package io.getstream.chat.android.client.rest

data class MuteUserRequest(
    val target_id: String,
    val user_id: String
)