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
import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import io.getstream.chat.android.offline.utils.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@InternalStreamChatApi
public class GlobalMutableState private constructor(
    override val clientState: ClientState,
) : MutableGlobalState {

    private val _totalUnreadCount = MutableStateFlow(0)
    private val _channelUnreadCount = MutableStateFlow(0)
    private val _errorEvent = MutableStateFlow(Event(ChatError()))
    private val _banned = MutableStateFlow(false)

    private val _mutedUsers = MutableStateFlow<List<Mute>>(emptyList())
    private val _channelMutes = MutableStateFlow<List<ChannelMute>>(emptyList())
    private val _typingChannels = MutableStateFlow(emptyMap<String, TypingEvent>())

    @Deprecated(
        message = "Use ClientState.user instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().clientState.user",
            imports = [
                "io.getstream.chat.android.client.ChatClient",
                "io.getstream.chat.android.offline.extensions.clientState"
            ]
        )
    )
    override val user: StateFlow<User?> = clientState.user

    @Deprecated(
        message = "Use ClientState.initialized instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().clientState.initialized",
            imports = [
                "io.getstream.chat.android.client.ChatClient",
                "io.getstream.chat.android.offline.extensions.clientState"
            ]
        )
    )
    override val initialized: StateFlow<Boolean> = clientState.initialized

    @Deprecated(
        message = "Use ClientState.connectionState instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().clientState.connectionState",
            imports = [
                "io.getstream.chat.android.client.ChatClient",
                "io.getstream.chat.android.offline.extensions.clientState"
            ]
        )
    )
    override val connectionState: StateFlow<ConnectionState> = clientState.connectionState

    override val totalUnreadCount: StateFlow<Int> = _totalUnreadCount

    override val channelUnreadCount: StateFlow<Int> = _channelUnreadCount

    override val errorEvents: StateFlow<Event<ChatError>> = _errorEvent

    override val muted: StateFlow<List<Mute>> = _mutedUsers

    override val channelMutes: StateFlow<List<ChannelMute>> = _channelMutes

    override val banned: StateFlow<Boolean> = _banned

    override val typingChannels: StateFlow<Map<String, TypingEvent>> = _typingChannels

    @Deprecated(
        message = "Use ClientState.isOnline() instead.",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().clientState.isOnline",
            imports = [
                "io.getstream.chat.android.client.ChatClient",
                "io.getstream.chat.android.offline.extensions.clientState"
            ]
        )
    )
    override fun isOnline(): Boolean = clientState.isOnline

    @Deprecated(
        message = "Use ClientState.isOffline() instead.",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().clientState.isOffline",
            imports = [
                "io.getstream.chat.android.client.ChatClient",
                "io.getstream.chat.android.offline.extensions.clientState"
            ]
        )
    )
    override fun isOffline(): Boolean = clientState.isOffline

    @Deprecated(
        message = "Use ClientState.isConnecting() instead.",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().clientState.isConnecting",
            imports = [
                "io.getstream.chat.android.client.ChatClient",
                "io.getstream.chat.android.offline.extensions.clientState"
            ]
        )
    )
    override fun isConnecting(): Boolean = clientState.isConnecting

    @Deprecated(
        message = "Use ClientState.isInitialized() instead.",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().clientState.isInitialized",
            imports = [
                "io.getstream.chat.android.client.ChatClient",
                "io.getstream.chat.android.offline.extensions.clientState"
            ]
        )
    )
    override fun isInitialized(): Boolean = clientState.isInitialized

    public companion object {
        @InternalStreamChatApi
        @VisibleForTesting
        @Volatile
        public var instance: GlobalMutableState? = null

        /**
         * Gets the singleton of [GlobalMutableState] or creates it in the first call.
         */
        @InternalStreamChatApi
        public fun get(clientState: ClientState): GlobalMutableState {
            return instance ?: synchronized(this) {
                instance ?: GlobalMutableState(clientState).also { globalState ->
                    instance = globalState
                }
            }
        }
    }

    override fun clearState() {
        _totalUnreadCount.value = 0
        _channelUnreadCount.value = 0
        _banned.value = false

        _mutedUsers.value = emptyList()
        _channelMutes.value = emptyList()
    }

    override fun setErrorEvent(errorEvent: Event<ChatError>) {
        _errorEvent.value = errorEvent
    }

    override fun setTotalUnreadCount(totalUnreadCount: Int) {
        _totalUnreadCount.value = totalUnreadCount
    }

    override fun setChannelUnreadCount(channelUnreadCount: Int) {
        _channelUnreadCount.value = channelUnreadCount
    }

    override fun setBanned(banned: Boolean) {
        _banned.value = banned
    }

    override fun setChannelMutes(channelMutes: List<ChannelMute>) {
        _channelMutes.value = channelMutes
    }

    override fun setMutedUsers(mutedUsers: List<Mute>) {
        _mutedUsers.value = mutedUsers
    }

    override fun tryEmitTypingEvent(cid: String, typingEvent: TypingEvent) {
        val typingChannelsCopy = _typingChannels.value.toMutableMap()

        if (typingEvent.users.isEmpty()) {
            typingChannelsCopy.remove(cid)
        } else {
            typingChannelsCopy[cid] = typingEvent
        }
        _typingChannels.tryEmit(typingChannelsCopy)
    }
}

internal fun GlobalState.toMutableState(): GlobalMutableState = this as GlobalMutableState
