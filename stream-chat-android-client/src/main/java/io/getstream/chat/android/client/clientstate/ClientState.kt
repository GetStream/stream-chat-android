package io.getstream.chat.android.client.clientstate

internal sealed class ClientState {
    object Idle : ClientState()
    object Pending : ClientState()
    class Connected(val connectionId: String) : ClientState()
    object Disconnected : ClientState()

    internal fun connectionIdOrError(): String = when (this) {
        is Connected -> connectionId
        else -> error("This state doesn't contain connectionId")
    }
}
