package io.getstream.chat.android.client.clientstate

internal sealed class SocketState {
    object Idle : SocketState()
    object Pending : SocketState()
    class Connected(val connectionId: String) : SocketState()
    object Disconnected : SocketState()

    internal fun connectionIdOrError(): String = when (this) {
        is Connected -> connectionId
        else -> error("This state doesn't contain connectionId")
    }
}
