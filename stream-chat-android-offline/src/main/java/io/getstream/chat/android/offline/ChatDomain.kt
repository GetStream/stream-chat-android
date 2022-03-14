package io.getstream.chat.android.offline

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.CheckResult
import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.model.ConnectionState
import io.getstream.chat.android.offline.utils.Event
import kotlinx.coroutines.flow.StateFlow

/**
 * The ChatDomain is the main entry point for all flow & offline operations on chat.
 */
public sealed interface ChatDomain {

    /** The current user on the chatDomain object */
    public val user: StateFlow<User?>

    /** if we want to track user presence */
    public var userPresence: Boolean

    /** if the client connection has been initialized */
    public val initialized: StateFlow<Boolean>

    /**
     * StateFlow<ConnectionState> that indicates if we are currently online, connecting of offline.
     */
    public val connectionState: StateFlow<ConnectionState>

    /**
     * The total unread message count for the current user.
     * Depending on your app you'll want to show this or the channelUnreadCount
     */
    public val totalUnreadCount: StateFlow<Int>

    /**
     * the number of unread channels for the current user
     */
    public val channelUnreadCount: StateFlow<Int>

    /**
     * The error event state flow object is triggered when errors in the underlying components occur.
     * The following example shows how to observe these errors
     *
     *  repo.errorEvent.collect {
     *       // create a toast
     *   }
     */
    public val errorEvents: StateFlow<Event<ChatError>>

    /**
     * list of users that you've muted
     */
    public val muted: StateFlow<List<Mute>>

    /**
     * List of channels you've muted
     */
    public val channelMutes: StateFlow<List<ChannelMute>>

    /**
     * if the current user is banned or not
     */
    public val banned: StateFlow<Boolean>

    /**
     * Updates about currently typing users in active channels. See [TypingEvent].
     */
    public val typingUpdates: StateFlow<TypingEvent>

    @InternalStreamChatApi
    public suspend fun disconnect()
    public fun isOnline(): Boolean
    public fun isOffline(): Boolean
    public fun isConnecting(): Boolean
    public fun isInitialized(): Boolean
    public fun clean()
    public fun getChannelConfig(channelType: String): Config
    public fun getVersion(): String

    /**
     * Returns a ChannelController for given cid.
     *
     * @param cid The full channel id. ie messaging:123.
     *
     * @return Executable async [Call] responsible for obtaining [ChannelController].
     *
     * @see io.getstream.chat.android.offline.channel.ChannelController
     */
    @CheckResult
    public fun getChannelController(cid: String): Call<ChannelController>

    /**
     * Watches the given channel and returns a ChannelController.
     *
     * @param cid The full channel id. ie messaging:123.
     * @param messageLimit How many messages to load on the first request.
     *
     * @return Executable async [Call] responsible for obtaining [ChannelController].
     *
     * @see io.getstream.chat.android.offline.channel.ChannelController
     */
    @CheckResult
    public fun watchChannel(cid: String, messageLimit: Int): Call<ChannelController>

    /**
     * Loads newer messages for the channel.
     *
     * @param cid The full channel id i. e. messaging:123.
     * @param messageLimit How many new messages to load.
     *
     * @return Executable async [Call] responsible for loading new messages in a channel.
     */
    @CheckResult
    public fun loadNewerMessages(cid: String, messageLimit: Int): Call<Channel>

    /**
     * Loads message for a given message id and channel id.
     *
     * @param cid The full channel id i. e. messaging:123.
     * @param messageId The id of the message.
     * @param olderMessagesOffset How many new messages to load before the requested message.
     * @param newerMessagesOffset How many new messages to load after the requested message.
     *
     * @return Executable async [Call] responsible for loading a message.
     */
    @CheckResult
    public fun loadMessageById(
        cid: String,
        messageId: String,
        olderMessagesOffset: Int,
        newerMessagesOffset: Int,
    ): Call<Message>

    /**
     * Performs giphy shuffle operation. Removes the original "ephemeral" message from local storage.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     *
     * @param message The message to send.
     *
     * @return Executable async [Call] responsible for shuffling Giphy image.
     *
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
     */
    @CheckResult
    public fun shuffleGiphy(message: Message): Call<Message>

    /**
     * Sends selected giphy message to the channel. Removes the original "ephemeral" message from local storage.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     *
     * @param message The message to send.
     *
     * @return Executable async [Call] responsible for sending Giphy.
     *
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
     */
    @CheckResult
    public fun sendGiphy(message: Message): Call<Message>

    /**
     * Edits the specified message. Local storage is updated immediately.
     * The API request is retried according to the retry policy specified on the chatDomain.
     *
     * @param message The message to edit.
     *
     * @return Executable async [Call] responsible for editing a message.
     *
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
     */
    @CheckResult
    @Deprecated(
        message = "ChatDomain.editMessage is deprecated. Use function ChatClient::updateMessage instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().updateMessage(message)",
            imports = arrayOf("io.getstream.chat.android.client.ChatClient")
        ),
        level = DeprecationLevel.WARNING
    )
    public fun editMessage(message: Message): Call<Message>

    /**
     * Sends the reaction. Immediately adds the reaction to local storage and updates the reaction fields on the related message.
     * API call to send the reaction is retried according to the retry policy specified on the chatDomain.
     *
     * @param cid The full channel id i. e. messaging:123.
     * @param reaction The reaction to add.
     * @param enforceUnique If set to true, new reaction will replace all reactions the user has on this message.
     *
     * @return Executable async [Call] responsible for sending a reaction.
     *
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
     */
    @CheckResult
    public fun sendReaction(cid: String, reaction: Reaction, enforceUnique: Boolean): Call<Reaction>

    /**
     * Deletes the specified reaction, request is retried according to the retry policy specified on the chatDomain.
     *
     * @param cid The full channel id, ie messaging:123.
     * @param reaction The reaction to mark as deleted.
     *
     * @return Executable async [Call] responsible for deleting reaction.
     *
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
     */
    @CheckResult
    public fun deleteReaction(cid: String, reaction: Reaction): Call<Message>

    /**
     * Marks all messages of the specified channel as read.
     *
     * @param cid The full channel id i. e. messaging:123.
     *
     * @return Executable async [Call] which completes with [Result] having data equal to true if the mark read event
     * was sent or false if there was no need to mark read (i. e. the messages are already marked as read).
     */
    @CheckResult
    public fun markRead(cid: String): Call<Boolean>

    /**
     * Marks all messages on a channel as read.
     *
     * @return Executable async [Call] responsible for marking all messages as read.
     */
    @CheckResult
    public fun markAllRead(): Call<Boolean>

    /**
     * Hides the channel with the specified id.
     *
     * @param cid The full channel id i. e. messaging:123.
     * @param keepHistory Boolean, if you want to keep the history of this channel or not.
     *
     * @return Executable async [Call] responsible for hiding a channel.
     *
     * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=kotlin">Hiding a channel</a>
     */
    @CheckResult
    public fun hideChannel(cid: String, keepHistory: Boolean): Call<Unit>

    public data class Builder(
        private val appContext: Context,
        private val client: ChatClient,
    ) {

        public constructor(client: ChatClient, appContext: Context) : this(appContext, client)

        private var handler: Handler = Handler(Looper.getMainLooper())

        private var userPresence: Boolean = false
        private var storageEnabled: Boolean = true
        private var recoveryEnabled: Boolean = true
        private var backgroundSyncEnabled: Boolean = true
        private var globalMutableState = GlobalMutableState.getOrCreate()

        @VisibleForTesting
        internal fun handler(handler: Handler) = apply {
            this.handler = handler
        }

        public fun enableBackgroundSync(): Builder {
            backgroundSyncEnabled = true
            return this
        }

        public fun disableBackgroundSync(): Builder {
            backgroundSyncEnabled = false
            return this
        }

        public fun offlineDisabled(): Builder {
            this.storageEnabled = false
            return this
        }

        public fun recoveryEnabled(): Builder {
            this.recoveryEnabled = true
            return this
        }

        public fun recoveryDisabled(): Builder {
            this.recoveryEnabled = false
            return this
        }

        public fun userPresenceEnabled(): Builder {
            this.userPresence = true
            return this
        }

        public fun userPresenceDisabled(): Builder {
            this.userPresence = false
            return this
        }

        internal fun globalMutableState(globalMutableState: GlobalMutableState): Builder = apply {
            this.globalMutableState = globalMutableState
        }

        public fun build(): ChatDomain {
            instance?.run {
                Log.e(
                    "Chat",
                    "[ERROR] You have just re-initialized ChatDomain, old configuration has been overridden [ERROR]"
                )
            }
            instance = buildImpl()
            return instance()
        }

        @SuppressLint("VisibleForTests")
        internal fun buildImpl(): ChatDomainImpl {
            return ChatDomainImpl(
                client,
                handler,
                recoveryEnabled,
                userPresence,
                backgroundSyncEnabled,
                appContext,
                globalState = globalMutableState
            )
        }
    }

    public companion object {
        @VisibleForTesting
        internal var instance: ChatDomain? = null

        @JvmStatic
        public fun instance(): ChatDomain = instance
            ?: throw IllegalStateException("ChatDomain.Builder::build() must be called before obtaining ChatDomain instance")

        public val isInitialized: Boolean
            get() = instance != null
    }
}
