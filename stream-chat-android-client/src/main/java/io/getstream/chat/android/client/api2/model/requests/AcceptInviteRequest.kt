package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class AcceptInviteRequest(
    val user: AcceptInviteUser,
    val message: AcceptInviteMessage,
    val accept_invite: Boolean = true,
) {
    @JsonClass(generateAdapter = true)
    data class AcceptInviteUser(val id: String)

    @JsonClass(generateAdapter = true)
    data class AcceptInviteMessage(val text: String?)

    companion object {
        fun create(userId: String, message: String?): AcceptInviteRequest {
            return AcceptInviteRequest(
                user = AcceptInviteUser(userId),
                message = AcceptInviteMessage(message),
            )
        }
    }
}
