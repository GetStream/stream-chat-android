package io.getstream.chat.android.client

import io.getstream.chat.android.client.models.User

public class ClientState {
    public var user: User? = null
    public var connectionId: String? = null
    public var socketConnected: Boolean = false

    public fun reset() {
        user = null
        connectionId = null
    }
}
