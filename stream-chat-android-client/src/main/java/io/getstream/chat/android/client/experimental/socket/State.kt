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

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.experimental.socket.ws.OkHttpWebSocket

/**
 * @startuml
 * state SocketState {
 *  state Connecting
 *  state Connected
 *  state Disconnecting
 *  state Disconnected
 *  state Destroyed
 * }
 *
 * [*] --> Disconnected
 * Disconnected --> Connecting : Lifecycle.Started
 * Disconnected --> Destroyed : Lifecycle.Terminate
 *
 * Connecting --> Connected : WebSocket.OnConnectionOpened
 * Connecting --> Disconnected: WebSocket.Terminate
 *
 * Connected --> Disconnecting: Lifecycle.Stopped
 * Connected --> Connected : OnConnectedEventReceived
 * Connected --> Destroyed : Lifecycle.Terminate
 * Connected --> Disconnected: WebSocket.Terminate
 *
 * Disconnecting --> Disconnected: WebSocket.Terminate
 *
 * Destroyed --> [*]
 * @enduml
 */
/**
 * State of the socket connection.
 */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal sealed class State {

    /**
     * State of socket when connection is being established.
     */
    data class Connecting internal constructor(val webSocket: OkHttpWebSocket) : State()

    /**
     * State of socket when the connection is established.
     */
    data class Connected internal constructor(val event: ConnectedEvent, internal val webSocket: OkHttpWebSocket) :
        State()

    /**
     * State of socket when connection is being disconnecting.
     */
    data class Disconnecting internal constructor(val disconnectCause: DisconnectCause) : State()

    /**
     * State of socket when connection is disconnected.
     * The connection maybe established again based on [disconnectCause].
     */
    data class Disconnected internal constructor(val disconnectCause: DisconnectCause) : State()

    /**
     * State of socket after it is destroyed and won't be reconnected.
     */
    object Destroyed : State()
}
