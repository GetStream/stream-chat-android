package io.getstream.chat.android.offline.experimental.global

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.model.ConnectionState
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import io.getstream.chat.android.offline.utils.Event
import io.getstream.chat.android.offline.utils.RetryPolicy
import kotlinx.coroutines.flow.StateFlow

internal class GlobalMutableState(private val chatDomainImpl: ChatDomainImpl) : GlobalState {

    override val user: StateFlow<User?> = chatDomainImpl.user

    override var offlineEnabled: Boolean
        get() = chatDomainImpl.offlineEnabled
        set(value) {
            chatDomainImpl.offlineEnabled = value
        }

    override var userPresence: Boolean
        get() = chatDomainImpl.userPresence
        set(value) {
            chatDomainImpl.userPresence = value
        }

    override val initialized: StateFlow<Boolean> = chatDomainImpl.initialized

    override val connectionState: StateFlow<ConnectionState> = chatDomainImpl.connectionState

    override val totalUnreadCount: StateFlow<Int> = chatDomainImpl.totalUnreadCount

    override val channelUnreadCount: StateFlow<Int> = chatDomainImpl.channelUnreadCount

    override val errorEvents: StateFlow<Event<ChatError>> = chatDomainImpl.errorEvents

    override val muted: StateFlow<List<Mute>> = chatDomainImpl.muted

    override val channelMutes: StateFlow<List<ChannelMute>> = chatDomainImpl.channelMutes

    override val banned: StateFlow<Boolean> = chatDomainImpl.banned

    override val retryPolicy: RetryPolicy = chatDomainImpl.retryPolicy

    override val typingUpdates: StateFlow<TypingEvent> = chatDomainImpl.typingUpdates

    override fun isOnline(): Boolean = chatDomainImpl.isOnline()

    override fun isConnecting(): Boolean = chatDomainImpl.isConnecting()

    override fun isInitialized(): Boolean = chatDomainImpl.isInitialized()

    override fun getActiveQueries(): List<QueryChannelsController> = chatDomainImpl.getActiveQueries()

    override fun clean() = chatDomainImpl.clean()

    override fun getChannelConfig(channelType: String): Config = chatDomainImpl.getChannelConfig(channelType)
}
