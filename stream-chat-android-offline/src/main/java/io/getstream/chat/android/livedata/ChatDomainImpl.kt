package io.getstream.chat.android.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.map
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.offline.model.ConnectionState
import kotlinx.coroutines.flow.map
import io.getstream.chat.android.offline.ChatDomain as ChatDomainStateFlow

/**
 * The Chat Domain exposes livedata objects to make it easier to build your chat UI.
 * It intercepts the various low level events to ensure data stays in sync.
 * Offline storage is handled using Room
 *
 * A different Room database is used for different users. That's why it's mandatory to specify the user id when
 * initializing the ChatRepository
 *
 * chatDomain.channel(type, id) returns a controller object with channel specific livedata objects
 * chatDomain.queryChannels(query) returns a livedata object for the specific queryChannels query
 *
 * chatDomain.online livedata object indicates if you're online or not
 * chatDomain.totalUnreadCount livedata object returns the current unread count for this user
 * chatDomain.muted the list of muted users
 * chatDomain.banned if the current user is banned or not
 * chatDomain.channelUnreadCount livedata object returns the number of unread channels for this user
 * chatDomain.errorEvents events for errors that happen while interacting with the chat
 *
 */
internal class ChatDomainImpl internal constructor(private val chatDomainStateFlow: ChatDomainStateFlow) :
    ChatDomain {

    override var userPresence: Boolean
        get() = chatDomainStateFlow.userPresence
        set(value) {
            chatDomainStateFlow.userPresence = value
        }

    override val user: LiveData<User?> = chatDomainStateFlow.user.asLiveData()

    /** if the client connection has been initialized */
    override val initialized: LiveData<Boolean> = chatDomainStateFlow.initialized.asLiveData()

    /**
     * LiveData<Boolean> that indicates if we are currently online
     */
    override val connectionState: LiveData<ConnectionState> = chatDomainStateFlow.connectionState.asLiveData()

    /**
     * The total unread message count for the current user.
     * Depending on your app you'll want to show this or the channelUnreadCount.
     */
    override val totalUnreadCount: LiveData<Int> = chatDomainStateFlow.totalUnreadCount.asLiveData()

    /**
     * the number of unread channels for the current user
     */
    override val channelUnreadCount: LiveData<Int> = chatDomainStateFlow.channelUnreadCount.asLiveData()

    /**
     * list of users that you've muted
     */
    override val muted: LiveData<List<Mute>> = chatDomainStateFlow.muted.asLiveData()

    /**
     * List of channels you've muted
     */
    override val channelMutes: LiveData<List<ChannelMute>> = chatDomainStateFlow.channelMutes.asLiveData()

    /**
     * if the current user is banned or not
     */
    override val banned: LiveData<Boolean> = chatDomainStateFlow.banned.asLiveData()

    /**
     * The error event livedata object is triggered when errors in the underlying components occur.
     * The following example shows how to observe these errors
     *
     *  channelController.errorEvent.observe(this) {
     *       // create a toast
     *   })
     *
     */
    override val errorEvents: LiveData<Event<ChatError>> = chatDomainStateFlow.errorEvents.map(::Event).asLiveData()
    override val typingUpdates: LiveData<TypingEvent> = chatDomainStateFlow.typingUpdates.asLiveData()

    override fun getVersion(): String = chatDomainStateFlow.getVersion()

    override fun isOnline(): Boolean = chatDomainStateFlow.isOnline()

    override fun isOffline(): Boolean = chatDomainStateFlow.isOffline()

    override fun isInitialized(): Boolean = chatDomainStateFlow.isInitialized()

    override fun getChannelConfig(channelType: String): Config = chatDomainStateFlow.getChannelConfig(channelType)

    // region use-case functions

    override fun shuffleGiphy(message: Message): Call<Message> = chatDomainStateFlow.shuffleGiphy(message)

    override fun sendGiphy(message: Message): Call<Message> = chatDomainStateFlow.sendGiphy(message)

    @Deprecated(
        message = "ChatDomain.editMessage is deprecated. Use function ChatClient::updateMessage instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().updateMessage(message)",
            imports = arrayOf("io.getstream.chat.android.client.ChatClient")
        ),
        level = DeprecationLevel.WARNING
    )

    override fun sendReaction(cid: String, reaction: Reaction, enforceUnique: Boolean): Call<Reaction> =
        chatDomainStateFlow.sendReaction(cid, reaction, enforceUnique)

    override fun deleteReaction(cid: String, reaction: Reaction): Call<Message> =
        chatDomainStateFlow.deleteReaction(cid, reaction)

    override fun markRead(cid: String): Call<Boolean> = chatDomainStateFlow.markRead(cid)
    // end region
}
