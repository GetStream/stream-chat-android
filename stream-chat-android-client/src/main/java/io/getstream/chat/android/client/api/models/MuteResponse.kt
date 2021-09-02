package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User

internal data class MuteResponse(
    val user: User,
    val mute: Mute,
    val mutes: List<Mute>,
)
