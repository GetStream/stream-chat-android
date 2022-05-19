package io.getstream.chat.android.client.socket

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ConnectedEvent

/**
 * State of the socket connection.
 */
@VisibleForTesting
internal sealed class State {

    /**
     * State of socket when connection is being established.
     */
    data class Connecting(val session: Session) : State()

    /**
     * State of socket when the connection is established.
     */
    data class Connected(val event: ConnectedEvent?, val session: Session) : State()

    /**
     * State of socket when the connection is permanently disabled.
     */
    class DisconnectedPermanently(val error: ChatNetworkError?) : State()

    /**
     * State of socket when connection is being disconnecting.
     */
    data class Disconnecting(val disconnectCause: DisconnectCause) : State()

    /**
     * State of socket when connection is disconnected.
     * The connection maybe established again based on [disconnectCause].
     */
    data class Disconnected(val disconnectCause: DisconnectCause) : State()

    /**
     * State of socket after it is destroyed and won't be reconnected.
     */
    object Destroyed : State()

    /**
     * Get connection id of this connection.
     */
    internal fun connectionIdOrError(): String = when (this) {
        is Connected -> event?.connectionId ?: error("This state doesn't contain connectionId")
        else -> error("This state doesn't contain connectionId")
    }
}