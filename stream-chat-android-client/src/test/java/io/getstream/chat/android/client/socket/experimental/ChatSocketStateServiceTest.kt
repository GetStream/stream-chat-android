/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.socket.experimental

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.socket.ChatSocketStateService
import io.getstream.chat.android.client.socket.ChatSocketStateService.RestartReason
import io.getstream.chat.android.client.socket.ChatSocketStateService.State
import io.getstream.chat.android.client.socket.SocketFactory
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomChatNetworkError
import io.getstream.result.Error
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ChatSocketStateServiceTest {

    /**
     * Use [onReconnectArgs] as arguments
     */
    @ParameterizedTest
    @MethodSource("onReconnectArgs")
    fun `When Reconnect event arrives, should move to the proper state`(
        initialState: State,
        connectionConf: SocketFactory.ConnectionConf,
        forceReconnection: Boolean,
        resultState: State,
    ) = runTest {
        val sut = ChatSocketStateService(initialState)

        sut.onReconnect(connectionConf, forceReconnection)

        sut.currentState `should be equal to` resultState
    }

    /**
     * Use [onConnectArgs] as arguments
     */
    @ParameterizedTest
    @MethodSource("onConnectArgs")
    fun `When Connect event arrives, should move to the proper state`(
        initialState: State,
        connectionConf: SocketFactory.ConnectionConf,
        resultState: State,
    ) = runTest {
        val sut = ChatSocketStateService(initialState)

        sut.onConnect(connectionConf)

        sut.currentState `should be equal to` resultState
    }

    /**
     * Use [onNetworkNotAvailableArgs] as arguments
     */
    @ParameterizedTest
    @MethodSource("onNetworkNotAvailableArgs")
    fun `When NetworkNotAvailable event arrives, should move to the proper state`(
        initialState: State,
        resultState: State,
    ) = runTest {
        val sut = ChatSocketStateService(initialState)

        sut.onNetworkNotAvailable()

        sut.currentState `should be equal to` resultState
    }

    /**
     * Use [onNetworkAvailableArgs] as arguments
     */
    @ParameterizedTest
    @MethodSource("onNetworkAvailableArgs")
    fun `When NetworkAvailable event arrives, should move to the proper state`(
        initialState: State,
        resultState: State,
    ) = runTest {
        val sut = ChatSocketStateService(initialState)

        sut.onNetworkAvailable()

        sut.currentState `should be equal to` resultState
    }

    /**
     * Use [onConnectionEstablishedArgs] as arguments
     */
    @ParameterizedTest
    @MethodSource("onConnectionEstablishedArgs")
    fun `When Connection Established event arrives, should move to the proper state`(
        initialState: State,
        connectedEvent: ConnectedEvent,
        resultState: State,
    ) = runTest {
        val sut = ChatSocketStateService(initialState)

        sut.onConnectionEstablished(connectedEvent)

        sut.currentState `should be equal to` resultState
    }

    /**
     * Use [onUnrecoverableErrorArgs] as arguments
     */
    @ParameterizedTest
    @MethodSource("onUnrecoverableErrorArgs")
    fun `When an unrecoverable error arrives, should move to the proper state`(
        initialState: State,
        error: Error.NetworkError,
        resultState: State,
    ) = runTest {
        val sut = ChatSocketStateService(initialState)

        sut.onUnrecoverableError(error)

        sut.currentState `should be equal to` resultState
    }

    /**
     * Use [onNetworkErrorArgs] as arguments
     */
    @ParameterizedTest
    @MethodSource("onNetworkErrorArgs")
    fun `When an error arrives, should move to the proper state`(
        initialState: State,
        error: Error.NetworkError,
        resultState: State,
    ) = runTest {
        val sut = ChatSocketStateService(initialState)

        sut.onNetworkError(error)

        sut.currentState `should be equal to` resultState
    }

    /**
     * Use [onRequiredDisconnectArgs] as arguments
     */
    @ParameterizedTest
    @MethodSource("onRequiredDisconnectArgs")
    fun `When RequiredDisconnect event arrives, should move to the proper state`(
        initialState: State,
        resultState: State,
    ) = runTest {
        val sut = ChatSocketStateService(initialState)

        sut.onRequiredDisconnect()

        sut.currentState `should be equal to` resultState
    }

    /**
     * Use [onStopArgs] as arguments
     */
    @ParameterizedTest
    @MethodSource("onStopArgs")
    fun `When Stop event arrives, should move to the proper state`(
        initialState: State,
        resultState: State,
    ) = runTest {
        val sut = ChatSocketStateService(initialState)

        sut.onStop()

        sut.currentState `should be equal to` resultState
    }

    /**
     * Use [onResumeArgs] as arguments
     */
    @ParameterizedTest
    @MethodSource("onResumeArgs")
    fun `When Resume event arrives, should move to the proper state`(
        initialState: State,
        resultState: State,
    ) = runTest {
        val sut = ChatSocketStateService(initialState)

        sut.onResume()

        sut.currentState `should be equal to` resultState
    }

    /**
     * Use [onWebSocketEventLostArgs] as arguments
     */
    @ParameterizedTest
    @MethodSource("onWebSocketEventLostArgs")
    fun `When WebSocket event lost arrives, should move to the proper state`(
        initialState: State,
        resultState: State,
    ) = runTest {
        val sut = ChatSocketStateService(initialState)

        sut.onWebSocketEventLost()

        sut.currentState `should be equal to` resultState
    }

    companion object {

        @JvmStatic
        fun onNetworkAvailableArgs() = listOf(
            Arguments.of(
                State.RestartConnection(RestartReason.NETWORK_AVAILABLE),
                State.RestartConnection(RestartReason.NETWORK_AVAILABLE),
            ),
            State.Connecting(Mother.randomConnectionConf(), Mother.randomConnectionType()).let {
                Arguments.of(
                    it,
                    it,
                )
            },
            Mother.randomConnectedEvent().let {
                Arguments.of(
                    State.Connected(it),
                    State.Connected(it),
                )
            },
            Arguments.of(
                State.Disconnected.Stopped,
                State.Disconnected.Stopped,
            ),
            Arguments.of(
                State.Disconnected.NetworkDisconnected,
                State.RestartConnection(RestartReason.NETWORK_AVAILABLE),
            ),
            Arguments.of(
                State.Disconnected.WebSocketEventLost,
                State.Disconnected.WebSocketEventLost,
            ),
            Arguments.of(
                State.Disconnected.DisconnectedByRequest,
                State.Disconnected.DisconnectedByRequest,
            ),
            randomChatNetworkError().let {
                Arguments.of(
                    State.Disconnected.DisconnectedTemporarily(it),
                    State.Disconnected.DisconnectedTemporarily(it),
                )
            },
            randomChatNetworkError().let {
                Arguments.of(
                    State.Disconnected.DisconnectedPermanently(it),
                    State.Disconnected.DisconnectedPermanently(it),
                )
            },
        )

        @JvmStatic
        fun onNetworkNotAvailableArgs() = listOf(
            Arguments.of(
                State.RestartConnection(RestartReason.NETWORK_AVAILABLE),
                State.Disconnected.NetworkDisconnected,
            ),
            Arguments.of(
                State.Connecting(Mother.randomConnectionConf(), Mother.randomConnectionType()),
                State.Disconnected.NetworkDisconnected,
            ),
            Arguments.of(
                State.Connected(Mother.randomConnectedEvent()),
                State.Disconnected.NetworkDisconnected,
            ),
            Arguments.of(
                State.Disconnected.Stopped,
                State.Disconnected.Stopped,
            ),
            Arguments.of(
                State.Disconnected.NetworkDisconnected,
                State.Disconnected.NetworkDisconnected,
            ),
            Arguments.of(
                State.Disconnected.WebSocketEventLost,
                State.Disconnected.NetworkDisconnected,
            ),
            Arguments.of(
                State.Disconnected.DisconnectedByRequest,
                State.Disconnected.DisconnectedByRequest,
            ),
            Arguments.of(
                State.Disconnected.DisconnectedTemporarily(randomChatNetworkError()),
                State.Disconnected.NetworkDisconnected,
            ),
            randomChatNetworkError().let {
                Arguments.of(
                    State.Disconnected.DisconnectedPermanently(it),
                    State.Disconnected.DisconnectedPermanently(it),
                )
            },
        )

        @JvmStatic
        fun onRequiredDisconnectArgs() = listOf(
            Arguments.of(
                State.RestartConnection(RestartReason.NETWORK_AVAILABLE),
                State.Disconnected.DisconnectedByRequest,
            ),
            Arguments.of(
                State.Connecting(Mother.randomConnectionConf(), Mother.randomConnectionType()),
                State.Disconnected.DisconnectedByRequest,
            ),
            Arguments.of(
                State.Connected(Mother.randomConnectedEvent()),
                State.Disconnected.DisconnectedByRequest,
            ),
            Arguments.of(
                State.Disconnected.Stopped,
                State.Disconnected.DisconnectedByRequest,
            ),
            Arguments.of(
                State.Disconnected.NetworkDisconnected,
                State.Disconnected.DisconnectedByRequest,
            ),
            Arguments.of(
                State.Disconnected.WebSocketEventLost,
                State.Disconnected.DisconnectedByRequest,
            ),
            Arguments.of(
                State.Disconnected.DisconnectedByRequest,
                State.Disconnected.DisconnectedByRequest,
            ),
            Arguments.of(
                State.Disconnected.DisconnectedTemporarily(randomChatNetworkError()),
                State.Disconnected.DisconnectedByRequest,
            ),
            Arguments.of(
                State.Disconnected.DisconnectedPermanently(randomChatNetworkError()),
                State.Disconnected.DisconnectedByRequest,
            ),
        )

        @JvmStatic
        fun onStopArgs() = listOf(
            Arguments.of(
                State.RestartConnection(RestartReason.LIFECYCLE_RESUME),
                State.Disconnected.Stopped,
            ),
            Arguments.of(
                State.Connecting(Mother.randomConnectionConf(), Mother.randomConnectionType()),
                State.Disconnected.Stopped,
            ),
            Arguments.of(
                State.Connected(Mother.randomConnectedEvent()),
                State.Disconnected.Stopped,
            ),
            Arguments.of(
                State.Disconnected.Stopped,
                State.Disconnected.Stopped,
            ),
            Arguments.of(
                State.Disconnected.NetworkDisconnected,
                State.Disconnected.Stopped,
            ),
            Arguments.of(
                State.Disconnected.WebSocketEventLost,
                State.Disconnected.Stopped,
            ),
            Arguments.of(
                State.Disconnected.DisconnectedByRequest,
                State.Disconnected.DisconnectedByRequest,
            ),
            Arguments.of(
                State.Disconnected.DisconnectedTemporarily(randomChatNetworkError()),
                State.Disconnected.Stopped,
            ),
            randomChatNetworkError().let {
                Arguments.of(
                    State.Disconnected.DisconnectedPermanently(it),
                    State.Disconnected.DisconnectedPermanently(it),
                )
            },
        )

        @JvmStatic
        fun onResumeArgs() = listOf(
            Arguments.of(
                State.RestartConnection(RestartReason.LIFECYCLE_RESUME),
                State.RestartConnection(RestartReason.LIFECYCLE_RESUME),
            ),
            State.Connecting(Mother.randomConnectionConf(), Mother.randomConnectionType()).let {
                Arguments.of(
                    it,
                    it,
                )
            },
            Mother.randomConnectedEvent().let {
                Arguments.of(
                    State.Connected(it),
                    State.Connected(it),
                )
            },
            Arguments.of(
                State.Disconnected.Stopped,
                State.RestartConnection(RestartReason.LIFECYCLE_RESUME),
            ),
            Arguments.of(
                State.Disconnected.NetworkDisconnected,
                State.Disconnected.NetworkDisconnected,
            ),
            Arguments.of(
                State.Disconnected.WebSocketEventLost,
                State.Disconnected.WebSocketEventLost,
            ),
            Arguments.of(
                State.Disconnected.DisconnectedByRequest,
                State.Disconnected.DisconnectedByRequest,
            ),
            randomChatNetworkError().let {
                Arguments.of(
                    State.Disconnected.DisconnectedTemporarily(it),
                    State.Disconnected.DisconnectedTemporarily(it),
                )
            },
            randomChatNetworkError().let {
                Arguments.of(
                    State.Disconnected.DisconnectedPermanently(it),
                    State.Disconnected.DisconnectedPermanently(it),
                )
            },
        )

        @JvmStatic
        fun onWebSocketEventLostArgs() = listOf(
            Arguments.of(
                State.RestartConnection(RestartReason.NETWORK_AVAILABLE),
                State.Disconnected.WebSocketEventLost,
            ),
            Arguments.of(
                State.Connecting(Mother.randomConnectionConf(), Mother.randomConnectionType()),
                State.Disconnected.WebSocketEventLost,
            ),
            Arguments.of(
                State.Connected(Mother.randomConnectedEvent()),
                State.Disconnected.WebSocketEventLost,
            ),
            Arguments.of(
                State.Disconnected.Stopped,
                State.Disconnected.Stopped,
            ),
            Arguments.of(
                State.Disconnected.NetworkDisconnected,
                State.Disconnected.NetworkDisconnected,
            ),
            Arguments.of(
                State.Disconnected.WebSocketEventLost,
                State.Disconnected.WebSocketEventLost,
            ),
            Arguments.of(
                State.Disconnected.DisconnectedByRequest,
                State.Disconnected.DisconnectedByRequest,
            ),
            Arguments.of(
                State.Disconnected.DisconnectedTemporarily(randomChatNetworkError()),
                State.Disconnected.WebSocketEventLost,
            ),
            randomChatNetworkError().let {
                Arguments.of(
                    State.Disconnected.DisconnectedPermanently(it),
                    State.Disconnected.DisconnectedPermanently(it),
                )
            },
        )

        @JvmStatic
        @Suppress("LongMethod")
        fun onReconnectArgs() = Mother.randomConnectionConf().let { newConnectionConf ->
            listOf(
                Arguments.of(
                    State.RestartConnection(RestartReason.NETWORK_AVAILABLE),
                    newConnectionConf,
                    true,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.FORCE_RECONNECTION),
                ),
                Arguments.of(
                    State.RestartConnection(RestartReason.NETWORK_AVAILABLE),
                    newConnectionConf,
                    false,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.AUTOMATIC_RECONNECTION),
                ),
                Arguments.of(
                    State.Connecting(Mother.randomConnectionConf(), Mother.randomConnectionType()),
                    newConnectionConf,
                    true,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.FORCE_RECONNECTION),
                ),
                Arguments.of(
                    State.Connecting(Mother.randomConnectionConf(), Mother.randomConnectionType()),
                    newConnectionConf,
                    false,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.AUTOMATIC_RECONNECTION),
                ),
                Mother.randomConnectedEvent().let {
                    Arguments.of(
                        State.Connected(it),
                        newConnectionConf,
                        randomBoolean(),
                        State.Connected(it),
                    )
                },
                Arguments.of(
                    State.Disconnected.Stopped,
                    newConnectionConf,
                    true,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.FORCE_RECONNECTION),
                ),
                Arguments.of(
                    State.Disconnected.Stopped,
                    newConnectionConf,
                    false,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.AUTOMATIC_RECONNECTION),
                ),
                Arguments.of(
                    State.Disconnected.NetworkDisconnected,
                    newConnectionConf,
                    true,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.FORCE_RECONNECTION),
                ),
                Arguments.of(
                    State.Disconnected.NetworkDisconnected,
                    newConnectionConf,
                    false,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.AUTOMATIC_RECONNECTION),
                ),
                Arguments.of(
                    State.Disconnected.WebSocketEventLost,
                    newConnectionConf,
                    true,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.FORCE_RECONNECTION),
                ),
                Arguments.of(
                    State.Disconnected.WebSocketEventLost,
                    newConnectionConf,
                    false,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.AUTOMATIC_RECONNECTION),
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedByRequest,
                    newConnectionConf,
                    true,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.FORCE_RECONNECTION),
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedByRequest,
                    newConnectionConf,
                    false,
                    State.Disconnected.DisconnectedByRequest,
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedTemporarily(randomChatNetworkError()),
                    newConnectionConf,
                    true,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.FORCE_RECONNECTION),
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedTemporarily(randomChatNetworkError()),
                    newConnectionConf,
                    false,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.AUTOMATIC_RECONNECTION),
                ),
                randomChatNetworkError().let {
                    Arguments.of(
                        State.Disconnected.DisconnectedPermanently(it),
                        newConnectionConf,
                        true,
                        State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.FORCE_RECONNECTION),
                    )
                },
                randomChatNetworkError().let {
                    Arguments.of(
                        State.Disconnected.DisconnectedPermanently(it),
                        newConnectionConf,
                        false,
                        State.Disconnected.DisconnectedPermanently(it),
                    )
                },
            )
        }

        @JvmStatic
        fun onConnectionEstablishedArgs() = Mother.randomConnectedEvent().let { connectedEvent ->
            listOf(
                Arguments.of(
                    State.RestartConnection(RestartReason.NETWORK_AVAILABLE),
                    connectedEvent,
                    State.Connected(connectedEvent),
                ),
                Arguments.of(
                    State.Connecting(Mother.randomConnectionConf(), Mother.randomConnectionType()),
                    connectedEvent,
                    State.Connected(connectedEvent),
                ),
                Mother.randomConnectedEvent().let {
                    Arguments.of(
                        State.Connected(it),
                        connectedEvent,
                        State.Connected(connectedEvent),
                    )
                },
                Arguments.of(
                    State.Disconnected.Stopped,
                    connectedEvent,
                    State.Disconnected.Stopped,
                ),
                Arguments.of(
                    State.Disconnected.NetworkDisconnected,
                    connectedEvent,
                    State.Connected(connectedEvent),
                ),
                Arguments.of(
                    State.Disconnected.WebSocketEventLost,
                    connectedEvent,
                    State.Connected(connectedEvent),
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedByRequest,
                    connectedEvent,
                    State.Disconnected.DisconnectedByRequest,
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedTemporarily(randomChatNetworkError()),
                    connectedEvent,
                    State.Connected(connectedEvent),
                ),
                randomChatNetworkError().let {
                    Arguments.of(
                        State.Disconnected.DisconnectedPermanently(it),
                        connectedEvent,
                        State.Disconnected.DisconnectedPermanently(it),
                    )
                },
            )
        }

        @JvmStatic
        fun onUnrecoverableErrorArgs() = randomChatNetworkError().let { error ->
            listOf(
                Arguments.of(
                    State.RestartConnection(RestartReason.NETWORK_AVAILABLE),
                    error,
                    State.Disconnected.DisconnectedPermanently(error),
                ),
                Arguments.of(
                    State.Connecting(Mother.randomConnectionConf(), Mother.randomConnectionType()),
                    error,
                    State.Disconnected.DisconnectedPermanently(error),
                ),
                Arguments.of(
                    State.Connected(Mother.randomConnectedEvent()),
                    error,
                    State.Disconnected.DisconnectedPermanently(error),
                ),
                Arguments.of(
                    State.Disconnected.Stopped,
                    error,
                    State.Disconnected.Stopped,
                ),
                Arguments.of(
                    State.Disconnected.NetworkDisconnected,
                    error,
                    State.Disconnected.DisconnectedPermanently(error),
                ),
                Arguments.of(
                    State.Disconnected.WebSocketEventLost,
                    error,
                    State.Disconnected.DisconnectedPermanently(error),
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedByRequest,
                    error,
                    State.Disconnected.DisconnectedByRequest,
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedTemporarily(randomChatNetworkError()),
                    error,
                    State.Disconnected.DisconnectedPermanently(error),
                ),
                randomChatNetworkError().let {
                    Arguments.of(
                        State.Disconnected.DisconnectedPermanently(it),
                        error,
                        State.Disconnected.DisconnectedPermanently(it),
                    )
                },
            )
        }

        @JvmStatic
        fun onNetworkErrorArgs() = randomChatNetworkError().let { error ->
            listOf(
                Arguments.of(
                    State.RestartConnection(RestartReason.NETWORK_AVAILABLE),
                    error,
                    State.Disconnected.DisconnectedTemporarily(error),
                ),
                Arguments.of(
                    State.Connecting(Mother.randomConnectionConf(), Mother.randomConnectionType()),
                    error,
                    State.Disconnected.DisconnectedTemporarily(error),
                ),
                Arguments.of(
                    State.Connected(Mother.randomConnectedEvent()),
                    error,
                    State.Disconnected.DisconnectedTemporarily(error),
                ),
                Arguments.of(
                    State.Disconnected.Stopped,
                    error,
                    State.Disconnected.Stopped,
                ),
                Arguments.of(
                    State.Disconnected.NetworkDisconnected,
                    error,
                    State.Disconnected.DisconnectedTemporarily(error),
                ),
                Arguments.of(
                    State.Disconnected.WebSocketEventLost,
                    error,
                    State.Disconnected.DisconnectedTemporarily(error),
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedByRequest,
                    error,
                    State.Disconnected.DisconnectedByRequest,
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedTemporarily(randomChatNetworkError()),
                    error,
                    State.Disconnected.DisconnectedTemporarily(error),
                ),
                randomChatNetworkError().let {
                    Arguments.of(
                        State.Disconnected.DisconnectedPermanently(it),
                        error,
                        State.Disconnected.DisconnectedPermanently(it),
                    )
                },
            )
        }

        @JvmStatic
        fun onConnectArgs() = Mother.randomConnectionConf().let { newConnectionConf ->
            listOf(
                Arguments.of(
                    State.RestartConnection(RestartReason.NETWORK_AVAILABLE),
                    newConnectionConf,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.INITIAL_CONNECTION),
                ),
                Arguments.of(
                    State.Connecting(Mother.randomConnectionConf(), Mother.randomConnectionType()),
                    newConnectionConf,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.INITIAL_CONNECTION),
                ),
                Mother.randomConnectedEvent().let {
                    Arguments.of(
                        State.Connected(it),
                        newConnectionConf,
                        State.Connected(it),
                    )
                },
                Arguments.of(
                    State.Disconnected.Stopped,
                    newConnectionConf,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.INITIAL_CONNECTION),
                ),
                Arguments.of(
                    State.Disconnected.NetworkDisconnected,
                    newConnectionConf,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.INITIAL_CONNECTION),
                ),
                Arguments.of(
                    State.Disconnected.WebSocketEventLost,
                    newConnectionConf,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.INITIAL_CONNECTION),
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedByRequest,
                    newConnectionConf,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.INITIAL_CONNECTION),
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedTemporarily(randomChatNetworkError()),
                    newConnectionConf,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.INITIAL_CONNECTION),
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedPermanently(randomChatNetworkError()),
                    newConnectionConf,
                    State.Connecting(newConnectionConf, ChatSocketStateService.ConnectionType.INITIAL_CONNECTION),
                ),
            )
        }
    }
}
