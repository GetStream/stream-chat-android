package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User

internal data class MuteUserResponse(
    var mute: Mute,
    @SerializedName("own_user")
    var ownUser: User
)
