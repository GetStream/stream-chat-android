package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.User

public abstract class InitConnectionListener {

    public open fun onSuccess(data: ConnectionData) {
    }

    public open fun onError(error: ChatError) {
    }

    public data class ConnectionData(val user: User, val connectionId: String)
}
