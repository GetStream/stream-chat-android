package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User

internal data class MuteResponse(
    @SerializedName("own_user")
    val user: User,
    @SerializedName("channel_mute")
    val mute: Mute,
    @SerializedName("channel_mutes")
    val mutes: List<Mute>
)
