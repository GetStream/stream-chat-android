package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.User

internal data class AcceptInviteRequest(
    val user: User,
    val message: AcceptInviteMessage,
    val accept_invite: Boolean = true
) {
    data class AcceptInviteMessage(val text: String? = null)
}
