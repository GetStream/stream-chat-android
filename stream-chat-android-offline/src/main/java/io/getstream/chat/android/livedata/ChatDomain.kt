package io.getstream.chat.android.livedata

import android.content.Context
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
import io.getstream.chat.android.livedata.usecase.UseCaseHelper
import io.getstream.chat.android.offline.repository.database.ChatDatabase
import io.getstream.chat.android.offline.utils.Event
import io.getstream.chat.android.offline.utils.RetryPolicy
import java.io.File
import io.getstream.chat.android.offline.ChatDomain as OfflineChatDomain
import io.getstream.chat.android.offline.ChatDomain.Builder as OfflineChatDomainBuilder

/**
 * The ChatDomain is the main entry point for all livedata & offline operations on chat
 *
 * Use cases are exposed via chatDomain.useCases
 */
public interface ChatDomain {

    /** the current user on the chatDomain object, same as client.getCurrentUser() */
    public var currentUser: User

    /** if offline is enabled */
    public var offlineEnabled: Boolean

    /** if we want to track user presence */
    public var userPresence: Boolean

    /** if the client connection has been initialized */
    public val initialized: LiveData<Boolean>

    /**
     * LiveData<Boolean> that indicates if we are currently online
     */
    public val online: LiveData<Boolean>

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
     * if the current user is banned or not
     */
    public val banned: LiveData<Boolean>

    /** The retry policy for retrying failed requests */
    public var retryPolicy: RetryPolicy

    /**
     * Updates about currently typing users in active channels. See [TypingEvent].
     */
    public val typingUpdates: LiveData<TypingEvent>

    /** a helper object which lists all the initialized use cases for the chat domain */
    @Deprecated(
        level = DeprecationLevel.WARNING,
        message = "This property is deprecated now. Use cases are now exposed by ChatDomain directly as functions.",
        replaceWith = ReplaceWith("Replace this property call by obtaining a specific use case directly from ChatDomain."),
    )
    public val useCases: UseCaseHelper

    @Deprecated("Disconnecting from ChatClient will automatically disconnect from ChatDomain")
    public suspend fun disconnect()
    public fun isOnline(): Boolean
    public fun isOffline(): Boolean
    public fun isInitialized(): Boolean
    public fun getActiveQueries(): List<QueryChannelsController>
    public fun clean()
    public fun getChannelConfig(channelType: String): Config
    public fun getVersion(): String
    public fun removeMembers(cid: String, vararg userIds: String): Call<Channel>

    // region use-case functions

    /**
     * Returns a distinct channel based on its' members. If such channel exists returns existing one, otherwise creates a new.
     *
     * @param channelType String represents channel type.
     * @param members List of members' id.
     * @param extraData Map object with custom fields and additional data.
     *
     * @return [Call] instance with [Channel].
     */
    public fun createDistinctChannel(
        channelType: String,
        members: List<String>,
        extraData: Map<String, Any>,
    ): Call<Channel>

    // replaying events
    /**
     * Adds the provided channel to the active channels and replays events for all active channels
     *
     * @return executable async [Call] responsible for obtainging list of historical [ChatEvent] objects
     */
    public fun replayEventsForActiveChannels(cid: String): Call<List<ChatEvent>>

    // getting controllers
    /**
     * Returns a ChannelController for given cid
     *
     * @param cid the full channel id. ie messaging:123
     *
     * @return executable async [Call] responsible for obtaining [ChannelController]
     *
     * @see io.getstream.chat.android.livedata.controller.ChannelController
     */
    public fun getChannelController(cid: String): Call<ChannelController>

    /**
     * Watches the given channel and returns a ChannelController
     *
     * @param cid the full channel id. ie messaging:123
     * @param messageLimit how many messages to load on the first request
     *
     * @return executable async [Call] responsible for obtaining [ChannelController]
     *
     * @see io.getstream.chat.android.livedata.controller.ChannelController
     */
    public fun watchChannel(cid: String, messageLimit: Int): Call<ChannelController>

    /**
     * Queries offline storage and the API for channels matching the filter
     * Returns a queryChannelsController
     *
     * @param filter the filter object
     * @param sort how to sort the channels (default is last_message_at)
     * @param limit the number of channels to retrieve
     * @param messageLimit how many messages to retrieve per channel
     *
     * @return executable async [Call] responsible for obtaining [QueryChannelsController]
     *
     * @see io.getstream.chat.android.livedata.controller.QueryChannelsController
     * @see io.getstream.chat.android.client.utils.FilterObject
     * @see io.getstream.chat.android.client.api.models.QuerySort
     * @see <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin">Filter syntax</a>
     */
    public fun queryChannels(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int = 30,
        messageLimit: Int = 1,
    ): Call<QueryChannelsController>

    /**
     * Returns a thread controller for the given channel and message id
     *
     * @param cid the full channel id. ie messaging:123
     * @param parentId the message id for the parent of this thread
     *
     * @return executable async [Call] responsible for obtaining [ThreadController]
     *
     * @see io.getstream.chat.android.livedata.controller.ThreadController
     */
    public fun getThread(cid: String, parentId: String): Call<ThreadController>

    // loading more
    /**
     * Loads older messages for the channel
     *
     * @param cid: the full channel id i. e. messaging:123
     * @param messageLimit: how many new messages to load
     *
     * @return executable async [Call] responsible for loading older messages in a channel
     */
    public fun loadOlderMessages(cid: String, messageLimit: Int): Call<Channel>

    /**
     * Loads newer messages for the channel
     *
     * @param cid: the full channel id i. e. messaging:123
     * @param messageLimit: how many new messages to load
     *
     * @return executable async [Call] responsible for loading new messages in a channel
     */
    public fun loadNewerMessages(cid: String, messageLimit: Int): Call<Channel>

    /**
     * Loads message for a given message id and channel id
     *
     * @param cid: the full channel id i. e. messaging:123
     * @param messageId: the id of the message
     * @param olderMessagesOffset: how many new messages to load before the requested message
     * @param newerMessagesOffset: how many new messages to load after the requested message
     *
     * @return executable async [Call] responsible for loading a message
     *
     */
    public fun loadMessageById(
        cid: String,
        messageId: String,
        olderMessagesOffset: Int,
        newerMessagesOffset: Int,
    ): Call<Message>

    /**
     * Load more channels for this query
     *
     * @param filter the filter for querying channels, see https://getstream.io/chat/docs/query_channels/?language=kotlin
     * @param sort the sort for the channels, by default will sort on last_message_at
     * @param limit the number of channels to retrieve
     * @param messageLimit how many messages to fetch per chanel
     *
     * @return executable async [Call] responsible for loading more channels
     *
     * @see io.getstream.chat.android.client.api.models.FilterObject
     * @see io.getstream.chat.android.client.api.models.QuerySort
     * @see <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin">Filter syntax</a>
     */
    public fun queryChannelsLoadMore(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int,
        messageLimit: Int,
    ): Call<List<Channel>>

    /**
     * Load more channels for this query
     *
     * @param filter the filter for querying channels, see https://getstream.io/chat/docs/query_channels/?language=kotlin
     * @param sort the sort for the channels, by default will sort on last_message_at
     * @param messageLimit how many messages to fetch per chanel
     *
     * @return executable async [Call] responsible for loading more channels
     *
     * @see io.getstream.chat.android.client.api.models.FilterObject
     * @see io.getstream.chat.android.client.api.models.QuerySort
     * @see <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin">Filter syntax</a>
     */
    public fun queryChannelsLoadMore(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        messageLimit: Int,
    ): Call<List<Channel>>

    /**
     * Load more channels for this query
     *
     * @param filter the filter for querying channels, see https://getstream.io/chat/docs/query_channels/?language=kotlin
     * @param sort the sort for the channels, by default will sort on last_message_at
     *
     * @return executable async [Call] responsible for loading more channels
     *
     * @see io.getstream.chat.android.client.api.models.FilterObject
     * @see io.getstream.chat.android.client.api.models.QuerySort
     * @see <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin">Filter syntax</a>
     */
    public fun queryChannelsLoadMore(filter: FilterObject, sort: QuerySort<Channel>): Call<List<Channel>>

    /**
     * Loads more messages for the specified thread
     *
     * @param cid: the full channel id i. e. messaging:123
     * @param parentId: the parentId of the thread
     * @param messageLimit: how many new messages to load
     *
     * @return executable async [Call] responsible for loading more messages in a thread
     */
    public fun threadLoadMore(cid: String, parentId: String, messageLimit: Int): Call<List<Message>>

    // updating channel data
    /**
     * Creates a new channel. Will retry according to the retry policy if it fails
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     *
     * @param channel the channel object
     *
     * @return executable async [Call] responsible for creating a channel
     */
    public fun createChannel(channel: Channel): Call<Channel>

    /**
     * Sends the message. Immediately adds the message to local storage
     * API call to send the message is retried according to the retry policy specified on the chatDomain
     *
     * @param message the message to send
     *
     * @return executable async [Call] responsible for sending a message
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    public fun sendMessage(message: Message): Call<Message>

    /**
     * Sends the message. Immediately adds the message to local storage
     * API call to send the message is retried according to the retry policy specified on the chatDomain
     *
     * @param message the message to send
     *
     * @return executable async [Call] responsible for sending a message
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */

    public fun sendMessage(
        message: Message,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment)?,
    ): Call<Message>

    /**
     * Cancels the message of "ephemeral" type. Removes the message from local storage.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     *
     * @param message the message to send
     *
     * @return executable async [Call] responsible for canceling ephemeral message
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    public fun cancelMessage(message: Message): Call<Boolean>

    /**
     * Performs giphy shuffle operation. Removes the original "ephemeral" message from local storage.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     *
     * @param message the message to send
     *
     * @return executable async [Call] responsible for shuffling Giphy image
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    public fun shuffleGiphy(message: Message): Call<Message>

    /**
     * Sends selected giphy message to the channel. Removes the original "ephemeral" message from local storage.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     *
     * @param message the message to send
     *
     * @return executable async [Call] responsible for sending Giphy
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    public fun sendGiphy(message: Message): Call<Message>

    /**
     * Edits the specified message. Local storage is updated immediately
     * The API request is retried according to the retry policy specified on the chatDomain
     *
     * @param message the message to edit
     *
     * @return executable async [Call] responsible for editing a message
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    public fun editMessage(message: Message): Call<Message>

    /**
     * Deletes the specified message, request is retried according to the retry policy specified on the chatDomain
     *
     * @param message the message to mark as deleted
     *
     * @return executable async [Call] responsible for deleting a message
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    public fun deleteMessage(message: Message): Call<Message>

    /**
     * Sends the reaction. Immediately adds the reaction to local storage and updates the reaction fields on the related message.
     * API call to send the reaction is retried according to the retry policy specified on the chatDomain
     * @param cid: the full channel id i. e. messaging:123
     * @param reaction the reaction to add
     * @param enforceUnique if set to true, new reaction will replace all reactions the user has on this message
     *
     * @return executable async [Call] responsible for sending a reaction
     *
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    public fun sendReaction(cid: String, reaction: Reaction, enforceUnique: Boolean): Call<Reaction>

    /**
     * Deletes the specified reaction, request is retried according to the retry policy specified on the chatDomain
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     *
     * @param cid the full channel id, ie messaging:123
     * @param reaction the reaction to mark as deleted
     *
     * @return executable async [Call] responsible for deleting reaction
     */
    public fun deleteReaction(cid: String, reaction: Reaction): Call<Message>

    /**
     * Keystroke should be called whenever a user enters text into the message input
     * It automatically calls stopTyping when the user stops typing after 5 seconds
     *
     * @param cid the full channel id i. e. messaging:123
     * @param parentId set this field to `message.id` to indicate that typing event is happening in a thread
     *
     * @return executable async [Call] which completes with [Result] having data true when a typing event was sent, false if it wasn't sent
     */
    public fun keystroke(cid: String, parentId: String?): Call<Boolean>

    /**
     * StopTyping should be called when the user submits the text and finishes typing
     *
     * @param cid: the full channel id i. e. messaging:123
     * @param parentId set this field to `message.id` to indicate that typing event is happening in a thread
     *
     * @return executable async [Call] which completes with [Result] having data equal true when a typing event was sent,
     * false if it wasn't sent.
     */
    public fun stopTyping(cid: String, parentId: String? = null): Call<Boolean>

    /**
     * Marks all messages of the specified channel as read
     *
     * @param cid: the full channel id i. e. messaging:123
     *
     * @return executable async [Call] which completes with [Result] having data equal to true if the mark read event
     * was sent or false if there was no need to mark read (i. e. the messages are already marked as read).
     */
    public fun markRead(cid: String): Call<Boolean>

    /**
     * Marks all messages on a channel as read.
     *
     * @return executable async [Call] responsinble for marking all messages as read
     */
    public fun markAllRead(): Call<Boolean>

    /**
     * Hides the channel with the specified id
     *
     * @param cid: the full channel id i. e. messaging:123
     * @param keepHistory: boolean, if you want to keep the history of this channel or not
     *
     * @return executable async [Call] responsible for hiding a channel
     *
     * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=kotlin">Hiding a channel</a>
     */
    public fun hideChannel(cid: String, keepHistory: Boolean): Call<Unit>

    /**
     * Shows a channel that was previously hidden
     *
     * @param cid: the full channel id i. e. messaging:123
     *
     * @return executable async [Call] responsible for hiding a channel
     */
    public fun showChannel(cid: String): Call<Unit>

    /**
     * Leaves the channel with the specified id
     *
     * @param cid: the full channel id i. e. messaging:123
     *
     * @return executable async [Call] leaving a channel
     */
    public fun leaveChannel(cid: String): Call<Unit>

    /**
     * Deletes the channel with the specified id
     *
     * @param cid: the full channel id i. e. messaging:123
     *
     * @return executable async [Call] deleting a channel
     */
    public fun deleteChannel(cid: String): Call<Unit>

    /**
     * Set the reply state for the channel.
     *
     * @param cid CID of the channel where reply state is being set.
     * @param message The message we want reply to. The null value means dismiss reply state.
     *
     * @return executable async [Call]
     */
    public fun setMessageForReply(cid: String, message: Message?): Call<Unit>

    /**
     * Downloads the selected attachment to the "Download" folder in the public external storage directory.
     *
     * @param attachment the attachment to download
     * @return executable async [Call] downloading attachment
     */
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
     * @return executable async [Call] querying users
     */
    public fun searchUsersByName(
        querySearch: String,
        offset: Int,
        userLimit: Int,
        userPresence: Boolean,
    ): Call<List<User>>

    /**
     * Query members of a channel
     *
     * @param cid CID of the Channel whose members we are querying
     * @param offset indicates how many items to exclude from the start of the result
     * @param limit indicates the maximum allowed number of items in the result
     * @param filter applied to online queries for advanced selection criteria
     * @param sort the sort criteria applied to the result
     * @param members
     *
     * @return executable async [Call] querying members
     */
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
        private var user: User? = null

        public constructor(client: ChatClient, appContext: Context) : this(appContext, client)

        @Deprecated(
            message = "Use constructor without user",
            replaceWith = ReplaceWith("Use ChatDomain.Builder(appContext, chatClient) instead")
        )
        public constructor(appContext: Context, client: ChatClient, user: User?) : this(appContext, client) {
            this.user = user
        }

        @Deprecated(
            message = "Use constructor without user",
            replaceWith = ReplaceWith("Use ChatDomain.Builder(appContext, chatClient) instead")
        )
        public constructor(client: ChatClient, user: User?, appContext: Context) : this(appContext, client) {
            this.user = user
        }

        private val offlineChatDomainBuilder: OfflineChatDomainBuilder =
            OfflineChatDomainBuilder(appContext, client, user)

        internal fun database(db: ChatDatabase) = apply {
            offlineChatDomainBuilder.database(db)
        }

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

        public fun build(): ChatDomain {
            val offlineChatDomain = offlineChatDomainBuilder.build()
            ChatDomain.instance = buildImpl(offlineChatDomain)
            return ChatDomain.instance()
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
