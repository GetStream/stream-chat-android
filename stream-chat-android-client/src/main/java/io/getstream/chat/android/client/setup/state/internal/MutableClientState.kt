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

package io.getstream.chat.android.client.setup.state.internal

import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.User
import io.getstream.log.taggedLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Mutable version of [ClientState]. The class makes possible to change state of the SDK. Should only be used
 * internally by the SDK.
 */
internal class MutableClientState(private val networkStateProvider: NetworkStateProvider) : ClientState {

    private val logger by taggedLogger("Chat:ClientState")

    private val _initializationState = MutableStateFlow(InitializationState.NOT_INITIALIZED)
    private val _connectionState: MutableStateFlow<ConnectionState> = MutableStateFlow(ConnectionState.Offline)
    private var _user: MutableStateFlow<User?> = MutableStateFlow(null)

    override val user: StateFlow<User?>
        get() = _user

    override val isOnline: Boolean
        get() = _connectionState.value is ConnectionState.Connected

    override val isOffline: Boolean
        get() = _connectionState.value == ConnectionState.Offline

    override val isConnecting: Boolean
        get() = _connectionState.value == ConnectionState.Connecting

    override val initializationState: StateFlow<InitializationState>
        get() = _initializationState

    override val connectionState: StateFlow<ConnectionState> = _connectionState

    override val isNetworkAvailable: Boolean
        get() = networkStateProvider.isConnected()

    /**
     * Clears the state of [ClientMutableState].
     */
    fun clearState() {
        logger.d { "[clearState] no args" }
        _initializationState.value = InitializationState.NOT_INITIALIZED
        _connectionState.value = ConnectionState.Offline
        _user.value = null
    }

    /**
     * Sets the [ConnectionState]
     *
     * @param connectionState [ConnectionState]
     */
    fun setConnectionState(connectionState: ConnectionState) {
        logger.d { "[setConnectionState] state: $connectionState" }
        _connectionState.value = connectionState
    }

    /**
     * Sets initialized
     *
     * @param state [InitializationState]
     */
    fun setInitializationState(state: InitializationState) {
        _initializationState.value = state
    }

    /**
     * Sets the current connected user.
     *
     * @param user The [User] instance that will be configured.
     */
    fun setUser(user: User) {
        _user.value = user
    }
}
