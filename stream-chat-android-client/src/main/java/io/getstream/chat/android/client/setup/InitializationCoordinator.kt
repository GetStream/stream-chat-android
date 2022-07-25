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

package io.getstream.chat.android.client.setup

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.logging.StreamLog

/**
 * Coordinates the initialization of the Chat SDK
 */
@InternalStreamChatApi
public class InitializationCoordinator private constructor() {

    private val userDisconnectedListeners: MutableList<(User?) -> Unit> = mutableListOf()

    /**
     * Listeners which will get invoked when the web socket goes through connection changes.
     */
    private val onSocketConnectionStateChangedListener: MutableList<(ConnectionState) -> Unit> = mutableListOf()

    /**
     * Adds a listener to user disconnection.
     */
    public fun addUserDisconnectedListener(listener: (User?) -> Unit) {
        userDisconnectedListeners.add(listener)
    }

    /**
     * Adds a listener used to listen to socket connection changes
     */
    public fun addSocketConnectionStateListener(listener: (ConnectionState) -> Unit) {
        onSocketConnectionStateChangedListener.add(listener)
    }

    /**
     * Removes the listeners listening to socket state changes.
     */
    public fun removeSocketConnectionStateListeners() {
        onSocketConnectionStateChangedListener.clear()
    }

    /**
     * Notifies user disconnection
     */
    internal fun userDisconnected(user: User?) {
        userDisconnectedListeners.forEach { function -> function.invoke(user) }
    }

    /**
     * Notifies when the socket connection state has been changed.
     *
     * @param chatEvent The socket state change event. Pass in only the following event types:
     * [ConnectedEvent], [ConnectingEvent], [DisconnectedEvent].
     */
    internal fun socketConnectionStateChanged(chatEvent: ChatEvent) {
        val socketConnectionState: ConnectionState = when (chatEvent) {
            is ConnectedEvent -> ConnectionState.CONNECTED
            is ConnectingEvent -> ConnectionState.CONNECTING
            is DisconnectedEvent -> ConnectionState.OFFLINE
            else -> {
                StreamLog.w("InitializationCoordinator.socketConnectionStateChanged") {
                    "Only ChatEvents of types ConnectedEvent, ConnectingEvent and DisconnectedEvent" +
                        " should be passed as arguments to this method."
                }

                return
            }
        }

        onSocketConnectionStateChangedListener.forEach { function ->
            function(socketConnectionState)
        }
    }

    public companion object {
        private var instance: InitializationCoordinator? = null

        /**
         * Gets the initialization coordinator or creates it if necessary.
         */
        public fun getOrCreate(): InitializationCoordinator =
            instance ?: create().also { instance = it }

        @VisibleForTesting
        internal fun create(): InitializationCoordinator = InitializationCoordinator()
    }
}
