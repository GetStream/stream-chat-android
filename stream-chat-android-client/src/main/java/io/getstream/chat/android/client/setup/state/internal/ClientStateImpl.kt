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

package io.getstream.chat.android.client.setup.state.internal

import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.InitializationState
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.setup.state.ClientMutableState
import io.getstream.chat.android.client.setup.state.ClientState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class ClientStateImpl(private val networkStateProvider: NetworkStateProvider) : ClientMutableState {

    private val _initializationState = MutableStateFlow(InitializationState.NOT_INITIALIZED)
    private val _initialized = MutableStateFlow(false)
    private val _connectionState = MutableStateFlow(ConnectionState.OFFLINE)
    private val _user = MutableStateFlow<User?>(null)

    override val user: StateFlow<User?> = _user

    override val isOnline: Boolean
        get() = _connectionState.value == ConnectionState.CONNECTED

    override val isOffline: Boolean
        get() = _connectionState.value == ConnectionState.OFFLINE

    override val isConnecting: Boolean
        get() = _connectionState.value == ConnectionState.CONNECTING

    override val isInitialized: Boolean
        get() = _initializationState.value == InitializationState.COMPLETE

    @Deprecated(
        "Use initializationState instead",
        ReplaceWith("initializationState")
    )
    override val initialized: StateFlow<Boolean> = _initialized

    override val initializationState: StateFlow<InitializationState>
        get() = _initializationState

    override val connectionState: StateFlow<ConnectionState> = _connectionState

    override val isNetworkAvailable: Boolean
        get() = networkStateProvider.isConnected()

    override fun clearState() {
        _initializationState.value = InitializationState.NOT_INITIALIZED
        _connectionState.value = ConnectionState.OFFLINE
        _user.value = null
    }

    override fun setUser(user: User) {
        _user.value = user
    }

    override fun setConnectionState(connectionState: ConnectionState) {
        _connectionState.value = connectionState
    }

    override fun setInitializionState(state: InitializationState) {
        _initializationState.value = state
        _initialized.value = state == InitializationState.COMPLETE
    }
}

internal fun ClientState.toMutableState(): ClientMutableState? = this as? ClientMutableState
