package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.User

internal abstract class InitConnectionListener {

    open fun onSuccess(data: ConnectionData) {
    }

    open fun onError(error: ChatError) {
    }

    data class ConnectionData(val user: User, val connectionId: String)
}
