/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.internal.state.plugin.state.channel.internal

import androidx.collection.LruCache
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.extensions.getCreatedAtOrDefault
import io.getstream.chat.android.client.extensions.getCreatedAtOrNull
import io.getstream.chat.android.client.extensions.internal.updateUsers
import io.getstream.chat.android.client.extensions.internal.wasCreatedAfter
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateImpl.Companion.CACHED_LATEST_MESSAGES_LIMIT
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateImpl.Companion.TRIM_BUFFER
import io.getstream.chat.android.client.internal.state.utils.internal.combineStates
import io.getstream.chat.android.client.internal.state.utils.internal.mapState
import io.getstream.chat.android.client.internal.state.utils.internal.updateIf
import io.getstream.chat.android.client.internal.state.utils.internal.upsertSorted
import io.getstream.chat.android.client.internal.state.utils.internal.upsertSortedBounded
import io.getstream.chat.android.client.utils.channel.calculateNewLastMessageAt
import io.getstream.chat.android.extensions.lastMessageAt
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessagesState
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.PushPreference
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserId
import io.getstream.chat.android.models.mergeFromEvent
import io.getstream.chat.android.models.toChannelData
import io.getstream.log.taggedLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import kotlin.math.max

/**
 * Default implementation of the [ChannelState].
 *
 * @property channelType The type of the channel.
 * @property channelId The ID of the channel.
 * @property currentUser The currently logged in user.
 * @property latestUsers A [StateFlow] providing the latest users map. Used to enrich the members/watcher data with the
 * latest user info retrieved from different, unrelated operations.
 * @property mutedUsers A [StateFlow] providing the list of muted users.
 * @property liveLocations A [StateFlow] providing the active live locations.
 * @property messageLimit The initial limit specifying how many of the latest messages should be kept in memory.
 */
@Suppress("LargeClass", "LongParameterList", "TooManyFunctions")
internal class ChannelStateImpl(
    override val channelType: String,
    override val channelId: String,
    private val currentUser: User, // TODO: Check if we can rely on user being always up to date (ex. switchUser)
    private val latestUsers: StateFlow<Map<String, User>>,
    private val mutedUsers: StateFlow<List<Mute>>,
    private val liveLocations: StateFlow<List<Location>>,
    private val messageLimit: Int?,
) : ChannelState {

    override val cid: String = "$channelType:$channelId"

    // Messages
    private val _repliedMessage = MutableStateFlow<Message?>(null)
    private val _quotedMessagesMap = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    private val _messages = MutableStateFlow<List<Message>>(emptyList())

    /**
     * Keeps track of the latest messages in the channel, if `Jump to message` was called, and a different, non-latest
     * message set was loaded.
     */
    private val _cachedLatestMessages = MutableStateFlow<List<Message>>(emptyList())
    private val _pinnedMessages = MutableStateFlow<List<Message>>(emptyList())
    private val _oldMessages = MutableStateFlow<List<Message>>(emptyList())

    // Watchers
    private val _watcherCount = MutableStateFlow(0)
    private val _watchers = MutableStateFlow<List<User>>(emptyList()) // TODO Maybe use a map for performance sake

    // Typing events
    private val _typing = MutableStateFlow(TypingEvent(channelId, emptyList()))

    // Read state
    private val _reads = MutableStateFlow<Map<UserId, ChannelUserRead>>(emptyMap())

    // Members
    private val _memberCount = MutableStateFlow(0)
    private val _members = MutableStateFlow<List<Member>>(emptyList())

    // Channel data
    private val _channelData = MutableStateFlow<ChannelData?>(null)
    private val _hidden = MutableStateFlow(false)
    private val _muted = MutableStateFlow(false)
    private val _channelConfig = MutableStateFlow(Config())

    // Non-channel states
    private val _loading = MutableStateFlow(false)
    private val _loadingOlderMessages = MutableStateFlow(false)
    private val _loadingNewerMessages = MutableStateFlow(false)
    private val _endOfOlderMessages = MutableStateFlow(false)
    private val _endOfNewerMessages = MutableStateFlow(true)
    private var _recoveryNeeded = false
    private val _insideSearch = MutableStateFlow(false)
    private var lastStartTypingEvent: Date? = null
    private var keystrokeParentMessageId: String? = null

    /**
     * Keeps track of messages who are linked with polls.
     * Key: Poll ID
     * Value: Set of Message IDs linked to the poll
     */
    private val messagesWithPolls = mutableMapOf<String, Set<String>>()
    private val polls = mutableMapOf<String, Poll>()

    /* Keeps track of messages processed when updating the current user read state */
    private val processedMessageIds = LruCache<String, Boolean>(maxSize = 100)

    private val logger by taggedLogger("ChannelStateImpl")

    override val repliedMessage: StateFlow<Message?> = _repliedMessage.asStateFlow()

    override val quotedMessagesMap: StateFlow<Map<String, List<String>>> = _quotedMessagesMap.mapState {
        it.mapValues { entry -> entry.value.toList() }
    }

    override val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    override val pinnedMessages: StateFlow<List<Message>> = _pinnedMessages.asStateFlow()

    override val messagesState: StateFlow<MessagesState> = combineStates(_loading, _messages) { loading, messages ->
        when {
            loading -> MessagesState.Loading
            messages.isEmpty() -> MessagesState.OfflineNoResults
            else -> MessagesState.Result(messages)
        }
    }

    @Deprecated("This property is not used anymore and will be removed in future versions.")
    override val oldMessages: StateFlow<List<Message>> = _oldMessages.asStateFlow()

    override val watcherCount: StateFlow<Int> = _watcherCount.asStateFlow()

    override val watchers: StateFlow<List<User>> = combineStates(_watchers, latestUsers) { watchers, users ->
        watchers
            .updateUsers(users)
            .sortedBy(User::createdAt)
    }

    override val typing: StateFlow<TypingEvent> = _typing.asStateFlow()

    override val reads: StateFlow<List<ChannelUserRead>> = _reads.mapState { reads ->
        reads.values.sortedBy(ChannelUserRead::lastRead)
    }

    override val read: StateFlow<ChannelUserRead?> = _reads.mapState { reads ->
        reads[currentUser.id]
    }

    override val unreadCount: StateFlow<Int> = read.mapState { read ->
        read?.unreadMessages ?: 0
    }

    override val membersCount: StateFlow<Int> = _memberCount.asStateFlow()

    override val members: StateFlow<List<Member>> = combineStates(_members, latestUsers) { members, users ->
        members
            .updateUsers(users)
            .sortedBy(Member::createdAt)
    }

    override val channelData: StateFlow<ChannelData> = combineStates(_channelData, latestUsers) { data, users ->
        if (data == null) {
            ChannelData(type = channelType, id = channelId)
        } else {
            val createdBy = users[data.createdBy.id]
            data.copy(createdBy = createdBy ?: data.createdBy)
        }
    }

    override val hidden: StateFlow<Boolean> = _hidden.asStateFlow()

    override val muted: StateFlow<Boolean> = _muted.asStateFlow()

    override val loading: StateFlow<Boolean> = _loading.asStateFlow()

    override val loadingOlderMessages: StateFlow<Boolean> = _loadingOlderMessages.asStateFlow()

    override val loadingNewerMessages: StateFlow<Boolean> = _loadingNewerMessages.asStateFlow()

    override val endOfOlderMessages: StateFlow<Boolean> = _endOfOlderMessages.asStateFlow()

    override val endOfNewerMessages: StateFlow<Boolean> = _endOfNewerMessages.asStateFlow()

    override val recoveryNeeded: Boolean
        get() = _recoveryNeeded

    override val channelConfig: StateFlow<Config> = _channelConfig.asStateFlow()

    override val insideSearch: StateFlow<Boolean> = _insideSearch.asStateFlow()

    override val lastSentMessageDate: StateFlow<Date?> = combineStates(channelConfig, messages) { config, messages ->
        // TODO: Optimize this logic, we don't need to scan all messages every time
        messages
            .filter { it.user.id == currentUser.id }
            .lastMessageAt(config.skipLastMsgUpdateForSystemMsgs)
    }

    override val activeLiveLocations: StateFlow<List<Location>> = liveLocations.mapState { locations ->
        // Filter locations to only include those for this channel
        locations.filter { it.cid == cid }
    }

    override val messageCount: StateFlow<Int?> = _channelData.mapState { data ->
        data?.messageCount
    }

    override fun toChannel(): Channel {
        // Reconstruct the Channel object based on the current state
        return channelData.value
            .toChannel(
                messages = messages.value,
                cachedLatestMessages = _cachedLatestMessages.value,
                members = members.value,
                reads = reads.value,
                watchers = watchers.value,
                watcherCount = watcherCount.value,
                insideSearch = insideSearch.value,
            )
            .copy(
                config = channelConfig.value,
                hidden = hidden.value,
                pinnedMessages = pinnedMessages.value,
                activeLiveLocations = activeLiveLocations.value,
            )
    }

    override fun getMessageById(id: String): Message? {
        // TODO: Can we optimize the usages of this lookup, by doing the lookup + update in one pass?
        return _messages.value.find { it.id == id }
            ?: _cachedLatestMessages.value.find { it.id == id }
            ?: _pinnedMessages.value.find { it.id == id }
    }

    // region Messages

    /**
     * Sets the list of messages (overriding the current one).
     *
     * @param messages The list of messages to set.
     */
    fun setMessages(messages: List<Message>) {
        _messages.value = emptyList()
        upsertMessages(messages)
    }

    /**
     * Upserts a single message into the current state.
     *
     * @param message The message to upsert.
     */
    fun upsertMessage(message: Message) {
        upsertMessages(listOf(message))
    }

    /**
     * Upserts the list of messages into the current state.
     *
     * @param messages The list of messages to upsert.
     */
    fun upsertMessages(messages: List<Message>) {
        for (message in messages) {
            // Check if the message should be ignored for upsertion
            if (shouldIgnoreUpsertion(message)) {
                continue
            }

            // Store QuotedMessage reference (if available)
            val quotedMessage = message.replyTo
            if (quotedMessage != null) {
                addQuotedMessage(quotedMessage.id, message.id)
            }

            // Link message with poll (if available)
            val poll = message.poll
            if (poll != null) {
                registerPollForMessage(poll, message.id)
            }

            // Insert or update the message in the sorted list
            _messages.update { current ->
                current.upsertSorted(
                    element = message,
                    idSelector = Message::id,
                    comparator = compareBy { it.getCreatedAtOrNull() },
                )
            }
        }
    }

    /**
     * Upserts a single message into the cached latest messages state.
     * The cached messages are bounded to [CACHED_LATEST_MESSAGES_LIMIT] to prevent unbounded growth
     * while the user is in search mode.
     *
     * @param message The message to upsert.
     */
    fun upsertCachedMessage(message: Message) {
        // Check if the message should be ignored for upsertion
        if (shouldIgnoreUpsertion(message)) {
            return
        }

        // Store QuotedMessage reference (if available)
        val quotedMessage = message.replyTo
        if (quotedMessage != null) {
            addQuotedMessage(quotedMessage.id, message.id)
        }

        // Link message with poll (if available)
        val poll = message.poll
        if (poll != null) {
            registerPollForMessage(poll, message.id)
        }

        // Insert or update the message in the sorted list with a hard limit
        _cachedLatestMessages.update { current ->
            current.upsertSortedBounded(
                element = message,
                maxSize = CACHED_LATEST_MESSAGES_LIMIT,
                idSelector = Message::id,
                comparator = compareBy { it.getCreatedAtOrNull() },
            )
        }
    }

    /**
     * Updates a message in the current state. Does nothing if the message does not exist.
     *
     * @param message The message to update.
     */
    fun updateMessage(message: Message) {
        // Update message list
        val index = _messages.value.indexOfFirst { it.id == message.id }
        if (index >= 0) {
            _messages.update { current ->
                val mutableList = current.toMutableList()
                mutableList[index] = message
                mutableList.toList()
            }
        }
        // Update cached latest messages list
        val cachedIndex = _cachedLatestMessages.value.indexOfFirst { it.id == message.id }
        if (cachedIndex >= 0) {
            _cachedLatestMessages.update { current ->
                val mutableList = current.toMutableList()
                mutableList[cachedIndex] = message
                mutableList.toList()
            }
        }
        // Update pinned messages list
        val pinnedIndex = _pinnedMessages.value.indexOfFirst { it.id == message.id }
        if (pinnedIndex >= 0) {
            _pinnedMessages.update { current ->
                val mutableList = current.toMutableList()
                mutableList[pinnedIndex] = message
                mutableList.toList()
            }
        }
    }

    /**
     * Hard deletes a message from the current state.
     * Note: Soft deletes are handled via [updateMessage].
     *
     * @param id The ID of the message to delete.
     */
    fun deleteMessage(id: String) = deleteMessages(setOf(id))

    /**
     * Removes all messages created before the specified date.
     *
     * @param date The cutoff date; messages created before this date will be removed.
     * @param systemMessage An optional system message to upsert after removal.
     */
    fun removeMessagesBefore(date: Date, systemMessage: Message? = null) {
        _messages.update { current ->
            current.filter { message ->
                message.wasCreatedAfter(date)
            }
        }
        _cachedLatestMessages.update { current ->
            current.filter { message ->
                message.wasCreatedAfter(date)
            }
        }
        _pinnedMessages.update { current ->
            current.filter { message ->
                message.wasCreatedAfter(date)
            }
        }
        systemMessage?.let(::upsertMessage)
    }

    /**
     * Hides all messages created before the specified date.
     * This is an alias for [removeMessagesBefore].
     * TODO: Verify is this is required.
     *
     * @param date The cutoff date; messages created before this date will be hidden.
     */
    fun hideMessagesBefore(date: Date) {
        removeMessagesBefore(date)
    }

    /**
     * Deletes all messages from a specific user.
     *
     * @param userId The ID of the user whose messages should be deleted.
     * @param hard If true, messages are hard deleted; if false, they are soft deleted (marked as deleted).
     * @param deletedAt The timestamp to set for soft-deleted messages.
     */
    fun deleteMessagesFromUser(userId: String, hard: Boolean, deletedAt: Date) {
        // TODO: Optimize this logic by running the query once per message set
        val messagesFromUser = _messages.value.filter { it.user.id == userId } +
            _cachedLatestMessages.value.filter { it.user.id == userId } +
            _pinnedMessages.value.filter { it.user.id == userId }
        if (messagesFromUser.isEmpty()) return
        if (hard) {
            // Delete messages from state
            val ids = messagesFromUser.map { it.id }.toSet()
            deleteMessages(ids)
        } else {
            // Mark messages as deleted (soft delete)
            for (message in messagesFromUser) {
                val deletedMessage = message.copy(deletedAt = deletedAt)
                updateMessage(deletedMessage)
            }
        }
    }

    // endregion

    // region QuotedMessages

    /**
     * Sets the message being replied to.
     */
    fun setRepliedMessage(message: Message?) {
        _repliedMessage.value = message
    }

    /**
     * Adds a quoted message to the state.
     *
     * @param quotedMessageId The ID of the quoted message.
     * @param quotingMessageId The ID of the message that is quoting.
     */
    fun addQuotedMessage(quotedMessageId: String, quotingMessageId: String) {
        _quotedMessagesMap.update { current ->
            val newValue = current[quotedMessageId].orEmpty() + quotingMessageId
            current + (quotedMessageId to newValue)
        }
    }

    /**
     * Updates each message that quotes the given message.
     *
     * @param quotedMessage The message whose quoting messages should be updated.
     */
    fun updateQuotedMessageReferences(quotedMessage: Message) {
        _messages.update { current ->
            current.map { message ->
                if (message.replyTo?.id == quotedMessage.id || message.replyMessageId == quotedMessage.id) {
                    message.copy(replyTo = quotedMessage)
                } else {
                    message
                }
            }
        }
        _cachedLatestMessages.update { current ->
            current.map { message ->
                if (message.replyTo?.id == quotedMessage.id || message.replyMessageId == quotedMessage.id) {
                    message.copy(replyTo = quotedMessage)
                } else {
                    message
                }
            }
        }
        _pinnedMessages.update { current ->
            current.map { message ->
                if (message.replyTo?.id == quotedMessage.id || message.replyMessageId == quotedMessage.id) {
                    message.copy(replyTo = quotedMessage)
                } else {
                    message
                }
            }
        }
    }

    /**
     * Deletes references to a quoted message from all messages.
     *
     * @param quotedMessageId The ID of the quoted message to remove references for.
     */
    fun deleteQuotedMessageReferences(quotedMessageId: String) {
        _messages.update { current ->
            current.map { message ->
                if (message.replyTo?.id == quotedMessageId || message.replyMessageId == quotedMessageId) {
                    message.copy(replyTo = null)
                } else {
                    message
                }
            }
        }
        _cachedLatestMessages.update { current ->
            current.map { message ->
                if (message.replyTo?.id == quotedMessageId || message.replyMessageId == quotedMessageId) {
                    message.copy(replyTo = null)
                } else {
                    message
                }
            }
        }
        _pinnedMessages.update { current ->
            current.map { message ->
                if (message.replyTo?.id == quotedMessageId || message.replyMessageId == quotedMessageId) {
                    message.copy(replyTo = null)
                } else {
                    message
                }
            }
        }
        _quotedMessagesMap.update { current ->
            current - quotedMessageId
        }
    }

    // endregion

    // region PinnedMessages

    /**
     * Adds a pinned message to the current pinned messages list.
     *
     * @param pinnedMessage The pinned message to add.
     */
    fun addPinnedMessage(pinnedMessage: Message) = addPinnedMessages(listOf(pinnedMessage))

    /**
     * Adds pinned messages to the current pinned messages list.
     *
     * @param pinnedMessages The list of pinned messages to add.
     */
    fun addPinnedMessages(pinnedMessages: List<Message>) {
        for (pinnedMessage in pinnedMessages) {
            // Link message with poll (if available)
            val poll = pinnedMessage.poll
            if (poll != null) {
                registerPollForMessage(poll, pinnedMessage.id)
            }

            // Store QuotedMessage reference (if available)
            val quotedMessage = pinnedMessage.replyTo
            if (quotedMessage != null) {
                addQuotedMessage(quotedMessage.id, pinnedMessage.id)
            }

            _pinnedMessages.update { current ->
                current.upsertSorted(
                    element = pinnedMessage,
                    idSelector = Message::id,
                    comparator = compareBy { it.pinnedAt },
                )
            }
        }
    }

    /**
     * Deletes a pinned message from the current pinned messages list.
     *
     * @param messageId The ID of the pinned message to delete.
     */
    fun deletePinnedMessage(messageId: String) {
        _pinnedMessages.update { current ->
            current.filterNot { it.id == messageId }
        }
    }

    // endregion

    // region Members

    /**
     * Updates the ban status of a member.
     *
     * @param memberId The ID of the member to update.
     * @param banned `true` if the member is banned, `false` otherwise.
     * @param expiry The expiry date of the ban, or `null` if the ban does not expire.
     * @param shadow `true` if the ban is a shadow ban, `false` otherwise.
     */
    fun updateMemberBan(
        memberId: String?,
        banned: Boolean,
        expiry: Date?,
        shadow: Boolean,
    ) {
        _members.update { current ->
            current.map { member ->
                if (member.user.id == memberId) {
                    member.copy(
                        banned = banned,
                        banExpires = expiry,
                        shadowBanned = shadow,
                    )
                } else {
                    member
                }
            }
        }
    }

    /**
     * Sets the member count.
     *
     * @param count The total count of members.
     */
    fun setMemberCount(count: Int) {
        _memberCount.value = count
    }

    /**
     * Sets the list of members.
     *
     * @param members The list of members.
     */
    fun setMembers(members: List<Member>) {
        _members.value = members
    }

    /**
     * Adds a member to the current state.
     *
     * @param member The member to add.
     */
    fun addMember(member: Member) {
        _members.update { current ->
            if (current.any { it.getUserId() == member.getUserId() }) {
                current
            } else {
                current + member
            }
        }
        _memberCount.update { it + 1 }
    }

    /**
     * Upserts a single member into the current state.
     *
     * @param member The member to upsert.
     */
    fun upsertMember(member: Member) {
        upsertMembers(listOf(member))
    }

    /**
     * Upserts the list of members into the current state.
     *
     * @param members The list of members to upsert.
     */
    fun upsertMembers(members: List<Member>) {
        val currentMembers = _members.value.associateBy(Member::getUserId).toMutableMap()
        currentMembers += members.associateBy(Member::getUserId)
        _members.value = currentMembers.values.toList()
    }

    /**
     * Deletes a member from the current state.
     *
     * @param id The ID of the member to delete.
     */
    fun deleteMember(id: String) {
        _members.update { current ->
            current.filter { it.getUserId() != id }
        }
        _memberCount.update { max(0, it - 1) }
    }

    // endregion

    // region Membership

    /**
     * Sets the membership of the channel (relation between the current user and the channel).
     *
     * @param membership The membership to set.
     */
    fun setMembership(membership: Member) {
        updateChannelData { current ->
            current?.copy(membership = membership)
        }
    }

    /**
     * Removes the membership of the channel (relation between the current user and the channel).
     */
    fun deleteMembership() {
        updateChannelData { current ->
            current?.copy(membership = null)
        }
    }

    // endregion

    // region Watchers

    /**
     * Sets the list of watchers and the watcher count.
     *
     * @param watchers The list of watchers (may not contain all watchers).
     * @param watcherCount The total count of watchers.
     */
    fun setWatchers(watchers: List<User>, watcherCount: Int) {
        _watchers.value = watchers
        _watcherCount.value = max(0, watcherCount)
    }

    /**
     * Upserts a watcher from a [UserStartWatchingEvent].
     *
     * @param event The event containing the watcher information.
     */
    fun upsertWatcher(event: UserStartWatchingEvent) {
        upsertWatchers(listOf(event.user), event.watcherCount)
    }

    /**
     * Upserts the list of watchers and updates the watcher count.
     *
     * @param watchers The list of watchers to upsert.
     * @param watcherCount The total count of watchers.
     */
    fun upsertWatchers(watchers: List<User>, watcherCount: Int) {
        val currentWatchers = _watchers.value.associateBy { it.id }.toMutableMap()
        for (watcher in watchers) {
            currentWatchers[watcher.id] = watcher
        }
        _watchers.value = currentWatchers.values.sortedBy(User::createdAt)
        _watcherCount.value = max(0, watcherCount)
    }

    /**
     * Deletes a watcher from a [UserStopWatchingEvent].
     *
     * @param event The event containing the watcher information.
     */
    fun deleteWatcher(event: UserStopWatchingEvent) {
        deleteWatcher(event.user.id, event.watcherCount)
    }

    /**
     * Deletes a watcher by user ID and updates the watcher count.
     *
     * @param userId The ID of the user to delete from watchers.
     * @param watcherCount The total count of watchers after deletion.
     */
    fun deleteWatcher(userId: UserId, watcherCount: Int) {
        _watchers.update { current ->
            current.filterNot { it.id == userId }
        }
        _watcherCount.value = max(0, watcherCount)
    }

    // endregion

    // region Polls

    /**
     * Retrieves a poll by its ID.
     *
     * @param id The ID of the poll to retrieve.
     * @return The [Poll] if found, or null if not found.
     */
    fun getPoll(id: String): Poll? {
        return polls[id]
    }

    /**
     * Upserts a poll into the current state.
     *
     * @param poll The poll to upsert.
     */
    fun upsertPoll(poll: Poll) {
        polls[poll.id] = poll
        // Update the poll in associated messages
        val messageIds = messagesWithPolls[poll.id].orEmpty()
        for (messageId in messageIds) {
            val message = getMessageById(messageId)
            if (message != null) {
                val updatedMessage = message.copy(poll = poll)
                updateMessage(updatedMessage)
            }
        }
    }

    /**
     * Deletes a poll from the current state.
     *
     * @param poll The poll to delete.
     */
    fun deletePoll(poll: Poll) {
        polls.remove(poll.id)
        // Remove the poll from associated messages
        val messageIds = messagesWithPolls[poll.id].orEmpty()
        for (messageId in messageIds) {
            val message = getMessageById(messageId)
            if (message != null) {
                val updatedMessage = message.copy(poll = null)
                updateMessage(updatedMessage)
            }
        }
        // Remove the mapping
        messagesWithPolls.remove(poll.id)
    }

    // endregion

    // region ReadReceipts

    /**
     * Updates the read state for a single user.
     *
     * @param read The read state for a user.
     */
    fun updateRead(read: ChannelUserRead) {
        updateReads(listOf(read))
    }

    /**
     * Updates the reads state for the current user based on a received event.
     * Handles `message.new` and `notification.message_new` events.
     *
     * @param eventReceivedDate The date when the event was received.
     * @param message The new message that triggered the event.
     */
    fun updateCurrentUserRead(eventReceivedDate: Date, message: Message) {
        // Skip update if the message was already processed
        val isProcessed = processedMessageIds[message.id] == true
        if (isProcessed) {
            return
        }
        // Skip update if the channel is muted
        val isMuted = muted.value
        if (isMuted) {
            processedMessageIds.put(message.id, true)
            return
        }
        // Skip update for thread replies not shown in channel
        val isThreadReplyNotInChannel = message.parentId != null && !message.showInChannel
        if (isThreadReplyNotInChannel) {
            processedMessageIds.put(message.id, true)
            return
        }
        // Skip update for messages from current user
        val isFromCurrentUser = message.user.id == currentUser.id
        if (isFromCurrentUser) {
            processedMessageIds.put(message.id, true)
            return
        }
        // Skip update for messages from muted users
        val isFromMutedUser = mutedUsers.value.any { it.target?.id == message.user.id }
        if (isFromMutedUser) {
            processedMessageIds.put(message.id, true)
            return
        }
        // Skip update for messages from shadow banned users
        if (message.shadowed) {
            processedMessageIds.put(message.id, true)
            return
        }
        // Skip update for silent messages
        if (message.silent) {
            processedMessageIds.put(message.id, true)
            return
        }
        // Skip update if the event is outdated
        val currentRead = read.value
        if (currentRead != null && currentRead.lastReceivedEventDate.after(eventReceivedDate)) {
            processedMessageIds.put(message.id, true)
            return
        }
        // Update the unread count
        currentRead?.let {
            updateRead(
                it.copy(
                    lastReceivedEventDate = eventReceivedDate,
                    unreadMessages = it.unreadMessages.inc(),
                ),
            )
        }
        processedMessageIds.put(message.id, true)
    }

    /**
     * Marks the channel as read for the current user if the following conditions are met:
     * 1. Read events are enabled in the channel configuration.
     * 2. There are messages in the channel.
     * 3. The last message in the channel is different from the last read message for the current user.
     *
     * @return `true` if the channel was marked as read, `false` otherwise.
     */
    fun markRead(): Boolean {
        if (!channelConfig.value.readEventsEnabled) {
            // Ignore request, `read_events = false`
            return false
        }
        val lastMessage = _messages.value.lastOrNull()
        if (lastMessage == null) {
            // No messages in the channel, nothing to mark as read
            return true
        }
        val currentUserRead = read.value
        if (currentUserRead == null) {
            // No read state for current user, but we can still mark the channel as read, and create a new read state
            // later when we receive the updated read state from the backend
            return true
        }
        return if (lastMessage.id != currentUserRead.lastReadMessageId) {
            // The last message is different from the last read message, we can mark the channel as read
            val updatedRead = currentUserRead.copy(
                lastReceivedEventDate = lastMessage.getCreatedAtOrDefault(Date()),
                lastRead = lastMessage.getCreatedAtOrDefault(Date()),
                unreadMessages = 0,
            )
            _reads.update { current ->
                current + (updatedRead.getUserId() to updatedRead)
            }
            true
        } else {
            // Already marked up to the latest message
            false
        }
    }

    /**
     * Updates the delivered status for a user's read state.
     *
     * @param read The [ChannelUserRead] containing the delivered status to update.
     */
    fun updateDelivered(read: ChannelUserRead) {
        val updatedRead = _reads.value[read.user.id]?.copy(
            // Update only relevant fields
            user = read.user,
            lastReceivedEventDate = read.lastReceivedEventDate,
            lastDeliveredAt = read.lastDeliveredAt,
            lastDeliveredMessageId = read.lastDeliveredMessageId,
        ) ?: read
        _reads.update { current ->
            current + (updatedRead.user.id to updatedRead)
        }
    }

    // endregion

    // region ChannelData

    /**
     * Updates the channel data using the provided update function.
     *
     * @param update A function that takes the current [ChannelData] and returns the updated [ChannelData].
     */
    fun updateChannelData(update: (ChannelData?) -> ChannelData?) {
        _channelData.update { current -> update(current) }
    }

    /**
     * Updates the channel data based on a [HasChannel] event.
     *
     * @param event The event containing the channel information.
     */
    fun updateChannelData(event: HasChannel) {
        updateChannelData { current ->
            event.channel.toChannelData().let { newData ->
                current?.mergeFromEvent(newData) ?: newData
            }
        }
    }

    /**
     * Sets whether the channel is hidden.
     *
     * @param hidden `true` if the channel is hidden, `false` otherwise.
     */
    fun setHidden(hidden: Boolean) {
        _hidden.value = hidden
    }

    /**
     * Sets whether the channel is muted.
     *
     * @param muted `true` if the channel is muted, `false` otherwise.
     */
    fun setMuted(muted: Boolean) {
        _muted.value = muted
    }

    /**
     * Sets the push notification preference for the channel.
     *
     * @param preference The [PushPreference] to set.
     */
    fun setPushPreference(preference: PushPreference) {
        updateChannelData { current ->
            current?.copy(pushPreference = preference)
        }
    }

    /**
     * Sets the message count in the channel data.
     *
     * @param count The new message count to set.
     */
    fun setMessageCount(count: Int) {
        updateChannelData { current ->
            current?.copy(messageCount = count)
        }
    }

    /**
     * Updates the channel `lastMessageAt` with the new [message].
     *
     * @param message The [Message] to use to update the channel `lastMessageAt` property.
     */
    fun updateLastMessageAt(message: Message) {
        val skipSystemMsg = _channelConfig.value.skipLastMsgUpdateForSystemMsgs
        updateChannelData { channelData ->
            val newLastMessageAt = calculateNewLastMessageAt(
                message = message,
                currentLastMessageAt = channelData?.lastMessageAt,
                skipLastMsgUpdateForSystemMsgs = skipSystemMsg,
            )
            if (newLastMessageAt != channelData?.lastMessageAt) {
                channelData?.copy(lastMessageAt = newLastMessageAt)
            } else {
                channelData
            }
        }
    }

    /**
     * Marks the channel as deleted by setting the [deletedAt] timestamp.
     *
     * @param deletedAt The timestamp when the channel was deleted.
     */
    fun deleteChannel(deletedAt: Date) {
        updateChannelData { current ->
            current?.copy(deletedAt = deletedAt)
        }
    }

    // endregion

    // region ChannelConfig

    /**
     * Sets the channel configuration.
     *
     * @param config The new [Config] to set.
     */
    fun setChannelConfig(config: Config) {
        _channelConfig.value = config
    }

    // endregion

    // region Users

    fun upsertUserPresence(user: User) {
        // Update members state
        _members.update { current ->
            current.updateIf(
                filter = { member -> member.getUserId() == user.id },
                update = { member -> member.copy(user = user) },
            )
        }

        // Update watchers state
        _watchers.update { current ->
            current.updateIf(
                filter = { watcher -> watcher.id == user.id },
                update = { _ -> user },
            )
        }

        // Update channel data createdBy if needed
        val currentChannelData = _channelData.value
        if (currentChannelData != null && currentChannelData.createdBy.id == user.id) {
            _channelData.value = currentChannelData.copy(createdBy = user)
        }

        // Update messages state
        _messages.update { current ->
            current.updateIf(
                filter = { message -> message.user.id == user.id },
                update = { message -> message.copy(user = user) },
            )
        }
        _cachedLatestMessages.update { current ->
            current.updateIf(
                filter = { message -> message.user.id == user.id },
                update = { message -> message.copy(user = user) },
            )
        }
        _pinnedMessages.update { current ->
            current.updateIf(
                filter = { message -> message.user.id == user.id },
                update = { message -> message.copy(user = user) },
            )
        }
    }

    // endregion

    // region Typing

    /**
     * Sets the typing state for a user.
     *
     * @param typingEvent The [TypingEvent] to set.
     */
    fun setTyping(typingEvent: TypingEvent) {
        _typing.value = typingEvent
    }

    /**
     * Retrieves the last start typing event date.
     *
     * @return The [Date] of the last start typing event, or null if not set.
     */
    fun getLastStartTypingEvent(): Date? {
        return lastStartTypingEvent
    }

    /**
     * Sets the last start typing event date.
     *
     * @param date The [Date] to set as the last start typing event.
     */
    fun setLastStartTypingEvent(date: Date?) {
        lastStartTypingEvent = date
    }

    /**
     * Sets the keystroke parent message ID.
     *
     * @param messageId The parent message ID to set for keystrokes.
     */
    fun setKeystrokeParentMessageId(messageId: String?) {
        keystrokeParentMessageId = messageId
    }

    // endregion

    // region NonChannelStates

    /**
     * Sets the loading state.
     *
     * @param loading `true` if loading, `false` otherwise.
     */
    fun setLoading(loading: Boolean) {
        _loading.value = loading
    }

    /**
     * Sets the loading older messages state.
     *
     * @param loadingOlderMessages `true` if loading older messages, `false` otherwise.
     */
    fun setLoadingOlderMessages(loadingOlderMessages: Boolean) {
        _loadingOlderMessages.value = loadingOlderMessages
    }

    /**
     * Sets the loading newer messages state.
     *
     * @param loadingNewerMessages `true` if loading newer messages, `false` otherwise.
     */
    fun setLoadingNewerMessages(loadingNewerMessages: Boolean) {
        _loadingNewerMessages.value = loadingNewerMessages
    }

    /**
     * Sets the end of older messages state.
     *
     * @param endOfOlderMessages `true` if there are no more older messages to load, `false` otherwise.
     */
    fun setEndOfOlderMessages(endOfOlderMessages: Boolean) {
        _endOfOlderMessages.value = endOfOlderMessages
    }

    /**
     * Sets the end of newer messages state.
     *
     * @param endOfNewerMessages `true` if there are no more newer messages to load, `false` otherwise.
     */
    fun setEndOfNewerMessages(endOfNewerMessages: Boolean) {
        _endOfNewerMessages.value = endOfNewerMessages
    }

    /**
     * Trims messages from the oldest end if the limit is exceeded.
     * Call after loading newer messages or receiving new messages via WebSocket while at the end of the list.
     *
     * When trimming occurs:
     * - [endOfOlderMessages] is set to `false` since the oldest messages are no longer available.
     */
    fun trimOldestMessages() {
        applyMessageLimitIfNeeded(TrimDirection.FROM_OLDEST)
    }

    /**
     * Trims messages from the newest end if the limit is exceeded.
     * Call after loading older messages.
     *
     * When trimming occurs:
     * - [endOfNewerMessages] is set to `false` since the newest messages are no longer available.
     * - [insideSearch] is set to `true` since we're no longer viewing the latest messages.
     * - The current messages are cached before trimming to preserve them for later retrieval.
     */
    fun trimNewestMessages() {
        applyMessageLimitIfNeeded(TrimDirection.FROM_NEWEST)
    }

    /**
     * Applies the message limit by trimming messages from the specified direction.
     * Updates pagination flags accordingly.
     *
     * @param direction The direction from which to trim messages.
     */
    private fun applyMessageLimitIfNeeded(direction: TrimDirection) {
        val limit = messageLimit ?: return
        val currentSize = _messages.value.size
        if (currentSize <= limit + TRIM_BUFFER) return

        when (direction) {
            TrimDirection.FROM_OLDEST -> {
                _messages.update { it.takeLast(limit) }
                _endOfOlderMessages.value = false
            }

            TrimDirection.FROM_NEWEST -> {
                // Cache the latest messages before trimming to preserve them for later
                cacheLatestMessages()
                _messages.update { it.take(limit) }
                _endOfNewerMessages.value = false
                _insideSearch.value = true
            }
        }
    }

    /**
     * Sets whether the loading of the channel resulted in a error, and a recovery is needed.
     *
     * @param recoveryNeeded `true` if recovery is needed, `false` otherwise.
     */
    fun setRecoveryNeeded(recoveryNeeded: Boolean) {
        _recoveryNeeded = recoveryNeeded
    }

    /**
     * Sets whether the user is inside a search context in the channel.
     * IMPORTANT: InsideSearch means that the channel has loaded messages around a given message ID, and not the regular
     * latest messages.
     *
     * @param insideSearch `true` if inside search, `false` otherwise.
     */
    fun setInsideSearch(insideSearch: Boolean) {
        _insideSearch.value = insideSearch
    }

    /**
     * Caches the latest messages for the channel.
     * Call when calling `Jump to message` to preserve the current latest messages.
     */
    fun cacheLatestMessages() {
        _cachedLatestMessages.value = _messages.value
    }

    /**
     * Clears the cached latest messages for the channel.
     * Call when loading the latest messages again.
     */
    fun clearCachedLatestMessages() {
        _cachedLatestMessages.value = emptyList()
    }

    // endregion

    private fun shouldIgnoreUpsertion(message: Message): Boolean {
        // Skip messages from other shadow banned users
        val isFromCurrentUser = message.user.id == currentUser.id
        val isFromShadowBannedUser = message.shadowed
        if (!isFromCurrentUser && isFromShadowBannedUser) {
            return true
        }

        // Skip thread replies that are not shown in channel
        // Note: If we add `threads` handling directly in the channel state,
        // we would need to handle them separately
        val isThreadReply = message.parentId != null
        val isShownInChannel = message.showInChannel
        if (isThreadReply && !isShownInChannel) {
            return true
        }
        return false
    }

    private fun deleteMessages(ids: Set<String>) {
        _messages.update { current ->
            current.filterNot { ids.contains(it.id) }
        }
        _cachedLatestMessages.update { current ->
            current.filterNot { ids.contains(it.id) }
        }
        _pinnedMessages.update { current ->
            current.filterNot { ids.contains(it.id) }
        }
    }

    private fun registerPollForMessage(poll: Poll, messageId: String) {
        polls[poll.id] = poll
        val linkedMessages = messagesWithPolls[poll.id].orEmpty() + messageId
        messagesWithPolls[poll.id] = linkedMessages
    }

    fun updateReads(reads: List<ChannelUserRead>) {
        val currentUserRead = read.value
        // Root cause fix: When updating reads from server data, we should preserve local state
        // if it's more recent (has a newer lastReceivedEventDate). This prevents stale server
        // data from overwriting recent local updates, which happens when:
        // 1. Hidden channels receive messages (server doesn't track unread counts for hidden channels)
        // 2. Race conditions where updateCurrentUserRead() has updated local state but a concurrent
        //    query channels update calls updateReads() with stale server data
        // 3. When visible channels work correctly, server data is more recent, so it's used
        val readsToUpsert = if (currentUserRead != null) {
            reads.map { serverRead ->
                if (serverRead.getUserId() == currentUser.id) {
                    mergeCurrentUserRead(currentUserRead, serverRead)
                } else {
                    serverRead
                }
            }
        } else {
            reads
        }
        _reads.update { current ->
            current + readsToUpsert.associateBy(ChannelUserRead::getUserId)
        }
    }

    /**
     * Merges local and server read states for the current user.
     * Preserves local state if it's more recent, otherwise uses server data.
     *
     * @param localRead The local read state.
     * @param serverRead The server read state.
     * @return The merged read state.
     */
    private fun mergeCurrentUserRead(
        localRead: ChannelUserRead,
        serverRead: ChannelUserRead,
    ): ChannelUserRead {
        return if (localRead.lastReceivedEventDate.after(serverRead.lastReceivedEventDate)) {
            // Local state is more recent, preserve it but merge other fields from server
            logger.d {
                "[updateReads] Local read state is more recent, preserving: " +
                    "local.lastReceivedEventDate=${localRead.lastReceivedEventDate}, " +
                    "server.lastReceivedEventDate=${serverRead.lastReceivedEventDate}, " +
                    "local.unreadMessages=${localRead.unreadMessages}, " +
                    "server.unreadMessages=${serverRead.unreadMessages}"
            }
            localRead.copy(
                user = serverRead.user,
                lastRead = maxOf(localRead.lastRead, serverRead.lastRead),
                lastReadMessageId = serverRead.lastReadMessageId ?: localRead.lastReadMessageId,
                lastDeliveredAt = serverRead.lastDeliveredAt ?: localRead.lastDeliveredAt,
                lastDeliveredMessageId = serverRead.lastDeliveredMessageId ?: localRead.lastDeliveredMessageId,
                // lastReceivedEventDate and unreadMessages are preserved from local (not set in copy)
            )
        } else {
            // Server data is more recent, use it
            serverRead
        }
    }

    /**
     * Specifies the direction from which messages should be trimmed when the limit is exceeded.
     */
    private enum class TrimDirection {
        /** Trim oldest messages (beginning of the sorted list). */
        FROM_OLDEST,

        /** Trim newest messages (end of the sorted list). */
        FROM_NEWEST,
    }

    private companion object {
        /**
         * Hard limit for cached latest messages to prevent unbounded memory growth while in search mode.
         * When the user is viewing messages around a specific message (e.g., from a deep link or search),
         * new incoming messages are cached here. This limit ensures memory usage stays bounded even if
         * the user remains in search mode for an extended period in an active channel.
         */
        private const val CACHED_LATEST_MESSAGES_LIMIT = 25

        /**
         * Buffer to avoid trimming too frequently. Messages are only trimmed when the count exceeds
         * [messageLimit] + [TRIM_BUFFER].
         */
        private const val TRIM_BUFFER = 30
    }
}
