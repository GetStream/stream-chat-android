package io.getstream.chat.android.core.poc.library.rest

import io.getstream.chat.android.core.poc.library.User


data class AcceptInviteRequest(
    val user: User,
    val message: AcceptInviteMessage,
    val accept_invite: Boolean = true
) {
    data class AcceptInviteMessage(val text: String? = null)
}