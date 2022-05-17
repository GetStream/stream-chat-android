package io.getstream.chat.android.client.socket

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ConnectedEvent

@VisibleForTesting
internal sealed class State {
    data class Connecting(val session: Session) : State()
    data class Connected(val event: ConnectedEvent?, val session: Session) : State()

    class DisconnectedPermanently(val error: ChatNetworkError?) : State()

    data class Disconnecting(val disconnectCause: DisconnectCause) : State()
    data class Disconnected(val disconnectCause: DisconnectCause) : State()
    object Destroyed : State()

    internal fun connectionIdOrError(): String = when (this) {
        is Connected -> event?.connectionId ?: error("This state doesn't contain connectionId")
        else -> error("This state doesn't contain connectionId")
    }
}