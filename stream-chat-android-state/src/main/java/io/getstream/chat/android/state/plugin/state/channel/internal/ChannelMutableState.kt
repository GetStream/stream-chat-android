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

package io.getstream.chat.android.state.plugin.state.channel.internal

import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.extensions.getCreatedAtOrDefault
import io.getstream.chat.android.client.extensions.internal.updateUsers
import io.getstream.chat.android.client.extensions.internal.wasCreatedAfter
import io.getstream.chat.android.client.extensions.syncUnreadCountWithReads
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isPinned
import io.getstream.chat.android.extensions.lastMessageAt
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessagesState
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.utils.internal.combineStates
import io.getstream.chat.android.state.utils.internal.mapState
import io.getstream.log.taggedLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

@Suppress("TooManyFunctions", "LongParameterList")
/**
 * State container with mutable data of a channel.
 *
 * @property channelType The type of the channel.
 * @property channelId The ID of the channel.
 * @property userFlow State flow providing the user once it is set.
 * @property latestUsers Flow holding the latest updated users.
 * @property activeLiveLocations Flow holding the latest live locations.
 * @property baseMessageLimit The initial limit specifying how many of the latest messages should be kept in memory. If
 * provided, the [ChannelMutableState] will try to keep the number of messages in memory below this limit. Inserting new
 * messages in the channel (ex. from `message.new` event) will remove the oldest messages if the limit is exceeded. When
 * older messages are loaded (ex. by scrolling upwards), the limit is increased by a factor of [LIMIT_MULTIPLIER] if the
 * number of messages in the channel exceeds the limit. This ensures that the channel limit is not exceeded when loading
 * older messages. However, this means that the [baseMessageLimit] is not guaranteed to be the maximum number of
 * messages in the channel.
 * @property now Function providing the current time in milliseconds. Used to determine if a message is pinned or not.
 */
internal class ChannelMutableState(
    override val channelType: String,
    override val channelId: String,
    private val userFlow: StateFlow<User?>,
    latestUsers: StateFlow<Map<String, User>>,
    activeLiveLocations: StateFlow<List<Location>>,
    val baseMessageLimit: Int?,
    private val now: () -> Long,
) : ChannelState {

    override val cid: String = "%s:%s".format(channelType, channelId)

    private val seq = seqGenerator.incrementAndGet()
    private val logger by taggedLogger("Chat:ChannelState-$seq")

    private var messageLimit: Int? = baseMessageLimit

    private var _messages: MutableStateFlow<Map<String, Message>>? = MutableStateFlow(emptyMap())
    private var _pinnedMessages: MutableStateFlow<Map<String, Message>>? = MutableStateFlow(emptyMap())
    private var _typing: MutableStateFlow<TypingEvent>? = MutableStateFlow(TypingEvent(channelId, emptyList()))
    private var _typingChatEvents: MutableStateFlow<Map<String, TypingStartEvent>>? = MutableStateFlow(emptyMap())
    private var _rawReads: MutableStateFlow<Map<String, ChannelUserRead>>? = MutableStateFlow(emptyMap())
    private var rawReads: StateFlow<Map<String, ChannelUserRead>> = _rawReads!!
    private var _members: MutableStateFlow<Map<String, Member>>? = MutableStateFlow(emptyMap())
    private var _oldMessages: MutableStateFlow<Map<String, Message>>? = MutableStateFlow(emptyMap())
    private var _watchers: MutableStateFlow<Map<String, User>>? = MutableStateFlow(emptyMap())
    private var _watcherCount: MutableStateFlow<Int>? = MutableStateFlow(0)
    private var _endOfNewerMessages: MutableStateFlow<Boolean>? = MutableStateFlow(true)
    private var _endOfOlderMessages: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _loading: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _hidden: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _muted: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _channelData: MutableStateFlow<ChannelData?>? = MutableStateFlow(null)
    private var _repliedMessage: MutableStateFlow<Message?>? = MutableStateFlow(null)
    private var _quotedMessagesMap: MutableStateFlow<MutableMap<String, List<String>>>? =
        MutableStateFlow(mutableMapOf())
    private var _membersCount: MutableStateFlow<Int>? = MutableStateFlow(0)
    private var _insideSearch: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _loadingOlderMessages: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _loadingNewerMessages: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private val deletedMessagesIds: Set<String>
        get() = _messages
            ?.value
            ?.values
            ?.mapNotNull { it.takeIf { it.isDeleted() }?.id }
            ?.toSet()
            ?: emptySet()

    /** Channel config data. */
    private var _channelConfig: MutableStateFlow<Config>? = MutableStateFlow(Config())

    override val hidden: StateFlow<Boolean> = _hidden!!
    override val muted: StateFlow<Boolean> = _muted!!
    override val loading: StateFlow<Boolean> = _loading!!
    override val loadingOlderMessages: StateFlow<Boolean> = _loadingOlderMessages!!
    override val loadingNewerMessages: StateFlow<Boolean> = _loadingNewerMessages!!
    override val endOfOlderMessages: StateFlow<Boolean> = _endOfOlderMessages!!

    override val endOfNewerMessages: StateFlow<Boolean> = _endOfNewerMessages!!
    override val messagesCount: StateFlow<Int?> = _channelData!!.mapState { it?.messagesCount }
    override val activeLiveLocations: StateFlow<List<Location>> = activeLiveLocations.mapState { locations ->
        locations.filter { it.cid == cid }
    }

    /** the data to hide messages before */
    var hideMessagesBefore: Date? = null

    /**
     * Messages to show on the channels list when the state is inside search to show the latest message in the preview.
     */
    internal val cachedLatestMessages: MutableStateFlow<Map<String, Message>> = MutableStateFlow(emptyMap())

    /** The raw message list updated by recent users value. */
    val messageList: StateFlow<List<Message>> =
        combineStates(_messages!!, latestUsers) { messageMap, userMap -> messageMap.values.updateUsers(userMap) }

    val pinnedMessagesList: StateFlow<List<Message>> =
        combineStates(_pinnedMessages!!, latestUsers) { pinnedMessagesMap, userMap ->
            pinnedMessagesMap.values.filter { it.isPinned(now) }.updateUsers(userMap)
        }

    val rawPinnedMessages get() = _pinnedMessages?.value?.filterValues { it.isPinned(now) }.orEmpty()

    /** a list of messages sorted by message.createdAt */
    private val sortedVisibleMessages: StateFlow<List<Message>> =
        messagesTransformation(messageList)

    /** a list of pinned messages sorted by message.createdAt */
    private val sortedVisiblePinnedMessages: StateFlow<List<Message>> =
        messagesTransformation(pinnedMessagesList) { it.isPinned(now) }

    override val messagesState: StateFlow<MessagesState> =
        combineStates(loading, sortedVisibleMessages) { loading: Boolean, messages: List<Message> ->
            when {
                loading -> MessagesState.Loading
                messages.isEmpty() -> MessagesState.OfflineNoResults
                else -> MessagesState.Result(messages)
            }
        }

    private fun messagesTransformation(
        messages: StateFlow<Collection<Message>>,
        extraPredicate: (Message) -> Boolean = { true },
    ): StateFlow<List<Message>> {
        return combineStates(messages, userFlow) { messageCollection, user ->
            messageCollection.asSequence()
                .filter { it.parentId == null || it.showInChannel }
                .filter { it.user.id == user?.id || !it.shadowed }
                .filter(this::isMessageVisible)
                .filter(extraPredicate)
                .sortedBy { it.createdAt ?: it.createdLocallyAt }
                .toList()
        }
    }

    /** The date of the last typing event. */
    var lastStartTypingEvent: Date? = null
    internal var keystrokeParentMessageId: String? = null

    internal val visibleMessages: StateFlow<Map<String, Message>> = messageList.mapState { messages ->
        messages.filter(this::isMessageVisible)
            .associateBy(Message::id)
    }

    internal val visiblePinnedMessages: StateFlow<Map<String, Message>> = pinnedMessagesList.mapState { messages ->
        messages.filter(this::isMessageVisible)
            .associateBy(Message::id)
    }

    /** Sorted version of messages. */
    val sortedMessages: StateFlow<List<Message>> = visibleMessages.mapState { messagesMap ->
        messagesMap.values.sortedBy { message -> message.createdAt ?: message.createdLocallyAt }
    }

    /** Sorted version of messages. */
    val sortedPinnedMessages: StateFlow<List<Message>> = visiblePinnedMessages.mapState { messagesMap ->
        messagesMap.values.sortedBy { message -> message.createdAt ?: message.createdLocallyAt }
    }

    override val repliedMessage: StateFlow<Message?> = _repliedMessage!!

    override val quotedMessagesMap: StateFlow<Map<String, List<String>>> = _quotedMessagesMap!!

    /** Channel config data */
    override val channelConfig: StateFlow<Config> = _channelConfig!!

    override val messages: StateFlow<List<Message>> = sortedVisibleMessages

    override val pinnedMessages: StateFlow<List<Message>> = sortedVisiblePinnedMessages

    override val oldMessages: StateFlow<List<Message>> = messagesTransformation(_oldMessages!!.mapState { it.values })
    override val watcherCount: StateFlow<Int> = _watcherCount!!

    override val watchers: StateFlow<List<User>> =
        combineStates(_watchers!!, latestUsers) { watcherMap, userMap -> watcherMap.values.updateUsers(userMap) }
            .mapState { it.sortedBy(User::createdAt) }

    override val typing: StateFlow<TypingEvent> = _typing!!

    override val reads: StateFlow<List<ChannelUserRead>> = rawReads
        .mapState { it.values.sortedBy(ChannelUserRead::lastRead) }

    override val read: StateFlow<ChannelUserRead?> =
        combineStates(rawReads, userFlow) { readsMap, user -> user?.id?.let { readsMap[it] } }

    override val unreadCount: StateFlow<Int> = read.mapState { it?.unreadMessages ?: 0 }

    override val members: StateFlow<List<Member>> =
        combineStates(_members!!, latestUsers) { membersMap, usersMap -> membersMap.values.updateUsers(usersMap) }
            .mapState { it.sortedBy(Member::createdAt) }

    override val membersCount: StateFlow<Int> = _membersCount!!

    override val channelData: StateFlow<ChannelData> =
        combineStates(_channelData!!, latestUsers) { channelData, users ->
            if (channelData == null) {
                ChannelData(
                    type = channelType,
                    id = channelId,
                )
            } else {
                val result = if (users.containsKey(channelData.createdBy.id)) {
                    channelData.copy(createdBy = users[channelData.createdBy.id] ?: channelData.createdBy)
                } else {
                    channelData
                }
                result
            }
        }

    /** If we need to recover state when connection established again. */
    override var recoveryNeeded: Boolean = false

    override val insideSearch: StateFlow<Boolean> = _insideSearch!!

    override val lastSentMessageDate: StateFlow<Date?> = combineStates(
        userFlow,
        channelConfig,
        messages,
    ) { user, config, messages ->
        user?.id?.let { userId ->
            messages
                .filter { it.user.id == userId }
                .lastMessageAt(config.skipLastMsgUpdateForSystemMsgs)
        }
    }

    override fun toChannel(): Channel {
        // recreate a channel object from the various observables.
        return channelData.value
            .toChannel(
                messages = sortedMessages.value,
                cachedLatestMessages = cachedLatestMessages.value.values.toList(),
                members = members.value,
                reads = rawReads.value.values.toList(),
                watchers = watchers.value,
                watcherCount = watcherCount.value,
                insideSearch = insideSearch.value,
            )
            .copy(
                config = channelConfig.value,
                hidden = hidden.value,
                pinnedMessages = sortedPinnedMessages.value,
                activeLiveLocations = activeLiveLocations.value,
            ).syncUnreadCountWithReads()
    }

    /**
     * Sets the channel to be loading older messages.
     *
     * @param isLoading Boolean.
     */
    fun setLoadingOlderMessages(isLoading: Boolean) {
        _loadingOlderMessages?.value = isLoading
        val currentMessageCount = _messages?.value?.size ?: return
        if (isLoading) {
            messageLimit = messageLimit?.let { limit ->
                val multiplier = LIMIT_MULTIPLIER.takeIf {
                    currentMessageCount + TRIM_BUFFER >= limit
                } ?: NEUTRAL_MULTIPLIER
                (limit * multiplier).toInt()
            }
        }
    }

    /**
     * Sets the channel to be loading newer messages.
     *
     * @param isLoading Boolean.
     */
    fun setLoadingNewerMessages(isLoading: Boolean) {
        _loadingNewerMessages?.value = isLoading
    }

    /** Sets the end for newer messages. */
    fun setEndOfNewerMessages(isEnd: Boolean) {
        _endOfNewerMessages?.value = isEnd
    }

    /**
     * Sets the end for older messages.
     *
     * @param isEnd Boolean
     */
    fun setEndOfOlderMessages(isEnd: Boolean) {
        _endOfOlderMessages?.value = isEnd
    }

    /**
     * Sets loadings.
     *
     * @param isLoading Boolean.
     */
    fun setLoading(isLoading: Boolean) {
        _loading?.value = isLoading
    }

    /**
     * Sets hidden.
     *
     * @param isHidden Boolean
     */
    fun setHidden(isHidden: Boolean) {
        _hidden?.value = isHidden
    }

    /**
     * Sets muted.
     *
     * @param isMuted Boolean.
     */
    fun setMuted(isMuted: Boolean) {
        _muted?.value = isMuted
    }

    /** Sets [ChannelData]. */
    fun setChannelData(channelData: ChannelData) {
        _channelData?.value = channelData
    }

    fun updateChannelData(update: (ChannelData?) -> ChannelData?) {
        _channelData?.value = update(_channelData?.value)
    }

    /**
     * Sets replied message.
     *
     * @param repliedMessage [Message]
     */
    fun setRepliedMessage(repliedMessage: Message?) {
        _repliedMessage?.value = repliedMessage
    }

    /**
     *  Sets member count.
     *
     * @param count Int.
     */
    fun setMembersCount(count: Int) {
        _membersCount?.value = count
    }

    /** Sets inside search. This must be set when a search is started in the channel and the
     * user is looks sort a older message using the search functionality.
     *
     * @param isInsideSearch Boolean.
     * */
    fun setInsideSearch(isInsideSearch: Boolean) {
        when {
            isInsideSearch && !insideSearch.value -> {
                cacheLatestMessages()
            }

            !isInsideSearch && insideSearch.value -> {
                cachedLatestMessages.value = emptyMap()
            }
        }

        _insideSearch?.value = isInsideSearch
    }

    /**
     * Set channel config
     *
     * @param channelConfig [Config]
     */
    fun setChannelConfig(channelConfig: Config) {
        _channelConfig?.value = channelConfig
    }

    /**
     * Adds a quoted message to the state.
     */
    fun addQuotedMessage(quotedMessageId: String, quotingMessageId: String) {
        _quotedMessagesMap?.apply {
            val quotesMap = value
            quotesMap[quotedMessageId] = quotesMap[quotedMessageId]?.plus(quotingMessageId) ?: listOf(quotingMessageId)
            value = quotesMap
        }
    }

    /**
     * Updates StateFlows related to typing updates.
     */
    fun updateTypingEvents(eventsMap: Map<String, TypingStartEvent>, typingEvent: TypingEvent) {
        _typingChatEvents?.value = eventsMap
        _typing?.value = typingEvent
    }

    /**
     * Upsert members in the channel.
     *
     * @param members list of members to be upserted.
     */
    fun upsertMembers(members: List<Member>) {
        logger.d { "[upsertMembers] member.ids: ${members.map { it.getUserId() }}" }
        val membersMap = members.associateBy(Member::getUserId)
        _members?.apply { value = value + membersMap }
    }

    fun setMembers(members: List<Member>, membersCount: Int) {
        logger.d { "[setMembers] member.ids: ${members.map { it.getUserId() }}" }
        _members?.value = members.associateBy(Member::getUserId)
        _membersCount?.value = membersCount
    }

    /**
     * Add a member
     *
     * @param member The member to be added.
     */
    fun addMember(member: Member) {
        logger.d { "[addMember] member.id: ${member.getUserId()}" }
        _membersCount?.value = membersCount.value +
            (1.takeUnless { _members?.value?.keys?.contains(member.getUserId()) == true } ?: 0)
        upsertMembers(listOf(member))
    }

    /**
     * Deletes a member.
     *
     * @param member The member to be removed.
     */
    fun deleteMember(member: Member) {
        logger.d { "[deleteMember] member.id: ${member.getUserId()}" }
        _members?.let {
            _membersCount?.value = membersCount.value - it.value.count { it.key == member.getUserId() }
            it.value = it.value - member.getUserId()
        }
        _watchers?.let {
            deleteWatcher(
                member.user,
                watcherCount.value - it.value.count { it.key == member.getUserId() },
            )
        }
    }

    /**
     * Deletes a watcher.
     *
     * @param user The user to be removed.
     * @param watchersCount The current number of watchers.
     */
    internal fun deleteWatcher(user: User, watchersCount: Int) {
        logger.v { "[deleteWatcher] user.id: ${user.id}, watchersCount: $watchersCount" }
        _watchers?.apply {
            value = value - user.id
            _watcherCount?.value = watchersCount.takeUnless { it < 0 } ?: value.size
        }
    }

    fun deleteMessage(message: Message) {
        logger.v { "[deleteMessage] message.id: ${message.id}" }
        _messages?.apply { value = value - message.id }
        setPinned { pinned -> pinned - message.id }
    }

    fun deletePinnedMessage(message: Message) {
        logger.v { "[deletePinnedMessage] message.id=${message.id}, message.text=${message.text}" }
        setPinned { pinned -> pinned - message.id }
    }

    fun upsertWatchers(watchers: List<User>, watchersCount: Int) {
        logger.v { "[upsertWatchers] watchers.ids: ${watchers.map { it.id }}, watchersCount: $watchersCount" }
        _watchers?.apply {
            value = value + watchers.associateBy(User::id)
            _watcherCount?.value = watchersCount.takeUnless { it < 0 } ?: value.size
        }
    }

    fun setWatchers(watchers: List<User>, watchersCount: Int) {
        logger.v { "[setWatchers] watchers.ids: ${watchers.map { it.id }}, watchersCount: $watchersCount" }
        _watchers?.value = watchers.associateBy(User::id)
        _watcherCount?.value = watchersCount
    }

    /**
     * Upsert message in the channel.
     *
     * @param message message to be upserted.
     */
    fun upsertMessage(message: Message) {
        upsertMessages(listOf(message))
    }

    fun upsertUserPresence(user: User) {
        logger.d { "[upsertUserPresence] user.id: ${user.id}" }
        _members?.value?.get(user.id)?.copy(user = user)?.let { upsertMembers(listOf(it)) }
        user.takeIf { _watchers?.value?.any { it.key == user.id } == true }
            ?.let { upsertWatchers(listOf(it), watcherCount.value) }
        _channelData?.value?.takeIf { it.createdBy.id == user.id }
            ?.let { setChannelData(it.copy(createdBy = user)) }
        _messages?.apply {
            value = value.values.updateUsers(mapOf(user.id to user)).associateBy { it.id }
        }
        _pinnedMessages?.apply { value = value.updateUsers(mapOf(user.id to user)) }
    }

    fun upsertReads(reads: List<ChannelUserRead>) {
        _rawReads?.apply {
            value = value + reads.associateBy(ChannelUserRead::getUserId)
        }
    }

    /**
     * Marks channel as read locally if different conditions are met:
     * 1. Channel has read events enabled
     * 2. Channel has messages not marked as read yet
     * 3. Current user is set
     *
     * @return The flag to determine if the channel was marked as read locally.
     */
    fun markChannelAsRead(): Boolean = read.value
        ?.takeIf { channelConfig.value.readEventsEnabled }
        ?.let { currentUserRead ->
            messages.value.lastOrNull()?.let { lastMessage ->
                upsertReads(
                    listOf(
                        currentUserRead.copy(
                            lastReceivedEventDate = lastMessage.getCreatedAtOrDefault(Date()),
                            lastRead = lastMessage.getCreatedAtOrDefault(Date()),
                            unreadMessages = 0,
                        ),
                    ),
                )
                true
            }
        } ?: false

    fun removeMessagesBefore(date: Date) {
        logger.d { "[removeMessagesBefore] date: $date" }
        _messages?.apply { value = value.filter { it.value.wasCreatedAfter(date) } }
        setPinned { pinned -> pinned.filter { it.value.wasCreatedAfter(date) } }
    }

    fun upsertMessages(updatedMessages: Collection<Message>) {
        _messages?.apply {
            val newMessageList = (value + (updatedMessages.associateBy(Message::id) - deletedMessagesIds)).values
            value = applyMessageLimitIfNeeded(newMessageList).associateBy(Message::id)
        }
        _pinnedMessages?.value
            ?.let { pinnedMessages ->
                val pinnedMessageIds = pinnedMessages.keys
                updatedMessages
                    .filter { pinnedMessageIds.contains(it.id) }
                    .let { upsertPinnedMessages(it) }
            }
    }

    fun setMessages(messages: List<Message>) {
        _messages?.value = applyMessageLimitIfNeeded(messages).associateBy(Message::id)
    }

    fun setPinnedMessages(messages: List<Message>) {
        logger.d { "[setPinnedMessages] messages.size: ${messages.size}" }
        setPinned { messages.associateBy(Message::id) }
    }

    fun upsertPinnedMessages(messages: Collection<Message>) {
        logger.d { "[upsertPinnedMessages] messages.size: ${messages.size}" }
        setPinned { pinned -> pinned + (messages.associateBy(Message::id) - deletedMessagesIds) }
    }

    private inline fun setPinned(producer: (Map<String, Message>) -> Map<String, Message>) {
        val curPinnedMessages = _pinnedMessages?.value
        curPinnedMessages ?: return
        val newPinnedMessages = producer(curPinnedMessages).filterValues { it.isPinned(now) }
        logger.v { "[setPinned] pinned.size: ${curPinnedMessages.size} => ${newPinnedMessages.size}" }
        _pinnedMessages?.value = newPinnedMessages
    }

    /**
     * Checks if the given message is visible based on the `hideMessagesBefore` timestamp.
     *
     * This function returns `true` if `hideMessagesBefore` is `null` or if the message was created
     * after the `hideMessagesBefore` timestamp.
     *
     * @param message The message to check for visibility.
     * @return `true` if the message is visible, `false` otherwise.
     */
    private fun isMessageVisible(message: Message): Boolean {
        return hideMessagesBefore == null || message.wasCreatedAfter(hideMessagesBefore)
    }

    private fun cacheLatestMessages() {
        cachedLatestMessages.value = sortedMessages.value.associateBy(Message::id)
    }

    /**
     * Updates the cached messages with new messages.
     */
    fun updateCachedLatestMessages(messages: Map<String, Message>) {
        cachedLatestMessages.value = messages
    }

    /**
     * Resets the current message limit to the [baseMessageLimit].
     */
    internal fun resetMessageLimit() {
        messageLimit = baseMessageLimit
    }

    override fun getMessageById(id: String): Message? = _messages?.value?.get(id) ?: _pinnedMessages?.value?.get(id)

    internal fun destroy() {
        _messages = null
        _pinnedMessages = null
        _typing = null
        _typingChatEvents = null
        _rawReads = null
        _members = null
        _oldMessages = null
        _watchers = null
        _watcherCount = null
        _endOfNewerMessages = null
        _endOfOlderMessages = null
        _loading = null
        _hidden = null
        _muted = null
        _channelData = null
        _repliedMessage = null
        _quotedMessagesMap = null
        _membersCount = null
        _insideSearch = null
        _loadingOlderMessages = null
        _loadingNewerMessages = null
        _channelConfig = null
    }

    @Suppress("ComplexMethod")
    private fun applyMessageLimitIfNeeded(messages: Collection<Message>): Collection<Message> {
        // If no message limit is set or we are loading older messages, restriction is not applied
        if (messageLimit == null || loadingOlderMessages.value) {
            return messages
        }
        // Add buffer to avoid trimming too often
        return if (messages.size > messageLimit!! + TRIM_BUFFER) {
            val trimmedMessages = messages
                .sortedBy { it.createdAt ?: it.createdLocallyAt }
                .takeLast(messageLimit!!)
            // Set end of older messages to false, as we trimmed the messages
            setEndOfOlderMessages(false)
            trimmedMessages
        } else {
            messages
        }
    }

    private companion object {
        private val seqGenerator = AtomicInteger()

        private const val TRIM_BUFFER = 30
        private const val NEUTRAL_MULTIPLIER = 1.0
        private const val LIMIT_MULTIPLIER = 1.5
    }
}
