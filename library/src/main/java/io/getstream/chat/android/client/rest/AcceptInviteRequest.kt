package io.getstream.chat.android.client.rest

import io.getstream.chat.android.client.User


data class AcceptInviteRequest(
    val user: User,
    val message: AcceptInviteMessage,
    val accept_invite: Boolean = true
) {
    data class AcceptInviteMessage(val text: String? = null)
}