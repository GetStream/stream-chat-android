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

package io.getstream.chat.android.client.socket.experimental

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.socket.ChatSocketStateService
import io.getstream.chat.android.client.socket.ChatSocketStateService.State
import io.getstream.chat.android.client.socket.SocketFactory
import io.getstream.chat.android.test.randomBoolean
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
        resultState: State,
    ) {
        val sut = ChatSocketStateService(initialState)

        sut.onReconnect(connectionConf)

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
    ) {
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
    ) {
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
    ) {
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
    ) {
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
        error: ChatError.NetworkError,
        resultState: State,
    ) {
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
        error: ChatError.NetworkError,
        resultState: State,
    ) {
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
    ) {
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
    ) {
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
    ) {
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
    ) {
        val sut = ChatSocketStateService(initialState)

        sut.onWebSocketEventLost()

        sut.currentState `should be equal to` resultState
    }

    companion object {

        @JvmStatic
        fun onNetworkAvailableArgs() = listOf(
            Arguments.of(
                State.RestartConnection,
                State.RestartConnection,
            ),
            State.Connecting(Mother.randomConnectionConf(), randomBoolean()).let {
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
                State.RestartConnection,
            ),
            Arguments.of(
                State.Disconnected.WebSocketEventLost,
                State.Disconnected.WebSocketEventLost,
            ),
            Arguments.of(
                State.Disconnected.DisconnectedByRequest,
                State.Disconnected.DisconnectedByRequest,
            ),
            Mother.randomChatNetworkError().let {
                Arguments.of(
                    State.Disconnected.DisconnectedTemporarily(it),
                    State.Disconnected.DisconnectedTemporarily(it),
                )
            },
            Mother.randomChatNetworkError().let {
                Arguments.of(
                    State.Disconnected.DisconnectedPermanently(it),
                    State.Disconnected.DisconnectedPermanently(it),
                )
            },
        )

        @JvmStatic
        fun onNetworkNotAvailableArgs() = listOf(
            Arguments.of(
                State.RestartConnection,
                State.Disconnected.NetworkDisconnected,
            ),
            Arguments.of(
                State.Connecting(Mother.randomConnectionConf(), randomBoolean()),
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
                State.Disconnected.DisconnectedTemporarily(Mother.randomChatNetworkError()),
                State.Disconnected.NetworkDisconnected,
            ),
            Mother.randomChatNetworkError().let {
                Arguments.of(
                    State.Disconnected.DisconnectedPermanently(it),
                    State.Disconnected.DisconnectedPermanently(it),
                )
            },
        )

        @JvmStatic
        fun onRequiredDisconnectArgs() = listOf(
            Arguments.of(
                State.RestartConnection,
                State.Disconnected.DisconnectedByRequest,
            ),
            Arguments.of(
                State.Connecting(Mother.randomConnectionConf(), randomBoolean()),
                State.Disconnected.DisconnectedByRequest,
            ),
            Arguments.of(
                State.Connected(Mother.randomConnectedEvent()),
                State.Disconnected.DisconnectedByRequest,
            ),
            Arguments.of(
                State.Disconnected.Stopped,
                State.Disconnected.Stopped,
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
                State.Disconnected.DisconnectedTemporarily(Mother.randomChatNetworkError()),
                State.Disconnected.DisconnectedByRequest,
            ),
            Arguments.of(
                State.Disconnected.DisconnectedPermanently(Mother.randomChatNetworkError()),
                State.Disconnected.DisconnectedByRequest,
            ),
        )

        @JvmStatic
        fun onStopArgs() = listOf(
            Arguments.of(
                State.RestartConnection,
                State.Disconnected.Stopped,
            ),
            Arguments.of(
                State.Connecting(Mother.randomConnectionConf(), randomBoolean()),
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
                State.Disconnected.DisconnectedTemporarily(Mother.randomChatNetworkError()),
                State.Disconnected.Stopped,
            ),
            Mother.randomChatNetworkError().let {
                Arguments.of(
                    State.Disconnected.DisconnectedPermanently(it),
                    State.Disconnected.DisconnectedPermanently(it),
                )
            },
        )

        @JvmStatic
        fun onResumeArgs() = listOf(
            Arguments.of(
                State.RestartConnection,
                State.RestartConnection,
            ),
            State.Connecting(Mother.randomConnectionConf(), randomBoolean()).let {
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
                State.RestartConnection,
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
            Mother.randomChatNetworkError().let {
                Arguments.of(
                    State.Disconnected.DisconnectedTemporarily(it),
                    State.Disconnected.DisconnectedTemporarily(it),
                )
            },
            Mother.randomChatNetworkError().let {
                Arguments.of(
                    State.Disconnected.DisconnectedPermanently(it),
                    State.Disconnected.DisconnectedPermanently(it),
                )
            },
        )

        @JvmStatic
        fun onWebSocketEventLostArgs() = listOf(
            Arguments.of(
                State.RestartConnection,
                State.Disconnected.WebSocketEventLost,
            ),
            Arguments.of(
                State.Connecting(Mother.randomConnectionConf(), randomBoolean()),
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
                State.Disconnected.DisconnectedTemporarily(Mother.randomChatNetworkError()),
                State.Disconnected.WebSocketEventLost,
            ),
            Mother.randomChatNetworkError().let {
                Arguments.of(
                    State.Disconnected.DisconnectedPermanently(it),
                    State.Disconnected.DisconnectedPermanently(it),
                )
            },
        )

        @JvmStatic
        fun onReconnectArgs() = Mother.randomConnectionConf().let { newConnectionConf ->
            listOf(
                Arguments.of(
                    State.RestartConnection,
                    newConnectionConf,
                    State.Connecting(newConnectionConf, true)
                ),
                Arguments.of(
                    State.Connecting(Mother.randomConnectionConf(), randomBoolean()),
                    newConnectionConf,
                    State.Connecting(newConnectionConf, true),
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
                    State.Connecting(newConnectionConf, true),
                ),
                Arguments.of(
                    State.Disconnected.NetworkDisconnected,
                    newConnectionConf,
                    State.Connecting(newConnectionConf, true),
                ),
                Arguments.of(
                    State.Disconnected.WebSocketEventLost,
                    newConnectionConf,
                    State.Connecting(newConnectionConf, true),
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedByRequest,
                    newConnectionConf,
                    State.Disconnected.DisconnectedByRequest,
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedTemporarily(Mother.randomChatNetworkError()),
                    newConnectionConf,
                    State.Connecting(newConnectionConf, true),
                ),
                Mother.randomChatNetworkError().let {
                    Arguments.of(
                        State.Disconnected.DisconnectedPermanently(it),
                        newConnectionConf,
                        State.Disconnected.DisconnectedPermanently(it),
                    )
                },
            )
        }

        @JvmStatic
        fun onConnectionEstablishedArgs() = Mother.randomConnectedEvent().let { connectedEvent ->
            listOf(
                Arguments.of(
                    State.RestartConnection,
                    connectedEvent,
                    State.Connected(connectedEvent),
                ),
                Arguments.of(
                    State.Connecting(Mother.randomConnectionConf(), randomBoolean()),
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
                    State.Disconnected.DisconnectedTemporarily(Mother.randomChatNetworkError()),
                    connectedEvent,
                    State.Connected(connectedEvent),
                ),
                Mother.randomChatNetworkError().let {
                    Arguments.of(
                        State.Disconnected.DisconnectedPermanently(it),
                        connectedEvent,
                        State.Disconnected.DisconnectedPermanently(it),
                    )
                },
            )
        }

        @JvmStatic
        fun onUnrecoverableErrorArgs() = Mother.randomChatNetworkError().let { error ->
            listOf(
                Arguments.of(
                    State.RestartConnection,
                    error,
                    State.Disconnected.DisconnectedPermanently(error),
                ),
                Arguments.of(
                    State.Connecting(Mother.randomConnectionConf(), randomBoolean()),
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
                    State.Disconnected.DisconnectedTemporarily(Mother.randomChatNetworkError()),
                    error,
                    State.Disconnected.DisconnectedPermanently(error),
                ),
                Mother.randomChatNetworkError().let {
                    Arguments.of(
                        State.Disconnected.DisconnectedPermanently(it),
                        error,
                        State.Disconnected.DisconnectedPermanently(it),
                    )
                },
            )
        }

        @JvmStatic
        fun onNetworkErrorArgs() = Mother.randomChatNetworkError().let { error ->
            listOf(
                Arguments.of(
                    State.RestartConnection,
                    error,
                    State.Disconnected.DisconnectedTemporarily(error),
                ),
                Arguments.of(
                    State.Connecting(Mother.randomConnectionConf(), randomBoolean()),
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
                    State.Disconnected.DisconnectedTemporarily(Mother.randomChatNetworkError()),
                    error,
                    State.Disconnected.DisconnectedTemporarily(error),
                ),
                Mother.randomChatNetworkError().let {
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
                    State.RestartConnection,
                    newConnectionConf,
                    State.Connecting(newConnectionConf, false),
                ),
                Arguments.of(
                    State.Connecting(Mother.randomConnectionConf(), randomBoolean()),
                    newConnectionConf,
                    State.Connecting(newConnectionConf, false),
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
                    State.Connecting(newConnectionConf, false),
                ),
                Arguments.of(
                    State.Disconnected.NetworkDisconnected,
                    newConnectionConf,
                    State.Connecting(newConnectionConf, false),
                ),
                Arguments.of(
                    State.Disconnected.WebSocketEventLost,
                    newConnectionConf,
                    State.Connecting(newConnectionConf, false),
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedByRequest,
                    newConnectionConf,
                    State.Connecting(newConnectionConf, false),
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedTemporarily(Mother.randomChatNetworkError()),
                    newConnectionConf,
                    State.Connecting(newConnectionConf, false),
                ),
                Arguments.of(
                    State.Disconnected.DisconnectedPermanently(Mother.randomChatNetworkError()),
                    newConnectionConf,
                    State.Connecting(newConnectionConf, false),
                ),
            )
        }
    }
}
