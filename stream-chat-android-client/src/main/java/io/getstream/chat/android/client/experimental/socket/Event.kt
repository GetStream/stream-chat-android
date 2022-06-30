/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.experimental.socket

import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.events.ConnectedEvent

/**
 * Events
 */
internal sealed class Event {

    sealed class Lifecycle : Event() {

        object Started : Lifecycle() {
            override fun toString(): String {
                return "Event.Lifecycle.Started"
            }
        }

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

        object Terminate : Lifecycle() {
            override fun toString(): String {
                return "Event.Lifecycle.Terminate"
            }
        }
    }

    sealed class WebSocket : Event() {
        object Terminate : WebSocket() {
            override fun toString(): String {
                return "Event.WebSocket.Terminate"
            }
        }

        data class OnConnectionOpened<out WEB_SOCKET : Any>(val webSocket: WEB_SOCKET) : WebSocket()

        data class OnConnectedEventReceived(val connectedEvent: ConnectedEvent) : WebSocket()

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
         * Invoked when a web socket has been closed due to an error reading from or writing to the network.
         * Both outgoing and incoming messages may have been lost. No further calls to this listener will be made.
         *
         * @property throwable The error causing the failure.
         */
        data class OnConnectionFailed(val throwable: Throwable) : WebSocket()
    }
}

internal fun Event.Lifecycle.isStopped(): Boolean = this is Event.Lifecycle.Stopped

internal fun Event.Lifecycle.isStoppedAndAborted(): Boolean = this is Event.Lifecycle.Stopped.AndAborted
