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

package io.getstream.chat.android.client.setup.state

import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.state.internal.ClientStateImpl
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.flow.StateFlow

/**
 * The current state of the SDK. With this class you can get the current user, the connection state, initialization
 * state...
 */
public interface ClientState {

    /**
     * The current user in the OfflinePlugin state.
     */
    public val user: StateFlow<User?>

    /**
     * If the client connection has been initialized.
     */
    public val initialized: StateFlow<Boolean>

    /**
     * StateFlow<ConnectionState> that indicates if we are currently online, connecting of offline.
     */
    public val connectionState: StateFlow<ConnectionState>

    /**
     * If the user is online or not.
     *
     * @return True if the user is online otherwise False.
     */
    public val isOnline: Boolean

    /**
     * If the user is offline or not.
     *
     * @return True if the user is offline otherwise False.
     */
    public val isOffline: Boolean

    /**
     * If connection is in connecting state.
     *
     * @return True if the connection is in connecting state.
     */
    public val isConnecting: Boolean

    /**
     * If domain state is initialized or not.
     *
     * @return True if initialized otherwise False.
     */
    public val isInitialized: Boolean

    /**
     * Clears the state of [ClientState].
     */
    public fun clearState()

    public companion object {

        private var instance: ClientState? = null

        @InternalStreamChatApi
        public fun get(): ClientState =
            instance ?: create().also { clientState ->
                instance = clientState
            }

        @InternalStreamChatApi
        public fun create(): ClientState = ClientStateImpl()
    }
}
