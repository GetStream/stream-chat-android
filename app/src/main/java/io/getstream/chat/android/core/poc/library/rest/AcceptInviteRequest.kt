package io.getstream.chat.android.core.poc.library.rest

import io.getstream.chat.android.core.poc.library.User


class AcceptInviteRequest(val user: User, message: String? = null) {

    val accept_invite = true
    val message: AcceptInviteMessage?

    inner class AcceptInviteMessage(val text: String)

    init {

        if (message == null) {
            this.message = null
        } else {
            this.message = AcceptInviteMessage(message)
        }
    }
}