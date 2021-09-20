package io.getstream.chat.android.offline

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.CheckResult
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
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import io.getstream.chat.android.offline.repository.database.ChatDatabase
import io.getstream.chat.android.offline.thread.ThreadController
import io.getstream.chat.android.offline.utils.Event
import io.getstream.chat.android.offline.utils.RetryPolicy
import kotlinx.coroutines.flow.StateFlow

/**
 * The ChatDomain is the main entry point for all flow & offline operations on chat.
 */
public sealed interface ChatDomain {

    /** The current user on the chatDomain object */
    public val user: StateFlow<User?>

    /** if offline is enabled */
    public var offlineEnabled: Boolean

    /** if we want to track user presence */
    public var userPresence: Boolean

    /** if the client connection has been initialized */
    public val initialized: StateFlow<Boolean>

    /**
     * StateFlow<Boolean> that indicates if we are currently online
     */
    public val online: StateFlow<Boolean>

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
     * if the current user is banned or not
     */
    public val banned: StateFlow<Boolean>

    /** The retry policy for retrying failed requests */
    public var retryPolicy: RetryPolicy

    /**
     * Updates about currently typing users in active channels. See [TypingEvent].
     */
    public val typingUpdates: StateFlow<TypingEvent>

    @InternalStreamChatApi
    public suspend fun disconnect()
    public fun isOnline(): Boolean
    public fun isOffline(): Boolean
    public fun isInitialized(): Boolean
    public fun getActiveQueries(): List<QueryChannelsController>
    public fun clean()
    public fun getChannelConfig(channelType: String): Config
    public fun getVersion(): String

    @CheckResult
    @Deprecated(
        message = "Use ChatClient::removeMembers directly",
        replaceWith = ReplaceWith("ChatClient::removeMembers"),
        level = DeprecationLevel.WARNING,
    )
    public fun removeMembers(cid: String, vararg userIds: String): Call<Channel>

    /**
     * Returns a distinct channel based on its' members. If such channel exists returns existing one, otherwise creates a new.
     *
     * @param channelType String represents channel type.
     * @param members List of members' id.
     * @param extraData Map object with custom fields and additional data.
     *
     * @return [Call] instance with [Channel].
     */
    @CheckResult
    @Deprecated(
        message = "Use ChatClient::createChannel directly",
        replaceWith = ReplaceWith("ChatClient::createChannel"),
        level = DeprecationLevel.WARNING,
    )
    public fun createDistinctChannel(
        channelType: String,
        members: List<String>,
        extraData: Map<String, Any>,
    ): Call<Channel>

    /**
     * Adds the provided channel to the active channels and replays events for all active channels.
     *
     * @return Executable async [Call] responsible for obtaining list of historical [ChatEvent] objects.
     */
    @CheckResult
    public fun replayEventsForActiveChannels(cid: String): Call<List<ChatEvent>>

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
     * Queries offline storage and the API for channels matching the filter.
     *
     * @param filter The filter object.
     * @param sort How to sort the channels (default is last_message_at).
     * @param limit The number of channels to retrieve.
     * @param messageLimit How many messages to retrieve per channel.
     *
     * @return Executable async [Call] responsible for obtaining [QueryChannelsController].
     *
     * @see io.getstream.chat.android.offline.querychannels.QueryChannelsController
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
     * @see io.getstream.chat.android.offline.thread.ThreadController
     */
    @CheckResult
    public fun getThread(cid: String, parentId: String): Call<ThreadController>

    /**
     * Loads older messages for the channel.
     *
     * @param cid The full channel id i. e. messaging:123.
     * @param messageLimit How many new messages to load.
     *
     * @return Executable async [Call] responsible for loading older messages in a channel.
     */
    @CheckResult
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
     * Load more channels for this query.
     *
     * @param filter The filter for querying channels, see https://getstream.io/chat/docs/query_channels/?language=kotlin.
     * @param sort The sort for the channels, by default will sort on last_message_at.
     * @param limit The number of channels to retrieve.
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
        limit: Int,
        messageLimit: Int,
    ): Call<List<Channel>>

    /**
     * Load more channels for this query.
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

    /**
     * Creates a new channel. Will retry according to the retry policy if it fails.
     *
     * @param channel The channel object.
     *
     * @return Executable async [Call] responsible for creating a channel.
     *
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
     */
    @CheckResult
    public fun createChannel(channel: Channel): Call<Channel>

    /**
     * Sends the message. Immediately adds the message to local storage
     * API call to send the message is retried according to the retry policy specified on the chatDomain.
     *
     * @param message The message to send.
     *
     * @return Executable async [Call] responsible for sending a message.
     *
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
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
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
     */
    @CheckResult
    public fun cancelMessage(message: Message): Call<Boolean>

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
    public fun editMessage(message: Message): Call<Message>

    /**
     * Deletes the specified message, request is retried according to the retry policy specified on the chatDomain.
     *
     * @param message The message to mark as deleted.
     * @param hard Use to hard delete the message (delete in backend). CAN'T BE UNDONE.
     *
     * @return Executable async [Call] responsible for deleting a message.
     *
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
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
     * Keystroke should be called whenever a user enters text into the message input.
     * It automatically calls stopTyping when the user stops typing after 5 seconds.
     *
     * @param cid The full channel id i. e. messaging:123.
     * @param parentId Set this field to `message.id` to indicate that typing event is happening in a thread.
     *
     * @return Executable async [Call] which completes with [Result] having data true when a typing event was sent, false if it wasn't sent.
     */
    @CheckResult
    public fun keystroke(cid: String, parentId: String?): Call<Boolean>

    /**
     * StopTyping should be called when the user submits the text and finishes typing.
     *
     * @param cid The full channel id i. e. messaging:123.
     * @param parentId Set this field to `message.id` to indicate that typing event is happening in a thread.
     *
     * @return Executable async [Call] which completes with [Result] having data equal true when a typing event was sent,
     * false if it wasn't sent.
     */
    @CheckResult
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
    public fun showChannel(cid: String): Call<Unit>

    /**
     * Leaves the channel with the specified id.
     *
     * @param cid The full channel id i. e. messaging:123.
     *
     * @return Executable async [Call] leaving a channel.
     */
    @CheckResult
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
    public fun setMessageForReply(cid: String, message: Message?): Call<Unit>

    /**
     * Downloads the selected attachment to the "Download" folder in the public external storage directory.
     *
     * @param attachment The attachment to download.
     *
     * @return Executable async [Call] downloading attachment.
     */
    @CheckResult
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
     * @param filter Applied to online queries for advanced selection criteria.
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

    public data class Builder(
        private val appContext: Context,
        private val client: ChatClient,
    ) {

        public constructor(client: ChatClient, appContext: Context) : this(appContext, client)

        private var database: ChatDatabase? = null

        private var userPresence: Boolean = false
        private var storageEnabled: Boolean = true
        private var recoveryEnabled: Boolean = true
        private var backgroundSyncEnabled: Boolean = true
        private var uploadAttachmentsNetworkType: UploadAttachmentsNetworkType = UploadAttachmentsNetworkType.NOT_ROAMING

        internal fun database(db: ChatDatabase): Builder {
            this.database = db
            return this
        }

        public fun enableBackgroundSync(): Builder {
            backgroundSyncEnabled = true
            return this
        }

        public fun disableBackgroundSync(): Builder {
            backgroundSyncEnabled = false
            return this
        }

        public fun offlineEnabled(): Builder {
            this.storageEnabled = true
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

        public fun uploadAttachmentsWorkerNetworkType(networkType: UploadAttachmentsNetworkType): Builder {
            this.uploadAttachmentsNetworkType = networkType
            return this
        }

        public fun build(): ChatDomain {
            instance = buildImpl()
            return instance()
        }

        internal fun buildImpl(): ChatDomainImpl {
            val handler = Handler(Looper.getMainLooper())
            return ChatDomainImpl(
                client,
                database,
                handler,
                storageEnabled,
                recoveryEnabled,
                userPresence,
                backgroundSyncEnabled,
                appContext,
                uploadAttachmentsNetworkType,
            )
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
