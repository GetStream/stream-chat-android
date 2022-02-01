package io.getstream.chat.android.livedata

import android.content.Context
import android.util.Log
import androidx.annotation.CheckResult
import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.livedata.controller.QueryChannelsController
import io.getstream.chat.android.livedata.controller.ThreadController
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.livedata.utils.RetryPolicy
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.model.ConnectionState
import io.getstream.chat.android.offline.ChatDomain as OfflineChatDomain
import io.getstream.chat.android.offline.ChatDomain.Builder as OfflineChatDomainBuilder

/**
 * The ChatDomain is the main entry point for all livedata & offline operations on chat.
 */
public sealed interface ChatDomain {

    /** The current user on the chatDomain object */
    public val user: LiveData<User?>

    /** if offline is enabled */
    public var offlineEnabled: Boolean

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

    /** The retry policy for retrying failed requests */
    public val retryPolicy: RetryPolicy

    /**
     * Updates about currently typing users in active channels. See [TypingEvent].
     */
    public val typingUpdates: LiveData<TypingEvent>

    public fun isOnline(): Boolean
    public fun isOffline(): Boolean
    public fun isInitialized(): Boolean
    public fun getActiveQueries(): List<QueryChannelsController>
    public fun clean()
    public fun getChannelConfig(channelType: String): Config
    public fun getVersion(): String

    // region use-case functions

    // replaying events
    /**
     * Adds the provided channel to the active channels and replays events for all active channels.
     *
     * @return Executable async [Call] responsible for obtainging list of historical [ChatEvent] objects.
     */
    @CheckResult
    @Deprecated(
        message = "replayEventsForActiveChannels is deprecated. Use extension function ChatClient::replayEventsForActiveChannels instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().replayEventsForActiveChannels(attachment)",
            imports = arrayOf("io.getstream.chat.android.client.ChatClient")
        ),
        level = DeprecationLevel.ERROR
    )
    public fun replayEventsForActiveChannels(cid: String): Call<List<ChatEvent>>

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
     * Queries offline storage and the API for channels matching the filter.
     *
     * @param filter The filter object.
     * @param sort How to sort the channels (default is last_message_at).
     * @param limit The number of channels to retrieve.
     * @param messageLimit How many messages to retrieve per channel.
     *
     * @return Executable async [Call] responsible for obtaining [QueryChannelsController].
     *
     * @see io.getstream.chat.android.livedata.controller.QueryChannelsController
     * @see io.getstream.chat.android.client.utils.FilterObject
     * @see io.getstream.chat.android.client.api.models.QuerySort
     * @see <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin">Filter syntax</a>
     */
    @CheckResult
    public fun queryChannels(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int = 30,
        messageLimit: Int = 1,
    ): Call<QueryChannelsController>

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

    // loading more
    /**
     * Loads older messages for the channel.
     *
     * @param cid The full channel id i. e. messaging:123.
     * @param messageLimit How many new messages to load.
     *
     * @return Executable async [Call] responsible for loading older messages in a channel.
     */
    @CheckResult
    @Deprecated(
        message = "loadOlderMessages is deprecated. Use extension function ChatClient::loadOlderMessages instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().loadOlderMessages(cid, messageLimit)",
            imports = arrayOf("io.getstream.chat.android.client.ChatClient")
        ),
        level = DeprecationLevel.ERROR
    )
    public fun loadOlderMessages(cid: String, messageLimit: Int): Call<Channel>

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
     * Load more channels for this query.
     *
     * @param filter the filter for querying channels, see https://getstream.io/chat/docs/query_channels/?language=kotlin.
     * @param sort the sort for the channels, by default will sort on last_message_at.
     * @param limit the number of channels to retrieve
     * @param messageLimit how many messages to fetch per channel.
     * @param memberLimit The number of members per channel.
     *
     * @return Executable async [Call] responsible for loading more channels.
     *
     * @see io.getstream.chat.android.client.api.models.FilterObject
     * @see io.getstream.chat.android.client.api.models.QuerySort
     * @see <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin">Filter syntax</a>
     */
    @CheckResult
    public fun queryChannelsLoadMore(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int,
        messageLimit: Int,
        memberLimit: Int,
    ): Call<List<Channel>>

    /**
     * Load more channels for this query
     *
     * @param filter The filter for querying channels, see https://getstream.io/chat/docs/query_channels/?language=kotlin.
     * @param sort The sort for the channels, by default will sort on last_message_at.
     * @param messageLimit How many messages to fetch per channel.
     *
     * @return Executable async [Call] responsible for loading more channels.
     *
     * @see io.getstream.chat.android.client.api.models.FilterObject
     * @see io.getstream.chat.android.client.api.models.QuerySort
     * @see <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin">Filter syntax</a>
     */
    @CheckResult
    public fun queryChannelsLoadMore(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        messageLimit: Int,
    ): Call<List<Channel>>

    /**
     * Load more channels for this query.
     *
     * @param filter The filter for querying channels, see https://getstream.io/chat/docs/query_channels/?language=kotlin.
     * @param sort The sort for the channels, by default will sort on last_message_at.
     *
     * @return Executable async [Call] responsible for loading more channels.
     *
     * @see io.getstream.chat.android.client.api.models.FilterObject
     * @see io.getstream.chat.android.client.api.models.QuerySort
     * @see <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin">Filter syntax</a>
     */
    @CheckResult
    public fun queryChannelsLoadMore(filter: FilterObject, sort: QuerySort<Channel>): Call<List<Channel>>

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
     * Creates a new channel. Will retry according to the retry policy if it fails.
     *
     * @param channel The channel object.
     *
     * @return Executable async [Call] responsible for creating a channel.
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    @Deprecated(
        message = "createChannel is deprecated. Use ChatClient::createChannel extension function instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().createChannel(channel)",
            imports = arrayOf("io.getstream.chat.android.client.ChatClient")
        ),
    )
    @CheckResult
    public fun createChannel(channel: Channel): Call<Channel>

    /**
     * Sends the message. Immediately adds the message to local storage.
     * API call to send the message is retried according to the retry policy specified on the chatDomain.
     *
     * @param message The message to send.
     *
     * @return Executable async [Call] responsible for sending a message.
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    @CheckResult
    public fun sendMessage(message: Message): Call<Message>

    /**
     * Cancels the message of "ephemeral" type. Removes the message from local storage.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain.
     *
     * @param message The message to send.
     *
     * @return Executable async [Call] responsible for canceling ephemeral message.
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    @CheckResult
    @Deprecated(
        message = "cancelMessage is deprecated. Use extension function ChatClient::cancelMessage instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().cancelMessage(message)",
            imports = arrayOf("io.getstream.chat.android.client.ChatClient")
        ),
        level = DeprecationLevel.WARNING
    )
    public fun cancelMessage(message: Message): Call<Boolean>

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
    public fun editMessage(message: Message): Call<Message>

    /**
     * Deletes the specified message, request is retried according to the retry policy specified on the chatDomain.
     *
     * @param message The message to mark as deleted.
     *
     * @return Executable async [Call] responsible for deleting a message.
     * @param hard Use to hard delete the message (delete in backend). CAN'T BE UNDONE.
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    @CheckResult
    public fun deleteMessage(message: Message, hard: Boolean = false): Call<Message>

    public fun deleteMessage(message: Message): Call<Message>

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

    /**
     * Keystroke should be called whenever a user enters text into the message input.
     * It automatically calls stopTyping when the user stops typing after 5 seconds.
     *
     * @param cid The full channel id i. e. messaging:123.
     * @param parentId Set this field to `message.id` to indicate that typing event is happening in a thread.
     *
     * @return Executable async [Call] which completes with [Result] having data true when a typing event was sent, false if it wasn't sent.
     */
    @CheckResult
    @Deprecated(
        message = "keystroke is deprecated. Use extension function ChatClient::keystroke instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().keystroke(cid, parentId)",
            imports = arrayOf("io.getstream.chat.android.client.ChatClient")
        ),
        level = DeprecationLevel.ERROR
    )
    public fun keystroke(cid: String, parentId: String?): Call<Boolean>

    /**
     * StopTyping should be called when the user submits the text and finishes typing.
     *
     * @param cid The full channel id i. e. messaging:123.
     * @param parentId Set this field to `message.id` to indicate that typing event is happening in a thread.
     *
     * @return executable async [Call] which completes with [Result] having data equal true when a typing event was sent,
     * false if it wasn't sent.
     */
    @CheckResult
    @Deprecated(
        message = "stopTyping is deprecated. Use extension function ChatClient::stopTyping instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().stopTyping(cid, parentId)",
            imports = arrayOf("io.getstream.chat.android.client.ChatClient")
        ),
        level = DeprecationLevel.ERROR
    )
    public fun stopTyping(cid: String, parentId: String? = null): Call<Boolean>

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

    /**
     * Shows a channel that was previously hidden.
     *
     * @param cid The full channel id i. e. messaging:123.
     *
     * @return Executable async [Call] responsible for hiding a channel.
     */
    @CheckResult
    @Deprecated(
        message = "Deprecated. Use ChatClient::showChannel instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().showChannel(channelType, channelId)",
            imports = arrayOf("io.getstream.chat.android.client.ChatClient")
        ),
        level = DeprecationLevel.ERROR
    )
    public fun showChannel(cid: String): Call<Unit>

    /**
     * Leaves the channel with the specified id.
     *
     * @param cid The full channel id i. e. messaging:123.
     *
     * @return Executable async [Call] leaving a channel.
     */
    @CheckResult
    @Deprecated(
        message = "leaveChannel is deprecated. Use function ChatClient::removeMembers instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().removeMembers(channel.type, channel.id, listOf(userId))",
            imports = arrayOf("io.getstream.chat.android.client.ChatClient")
        ),
        level = DeprecationLevel.WARNING
    )
    public fun leaveChannel(cid: String): Call<Unit>

    /**
     * Deletes the channel with the specified id.
     *
     * @param cid The full channel id i. e. messaging:123.
     *
     * @return Executable async [Call] deleting a channel.
     */
    @CheckResult
    public fun deleteChannel(cid: String): Call<Unit>

    /**
     * Set the reply state for the channel.
     *
     * @param cid CID of the channel where reply state is being set.
     * @param message The message we want reply to. The null value means dismiss reply state.
     *
     * @return Executable async [Call].
     */
    @CheckResult
    @Deprecated(
        message = "setMessageForReply is deprecated. Use extension function ChatClient::setMessageForReply instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().setMessageForReply(attachment)",
            imports = arrayOf("io.getstream.chat.android.client.ChatClient")
        ),
        level = DeprecationLevel.ERROR
    )
    public fun setMessageForReply(cid: String, message: Message?): Call<Unit>

    /**
     * Downloads the selected attachment to the "Download" folder in the public external storage directory.
     *
     * @param attachment The attachment to download.
     * @return Executable async [Call] downloading attachment.
     */
    @CheckResult
    @Deprecated(
        message = "downloadAttachment is deprecated. Use extension function ChatClient::downloadAttachment instead",
        replaceWith = ReplaceWith(
            expression = "ChatClient.instance().downloadAttachment(attachment)",
            imports = arrayOf("io.getstream.chat.android.client.ChatClient")
        ),
        level = DeprecationLevel.ERROR
    )
    public fun downloadAttachment(attachment: Attachment): Call<Unit>

    /**
     * Perform api request with a search string as autocomplete if in online state. Otherwise performs search by name
     * in local database.
     *
     * @param querySearch Search string used as autocomplete.
     * @param offset Offset for paginated requests.
     * @param userLimit The page size in the request.
     * @param userPresence Presence flag to obtain additional info such as last active date.
     *
     * @return Executable async [Call] querying users.
     */
    @CheckResult
    public fun searchUsersByName(
        querySearch: String,
        offset: Int,
        userLimit: Int,
        userPresence: Boolean,
    ): Call<List<User>>

    /**
     * Query members of a channel.
     *
     * @param cid CID of the Channel whose members we are querying.
     * @param offset Indicates how many items to exclude from the start of the result.
     * @param limit Indicates the maximum allowed number of items in the result.
     * @param filter Filter applied to online queries for advanced selection criteria.
     * @param sort The sort criteria applied to the result.
     * @param members
     *
     * @return Executable async [Call] querying members.
     */
    @CheckResult
    public fun queryMembers(
        cid: String,
        offset: Int = 0,
        limit: Int = 0,
        filter: FilterObject = NeutralFilterObject,
        sort: QuerySort<Member> = QuerySort.desc(Member::createdAt),
        members: List<Member> = emptyList(),
    ): Call<List<Member>>

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

        public fun offlineEnabled(): Builder = apply {
            offlineChatDomainBuilder.offlineEnabled()
        }

        public fun offlineDisabled(): Builder = apply {
            offlineChatDomainBuilder.offlineDisabled()
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

        public fun uploadAttachmentsWorkerNetworkType(networkType: UploadAttachmentsNetworkType): Builder = apply {
            offlineChatDomainBuilder.uploadAttachmentsWorkerNetworkType(networkType)
        }

        public fun retryPolicy(retryPolicy: RetryPolicy): Builder = apply {
            offlineChatDomainBuilder.retryPolicy(retryPolicy)
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
