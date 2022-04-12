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

@file:Suppress("PropertyName")

package io.getstream.chat.android.offline.plugin.state.global.internal

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.model.connection.ConnectionState
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import io.getstream.chat.android.offline.utils.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class GlobalMutableState private constructor() : GlobalState {

    internal val _initialized = MutableStateFlow(false)
    internal val _connectionState = MutableStateFlow(ConnectionState.OFFLINE)
    internal val _totalUnreadCount = MutableStateFlow(0)
    internal val _channelUnreadCount = MutableStateFlow(0)
    internal val _errorEvent = MutableStateFlow(Event(ChatError()))
    internal val _banned = MutableStateFlow(false)

    internal val _mutedUsers = MutableStateFlow<List<Mute>>(emptyList())
    internal val _channelMutes = MutableStateFlow<List<ChannelMute>>(emptyList())
    internal val _typingChannels = MutableStateFlow(TypingEvent("", emptyList()))

    internal val _user = MutableStateFlow<User?>(null)

    override val user: StateFlow<User?> = _user

    override val initialized: StateFlow<Boolean> = _initialized

    override val connectionState: StateFlow<ConnectionState> = _connectionState

    override val totalUnreadCount: StateFlow<Int> = _totalUnreadCount

    override val channelUnreadCount: StateFlow<Int> = _channelUnreadCount

    override val errorEvents: StateFlow<Event<ChatError>> = _errorEvent

    override val muted: StateFlow<List<Mute>> = _mutedUsers

    override val channelMutes: StateFlow<List<ChannelMute>> = _channelMutes

    override val banned: StateFlow<Boolean> = _banned

    override val typingUpdates: StateFlow<TypingEvent> = _typingChannels

    override fun isOnline(): Boolean = _connectionState.value == ConnectionState.CONNECTED

    override fun isOffline(): Boolean = _connectionState.value == ConnectionState.OFFLINE

    override fun isConnecting(): Boolean = _connectionState.value == ConnectionState.CONNECTING

    override fun isInitialized(): Boolean {
        return _initialized.value
    }

    internal companion object {
        private var instance: GlobalMutableState? = null

        /**
         * Gets the singleton of [GlobalMutableState] or creates it in the first call.
         */
        internal fun getOrCreate(): GlobalMutableState {
            return instance ?: GlobalMutableState().also { globalState ->
                instance = globalState
            }
        }

        /**
         * Gets the current Singleton of GlobalState. If the initialization is not done yet, it returns null.
         */
        @Throws(IllegalArgumentException::class)
        internal fun get(): GlobalMutableState = requireNotNull(instance) {
            "Offline plugin must be configured in ChatClient. You must provide StreamOfflinePluginFactory as a " +
                "PluginFactory to be able to use GlobalState from the SDK"
        }

        /**
         * Creates an instance of [GlobalMutableState] with a fresh state. Please keep in mind that many instances of this class may
         * cause the SDK to present an inconsistent state.
         */
        @VisibleForTesting
        internal fun create(): GlobalMutableState = GlobalMutableState()
    }

    override fun clearState() {
        _initialized.value = false
        _connectionState.value = ConnectionState.OFFLINE
        _totalUnreadCount.value = 0
        _channelUnreadCount.value = 0
        _banned.value = false

        _mutedUsers.value = emptyList()
        _channelMutes.value = emptyList()
        _user.value = null
    }
}

internal fun GlobalState.toMutableState(): GlobalMutableState = this as GlobalMutableState
