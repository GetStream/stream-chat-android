package io.getstream.chat.android.core.poc.library

class ClientState {

    var user: User? = null
    var connectionId: String? = null

    fun reset() {
        user = null
        connectionId = null
    }

    fun getUser(id: String): User {
        return null!!
    }
}