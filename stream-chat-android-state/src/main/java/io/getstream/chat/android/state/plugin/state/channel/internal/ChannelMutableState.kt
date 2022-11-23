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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.getstream.chat.android.state.plugin.state.channel.internal

import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.extensions.internal.updateUsers
import io.getstream.chat.android.client.extensions.internal.wasCreatedAfter
import io.getstream.chat.android.core.utils.date.inOffsetWith
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessagesState
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Date

@Suppress("TooManyFunctions")
/** State container with mutable data of a channel.*/
internal class ChannelMutableState(
    override val channelType: String,
    override val channelId: String,
    private val scope: CoroutineScope,
    private val userFlow: StateFlow<User?>,
    latestUsers: StateFlow<Map<String, User>>,
) : ChannelState {

    override val cid: String = "%s:%s".format(channelType, channelId)

    private val _messages = MutableStateFlow<Map<String, Message>>(emptyMap())
    private val _countedMessage: MutableSet<String> = mutableSetOf()
    private val _typing = MutableStateFlow(TypingEvent(channelId, emptyList()))
    private val _typingChatEvents = MutableStateFlow<Map<String, TypingStartEvent>>(emptyMap())
    private val _rawReads = MutableStateFlow<Map<String, ChannelUserRead>>(emptyMap())
    private val _members = MutableStateFlow<Map<String, Member>>(emptyMap())
    private val _oldMessages = MutableStateFlow<Map<String, Message>>(emptyMap())
    private val _watchers = MutableStateFlow<Map<String, User>>(emptyMap())
    private val _watcherCount = MutableStateFlow(0)
    private val _endOfNewerMessages = MutableStateFlow(true)
    private val _endOfOlderMessages = MutableStateFlow(false)
    private val _loading = MutableStateFlow(false)
    private val _hidden = MutableStateFlow(false)
    private val _muted = MutableStateFlow(false)
    private val _channelData = MutableStateFlow<ChannelData?>(null)
    private val _repliedMessage = MutableStateFlow<Message?>(null)
    private val _membersCount = MutableStateFlow(0)
    private val _insideSearch = MutableStateFlow(false)
    private val _loadingOlderMessages = MutableStateFlow(false)
    private val _loadingNewerMessages = MutableStateFlow(false)
    private val _lastSentMessageDate = MutableStateFlow<Date?>(null)

    /** Channel config data. */
    private val _channelConfig: MutableStateFlow<Config> = MutableStateFlow(Config())

    /** the data to hide messages before */
    var hideMessagesBefore: Date? = null

    /**
     * Messages to show on the channels list when the state is inside search to show the latest message in the preview.
     */
    internal val cachedLatestMessages: MutableStateFlow<Map<String, Message>> = MutableStateFlow(emptyMap())

    /** The raw message list updated by recent users value. */
    val messageList: StateFlow<List<Message>> =
        _messages.combine(latestUsers) { messageMap, userMap -> messageMap.values.updateUsers(userMap) }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** a list of messages sorted by message.createdAt */
    private val sortedVisibleMessages: StateFlow<List<Message>> =
        messagesTransformation(messageList).stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _messagesState: StateFlow<MessagesState> =
        _loading.combine(sortedVisibleMessages) { loading: Boolean, messages: List<Message> ->
            when {
                loading -> MessagesState.Loading
                messages.isEmpty() -> MessagesState.OfflineNoResults
                else -> MessagesState.Result(messages)
            }
        }.stateIn(scope, SharingStarted.Eagerly, MessagesState.NoQueryActive)

    private fun messagesTransformation(messages: Flow<Collection<Message>>): StateFlow<List<Message>> {
        return messages.combine(userFlow) { messageCollection, user ->
            messageCollection.asSequence()
                .filter { it.parentId == null || it.showInChannel }
                .filter { it.user.id == user?.id || !it.shadowed }
                .filter { hideMessagesBefore == null || it.wasCreatedAfter(hideMessagesBefore) }
                .sortedBy { it.createdAt ?: it.createdLocallyAt }
                .toList()
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())
    }

    /** The date of the last typing event. */
    var lastStartTypingEvent: Date? = null
    internal var keystrokeParentMessageId: String? = null

    internal val visibleMessages: StateFlow<Map<String, Message>> = messageList.mapLatest { messages ->
        messages.filter { message -> hideMessagesBefore == null || message.wasCreatedAfter(hideMessagesBefore) }
            .associateBy(Message::id)
    }.stateIn(scope, SharingStarted.Eagerly, emptyMap())

    /** Sorted version of messages. */
    val sortedMessages: StateFlow<List<Message>> = visibleMessages.mapLatest { messagesMap ->
        messagesMap.values.sortedBy { message -> message.createdAt ?: message.createdLocallyAt }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val repliedMessage: StateFlow<Message?> = _repliedMessage

    /** Channel config data */
    override val channelConfig: StateFlow<Config> = _channelConfig

    override val messages: StateFlow<List<Message>> = sortedVisibleMessages

    override val messagesState: StateFlow<MessagesState> = _messagesState
    override val oldMessages: StateFlow<List<Message>> = messagesTransformation(_oldMessages.map { it.values })
    override val watcherCount: StateFlow<Int> = _watcherCount

    override val watchers: StateFlow<List<User>> =
        _watchers.combine(latestUsers) { watcherMap, userMap -> watcherMap.values.updateUsers(userMap) }
            .map { it.sortedBy(User::createdAt) }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val typing: StateFlow<TypingEvent> = _typing

    override val reads: StateFlow<List<ChannelUserRead>> = _rawReads
        .map { it.values.sortedBy(ChannelUserRead::lastRead) }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val read: StateFlow<ChannelUserRead?> = _rawReads
        .combine(userFlow) { readsMap, user -> user?.id?.let { readsMap[it] } }
        .stateIn(scope, SharingStarted.Eagerly, null)

    val lastMarkReadEvent: StateFlow<Date?> = read.mapLatest { it?.lastRead }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override val unreadCount: StateFlow<Int> = read.mapLatest { it?.unreadMessages ?: 0 }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    override val members: StateFlow<List<Member>> = _members
        .combine(latestUsers) { membersMap, usersMap -> membersMap.values.updateUsers(usersMap) }
        .map { it.sortedBy(Member::createdAt) }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val membersCount: StateFlow<Int> = _membersCount

    override val channelData: StateFlow<ChannelData> =
        combine(_channelData.filterNotNull(), latestUsers) { channelData, users ->
            if (users.containsKey(channelData.createdBy.id)) {
                channelData.copy(createdBy = users[channelData.createdBy.id] ?: channelData.createdBy)
            } else {
                channelData
            }
        }.stateIn(
            scope,
            SharingStarted.Eagerly,
            ChannelData(
                type = channelType,
                id = channelId,
            )
        )

    override val hidden: StateFlow<Boolean> = _hidden
    override val muted: StateFlow<Boolean> = _muted
    override val loading: StateFlow<Boolean> = _loading
    override val loadingOlderMessages: StateFlow<Boolean> = _loadingOlderMessages
    override val loadingNewerMessages: StateFlow<Boolean> = _loadingNewerMessages
    override val endOfOlderMessages: StateFlow<Boolean> = _endOfOlderMessages
    override val endOfNewerMessages: StateFlow<Boolean> = _endOfNewerMessages

    /** If we need to recover state when connection established again. */
    override var recoveryNeeded: Boolean = false

    override val insideSearch: StateFlow<Boolean> = _insideSearch

    override val lastSentMessageDate: StateFlow<Date?> = _lastSentMessageDate

    override fun toChannel(): Channel {
        // recreate a channel object from the various observables.
        val channelData = channelData.value

        val messages = sortedMessages.value
        val cachedMessages = cachedLatestMessages.value.values.toList()
        val members = members.value
        val watchers = watchers.value
        val reads = _rawReads.value.values.toList()
        val watcherCount = _watcherCount.value
        val insideSearch = _insideSearch.value

        val channel = channelData
            .toChannel(messages, cachedMessages, members, reads, watchers, watcherCount, insideSearch)
        channel.config = _channelConfig.value
        channel.unreadCount = unreadCount.value
        channel.hidden = _hidden.value
        channel.isInsideSearch = _insideSearch.value
        channel.cachedLatestMessages = cachedLatestMessages.value.values.toList()

        return channel
    }

    /**
     * Sets the channel to be loading older messages.
     *
     * @param isLoading Boolean.
     */
    fun setLoadingOlderMessages(isLoading: Boolean) {
        _loadingOlderMessages.value = isLoading
    }

    /**
     * Sets the channel to be loading newer messages.
     *
     * @param isLoading Boolean.
     */
    fun setLoadingNewerMessages(isLoading: Boolean) {
        _loadingNewerMessages.value = isLoading
    }

    /** Sets the end for newer messages. */
    fun setEndOfNewerMessages(isEnd: Boolean) {
        _endOfNewerMessages.value = isEnd
    }

    /**
     * Sets the end for older messages.
     *
     * @param isEnd Boolean
     */
    fun setEndOfOlderMessages(isEnd: Boolean) {
        _endOfOlderMessages.value = isEnd
    }

    /**
     * Sets loadings.
     *
     * @param isLoading Boolean.
     */
    fun setLoading(isLoading: Boolean) {
        _loading.value = isLoading
    }

    /**
     * Sets hidden.
     *
     * @param isHidden Boolean
     */
    fun setHidden(isHidden: Boolean) {
        _hidden.value = isHidden
    }

    /**
     * Sets muted.
     *
     * @param isMuted Boolean.
     */
    fun setMuted(isMuted: Boolean) {
        _muted.value = isMuted
    }

    /** Sets [ChannelData]. */
    fun setChannelData(channelData: ChannelData) {
        _channelData.value = channelData
    }

    /**
     * Sets replied message.
     *
     * @param repliedMessage [Message]
     */
    fun setRepliedMessage(repliedMessage: Message?) {
        _repliedMessage.value = repliedMessage
    }

    /**
     *  Sets member count.
     *
     * @param count Int.
     */
    fun setMembersCount(count: Int) {
        _membersCount.value = count
    }

    /** Sets inside search. This must be set when a search is started in the channel and the
     * user is looks sort a older message using the search functionality.
     *
     * @param isInsideSearch Boolean.
     * */
    fun setInsideSearch(isInsideSearch: Boolean) {
        when {
            isInsideSearch && !_insideSearch.value -> {
                cacheLatestMessages()
            }

            !isInsideSearch && _insideSearch.value -> {
                cachedLatestMessages.value = emptyMap()
            }
        }

        _insideSearch.value = isInsideSearch
    }

    /**
     * Sets the date of the last message sent by the current user.
     *
     * @param lastSentMessageDate The date of the last message.
     */
    fun setLastSentMessageDate(lastSentMessageDate: Date?) {
        _lastSentMessageDate.value = lastSentMessageDate
    }

    /**
     * Set channel config
     *
     * @param channelConfig [Config]
     */
    fun setChannelConfig(channelConfig: Config) {
        _channelConfig.value = channelConfig
    }

    /**
     * Updates StateFlows related to typing updates.
     */
    fun updateTypingEvents(eventsMap: Map<String, TypingStartEvent>, typingEvent: TypingEvent) {
        _typingChatEvents.value = eventsMap
        _typing.value = typingEvent
    }

    /**
     * Upsert members in the channel.
     *
     * @param members list of members to be upserted.
     */
    fun upsertMembers(members: List<Member>) {
        val membersMap = members.associateBy(Member::getUserId)
        _members.value = _members.value + membersMap
    }

    /**
     * Add a member
     *
     * @param member The member to be added.
     */
    fun addMember(member: Member) {
        _membersCount.value += 1.takeUnless { _members.value.keys.contains(member.getUserId()) } ?: 0
        upsertMembers(listOf(member))
    }

    /**
     * Deletes a member.
     *
     * @param member The member to be removed.
     */
    fun deleteMember(member: Member) {
        _membersCount.value -= _members.value.count { it.key == member.getUserId() }
        _members.value = _members.value - member.getUserId()
        deleteWatcher(
            member.user,
            _watcherCount.value - _watchers.value.count { it.key == member.getUserId() }
        )
    }

    /**
     * Deletes a watcher.
     *
     * @param user The user to be removed.
     * @param watchersCount The current number of watchers.
     */
    internal fun deleteWatcher(user: User, watchersCount: Int) {
        _watchers.value = _watchers.value - user.id
        _watcherCount.value = watchersCount.takeUnless { it < 0 } ?: _watchers.value.size
    }

    fun deleteMessage(message: Message, updateCount: Boolean = true) {
        _messages.value = _messages.value - message.id

        if (updateCount) {
            _countedMessage.remove(message.id)
        }
    }

    fun upsertWatchers(watchers: List<User>, watchersCount: Int) {
        _watchers.value += watchers.associateBy(User::id)
        _watcherCount.value = watchersCount.takeUnless { it == 0 } ?: _watchers.value.size
    }

    /**
     * Upsert message in the channel.
     *
     * @param message message to be upserted.
     */
    fun upsertMessage(message: Message, updateCount: Boolean = true) {
        _messages.value = _messages.value + (message.id to message)

        if (updateCount) {
            _countedMessage.add(message.id)
        }
    }

    fun upsertUserPresence(user: User) {
        _members.value[user.id]?.copy(user = user)?.let { upsertMembers(listOf(it)) }
        user.takeIf { _watchers.value.any { it.key == user.id } }
            ?.let { upsertWatchers(listOf(it), _watcherCount.value) }
    }

    fun increaseReadWith(message: Message) {
        val user = userFlow.value ?: return
        val newUserRead = (read.value ?: ChannelUserRead(user)).let { currentUserRead ->
            currentUserRead.copy(
                user = user,
                unreadMessages = ++currentUserRead.unreadMessages,
                lastMessageSeenDate = message.createdAt,
            )
        }
        _rawReads.value = _rawReads.value + (user.id to newUserRead)
    }

    fun upsertReads(reads: List<ChannelUserRead>) {
        val currentUser = userFlow.value
        val currentUserRead = read.value
        val lastRead = currentUserRead?.lastRead
        val incomingUserRead = currentUser?.id?.let { userId -> reads.firstOrNull { it.user.id == userId } }
        val newUserRead = when {
            incomingUserRead == null -> currentUserRead
            currentUserRead == null -> incomingUserRead
            lastRead == null -> incomingUserRead
            incomingUserRead.lastRead?.inOffsetWith(lastRead, OFFSET_EVENT_TIME) == true -> incomingUserRead
            else -> currentUserRead
        }
        _rawReads.value = _rawReads.value +
            reads.associateBy(ChannelUserRead::getUserId) +
            listOfNotNull(newUserRead).associateBy(ChannelUserRead::getUserId)
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
                        currentUserRead.copy(lastRead = lastMessage.let { it.createdAt ?: it.createdLocallyAt })
                    )
                )
                true
            }
        } ?: false

    fun removeMessagesBefore(date: Date) {
        _messages.value = _messages.value.filter { it.value.wasCreatedAfter(date) }
    }

    fun upsertMessages(updatedMessages: Collection<Message>, updateCount: Boolean = true) {
        _messages.value += updatedMessages.associateBy(Message::id)

        if (updateCount) {
            _countedMessage.addAll(updatedMessages.map { it.id })
        }
    }

    fun setMessages(messages: List<Message>) {
        _messages.value = messages.associateBy(Message::id)
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

    fun clearCountedMessages() {
        _countedMessage.clear()
    }

    fun insertCountedMessages(ids: List<String>) {
        _countedMessage.addAll(ids)
    }

    fun isMessageAlreadyCounted(messageId: String): Boolean = _countedMessage.contains(messageId)

    override fun getMessageById(id: String): Message? = _messages.value[id]

    private companion object {
        private const val OFFSET_EVENT_TIME = 5L
    }
}
