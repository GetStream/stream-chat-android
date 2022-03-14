package io.getstream.chat.android.livedata

import android.content.Context
import android.util.Log
import androidx.annotation.CheckResult
import androidx.lifecycle.LiveData
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
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.livedata.controller.ThreadController
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.offline.model.ConnectionState
import io.getstream.chat.android.offline.ChatDomain as OfflineChatDomain
import io.getstream.chat.android.offline.ChatDomain.Builder as OfflineChatDomainBuilder

/**
 * The ChatDomain is the main entry point for all livedata & offline operations on chat.
 */
public sealed interface ChatDomain {

    /** The current user on the chatDomain object */
    public val user: LiveData<User?>

    /** if we want to track user presence */
    public var userPresence: Boolean

    /** if the client connection has been initialized */
    public val initialized: LiveData<Boolean>

    /**
     * LiveData<ConnectionState> that indicates if we are currently online, connecting or offline
     */
    public val connectionState: LiveData<ConnectionState>

    /**
     * The total unread message count for the current user.
     * Depending on your app you'll want to show this or the channelUnreadCount
     */
    public val totalUnreadCount: LiveData<Int>

    /**
     * the number of unread channels for the current user
     */
    public val channelUnreadCount: LiveData<Int>

    /**
     * The error event livedata object is triggered when errors in the underlying components occure.
     * The following example shows how to observe these errors
     *
     *  repo.errorEvent.observe(this, EventObserver {
     *       // create a toast
     *   })
     *
     */
    public val errorEvents: LiveData<Event<ChatError>>

    /**
     * list of users that you've muted
     */
    public val muted: LiveData<List<Mute>>

    /**
     * List of channels you've muted
     */
    public val channelMutes: LiveData<List<ChannelMute>>

    /**
     * if the current user is banned or not
     */
    public val banned: LiveData<Boolean>

    /**
     * Updates about currently typing users in active channels. See [TypingEvent].
     */
    public val typingUpdates: LiveData<TypingEvent>

    public fun isOnline(): Boolean
    public fun isOffline(): Boolean
    public fun isInitialized(): Boolean
    public fun clean()
    public fun getChannelConfig(channelType: String): Config
    public fun getVersion(): String

    // region use-case functions

    // getting controllers
    /**
     * Returns a ChannelController for given cid.
     *
     * @param cid The full channel id. ie messaging:123.
     *
     * @return Executable async [Call] responsible for obtaining [ChannelController].
     *
     * @see io.getstream.chat.android.livedata.controller.ChannelController
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
     * @see io.getstream.chat.android.livedata.controller.ChannelController
     */
    @CheckResult
    public fun watchChannel(cid: String, messageLimit: Int): Call<ChannelController>

    /**
     * Returns a thread controller for the given channel and message id.
     *
     * @param cid The full channel id. ie messaging:123.
     * @param parentId The message id for the parent of this thread.
     *
     * @return Executable async [Call] responsible for obtaining [ThreadController].
     *
     * @see io.getstream.chat.android.livedata.controller.ThreadController
     */
    @CheckResult
    public fun getThread(cid: String, parentId: String): Call<ThreadController>

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
     * Loads message for a given message id and channel id
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
     * Loads more messages for the specified thread.
     *
     * @param cid The full channel id i. e. messaging:123.
     * @param parentId The parentId of the thread.
     * @param messageLimit How many new messages to load.
     *
     * @return Executable async [Call] responsible for loading more messages in a thread.
     */
    @CheckResult
    public fun threadLoadMore(cid: String, parentId: String, messageLimit: Int): Call<List<Message>>

    // updating channel data

    /**
     * Performs giphy shuffle operation. Removes the original "ephemeral" message from local storage.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain.
     *
     * @param message The message to send.
     *
     * @return Executable async [Call] responsible for shuffling Giphy image.
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    @CheckResult
    public fun shuffleGiphy(message: Message): Call<Message>

    /**
     * Sends selected giphy message to the channel. Removes the original "ephemeral" message from local storage.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain.
     *
     * @param message The message to send.
     *
     * @return Executable async [Call] responsible for sending Giphy.
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
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
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
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
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    @CheckResult
    public fun sendReaction(cid: String, reaction: Reaction, enforceUnique: Boolean): Call<Reaction>

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
     * Deletes the specified reaction, request is retried according to the retry policy specified on the chatDomain.
     *
     * @param cid The full channel id, ie messaging:123.
     * @param reaction The reaction to mark as deleted.
     *
     * @return Executable async [Call] responsible for deleting reaction.
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    @CheckResult
    public fun deleteReaction(cid: String, reaction: Reaction): Call<Message>

    // end region

    public data class Builder(
        private val appContext: Context,
        private val client: ChatClient,
    ) {

        public constructor(client: ChatClient, appContext: Context) : this(appContext, client)

        private val offlineChatDomainBuilder: OfflineChatDomainBuilder = OfflineChatDomainBuilder(appContext, client)

        public fun enableBackgroundSync(): Builder = apply {
            offlineChatDomainBuilder.enableBackgroundSync()
        }

        public fun disableBackgroundSync(): Builder = apply {
            offlineChatDomainBuilder.disableBackgroundSync()
        }

        public fun recoveryEnabled(): Builder = apply {
            offlineChatDomainBuilder.recoveryEnabled()
        }

        public fun recoveryDisabled(): Builder = apply {
            offlineChatDomainBuilder.recoveryDisabled()
        }

        public fun userPresenceEnabled(): Builder = apply {
            offlineChatDomainBuilder.userPresenceEnabled()
        }

        public fun userPresenceDisabled(): Builder = apply {
            offlineChatDomainBuilder.userPresenceDisabled()
        }

        public fun build(): ChatDomain {
            instance?.run {
                Log.e(
                    "Chat",
                    "[ERROR] You have just re-initialized ChatDomain, old configuration has been overridden [ERROR]"
                )
            }
            val offlineChatDomain = offlineChatDomainBuilder.build()
            instance = buildImpl(offlineChatDomain)

            return instance()
        }

        internal fun buildImpl(offlineChatDomainBuilder: OfflineChatDomain): ChatDomainImpl {
            return ChatDomainImpl(offlineChatDomainBuilder)
        }
    }

    public companion object {
        private var instance: ChatDomain? = null

        @JvmStatic
        public fun instance(): ChatDomain = instance
            ?: throw IllegalStateException("ChatDomain.Builder::build() must be called before obtaining ChatDomain instance")

        public val isInitialized: Boolean
            get() = instance != null
    }
}
