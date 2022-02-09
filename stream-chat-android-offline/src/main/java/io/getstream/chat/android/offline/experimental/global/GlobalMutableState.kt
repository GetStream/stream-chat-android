@file:Suppress("PropertyName")

package io.getstream.chat.android.offline.experimental.global

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.model.ConnectionState
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

        internal fun getOrCreate(): GlobalMutableState {
            return instance ?: GlobalMutableState().also { globalState ->
                instance = globalState
            }
        }

        internal fun create(): GlobalMutableState = GlobalMutableState()
    }
}

@ExperimentalStreamChatApi
internal fun GlobalState.toMutableState(): GlobalMutableState = this as GlobalMutableState
