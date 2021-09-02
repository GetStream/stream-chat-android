package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User

internal data class MuteUserResponse(
    var mute: Mute,
    var ownUser: User,
)
