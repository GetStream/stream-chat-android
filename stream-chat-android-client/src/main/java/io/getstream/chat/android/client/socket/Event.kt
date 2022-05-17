package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.events.ConnectedEvent

/**
 * Events
 */
internal sealed class Event {
    sealed class Lifecycle : Event() {

        object Started : Lifecycle()

        sealed class Stopped(val disconnectCause: DisconnectCause) : Lifecycle() {
            /**
             * Stop after sending all pending messages.
             */
            data class WithReason(
                val cause: DisconnectCause,
                val shutdownReason: ShutdownReason = ShutdownReason.GRACEFUL,
            ) : Stopped(disconnectCause = cause)

            /**
             * Stop and discard all pending messages.
             */
            data class AndAborted(val cause: DisconnectCause) : Stopped(disconnectCause = cause)
        }

        object Terminate : Lifecycle()
    }

    sealed class WebSocket : Event() {
        object Terminate : WebSocket()

        data class OnConnectionOpened<out WEB_SOCKET : Any>(val webSocket: WEB_SOCKET) : WebSocket()

        data class OnConnectedEventReceived(val connectedEvent: ConnectedEvent): WebSocket()

        data class OnMessageReceived(val message: String) : WebSocket()

        /**
         * Invoked when the peer has indicated that no more incoming messages will be transmitted.
         *
         * @property shutdownReason Reason to shutdown from the peer.
         */
        data class OnConnectionClosing(val shutdownReason: ShutdownReason) : WebSocket()

        /**
         * Invoked when both peers have indicated that no more messages will be transmitted and the connection has been
         * successfully released. No further calls to this listener will be made.
         *
         * @property shutdownReason Reason to shutdown from the peer.
         */
        data class OnConnectionClosed(val shutdownReason: ShutdownReason) : WebSocket()

        /**
         * Invoked when a web socket has been closed due to an error reading from or writing to the network. Both outgoing
         * and incoming messages may have been lost. No further calls to this listener will be made.
         *
         * @property throwable The error causing the failure.
         */
        data class OnConnectionFailed(val throwable: Throwable) : WebSocket()
    }
}

internal fun Event.Lifecycle.isStopped(): Boolean = this is Event.Lifecycle.Stopped

internal fun Event.Lifecycle.isStoppedAndAborted(): Boolean = this is Event.Lifecycle.Stopped.AndAborted