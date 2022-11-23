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

import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.setup.state.ClientMutableState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.InitializationState
import io.getstream.logging.StreamLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class ClientStateImpl(private val networkStateProvider: NetworkStateProvider) : ClientMutableState {

    private val logger = StreamLog.getLogger("Chat:ClientState")

    private val _initializationState = MutableStateFlow(InitializationState.NOT_INITIALIZED)
    private val _connectionState = MutableStateFlow(ConnectionState.OFFLINE)

    override val isOnline: Boolean
        get() = _connectionState.value == ConnectionState.CONNECTED

    override val isOffline: Boolean
        get() = _connectionState.value == ConnectionState.OFFLINE

    override val isConnecting: Boolean
        get() = _connectionState.value == ConnectionState.CONNECTING

    override val isInitialized: Boolean
        get() = _initializationState.value == InitializationState.COMPLETE

    override val initializationState: StateFlow<InitializationState>
        get() = _initializationState

    override val connectionState: StateFlow<ConnectionState> = _connectionState

    override val isNetworkAvailable: Boolean
        get() = networkStateProvider.isConnected()

    override fun clearState() {
        logger.d { "[clearState] no args" }
        _initializationState.value = InitializationState.NOT_INITIALIZED
        _connectionState.value = ConnectionState.OFFLINE
    }

    override fun setConnectionState(connectionState: ConnectionState) {
        logger.d { "[setConnectionState] state: $connectionState" }
        _connectionState.value = connectionState
    }

    override fun setInitializationState(state: InitializationState) {
        _initializationState.value = state
    }
}

internal fun ClientState.toMutableState(): ClientMutableState? = this as? ClientMutableState
