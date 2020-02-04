package io.getstream.chat.android.client.rest

import io.getstream.chat.android.client.Mute
import io.getstream.chat.android.client.User


data class MuteUserResponse(
    var mute: Mute,
    var own_user: User
)
