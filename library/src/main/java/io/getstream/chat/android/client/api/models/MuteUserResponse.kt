package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User


data class MuteUserResponse(
    var mute: Mute,
    var own_user: User
)
