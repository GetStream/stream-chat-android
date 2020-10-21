package io.getstream.chat.android.client

import io.getstream.chat.android.client.models.User

internal class ClientState {
    var user: User? = null
    var connectionId: String? = null
    var socketConnected: Boolean = false

    fun reset() {
        user = null
        connectionId = null
    }
}
