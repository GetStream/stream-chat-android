package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class RejectInviteRequest(
    val reject_invite: Boolean = true,
)
