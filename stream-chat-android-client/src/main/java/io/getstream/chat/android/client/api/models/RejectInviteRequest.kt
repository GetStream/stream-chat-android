package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName

internal data class RejectInviteRequest(
    @SerializedName("reject_invite")
    val rejectInvite: Boolean = true
)
