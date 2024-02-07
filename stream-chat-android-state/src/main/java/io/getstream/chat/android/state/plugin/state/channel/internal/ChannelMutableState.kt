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
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessagesState
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.utils.internal.combineStates
import io.getstream.chat.android.state.utils.internal.mapState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

@Suppress("TooManyFunctions")
/** State container with mutable data of a channel.*/
internal class ChannelMutableState(
    override val channelType: String,
    override val channelId: String,
    private val userFlow: StateFlow<User?>,
    latestUsers: StateFlow<Map<String, User>>,
) : ChannelState {

    override val cid: String = "%s:%s".format(channelType, channelId)

    private var _messages: MutableStateFlow<Map<String, Message>>? = MutableStateFlow(emptyMap())
    private var _countedMessage: MutableSet<String>? = mutableSetOf()
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
    private var _lastSentMessageDate: MutableStateFlow<Date?>? = MutableStateFlow(null)

    /** Channel config data. */
    private var _channelConfig: MutableStateFlow<Config>? = MutableStateFlow(Config())

    override val hidden: StateFlow<Boolean> = _hidden!!
    override val muted: StateFlow<Boolean> = _muted!!
    override val loading: StateFlow<Boolean> = _loading!!
    override val loadingOlderMessages: StateFlow<Boolean> = _loadingOlderMessages!!
    override val loadingNewerMessages: StateFlow<Boolean> = _loadingNewerMessages!!
    override val endOfOlderMessages: StateFlow<Boolean> = _endOfOlderMessages!!

    override val endOfNewerMessages: StateFlow<Boolean> = _endOfNewerMessages!!

    /** the data to hide messages before */
    var hideMessagesBefore: Date? = null

    /**
     * Messages to show on the channels list when the state is inside search to show the latest message in the preview.
     */
    internal val cachedLatestMessages: MutableStateFlow<Map<String, Message>> = MutableStateFlow(emptyMap())

    /** The raw message list updated by recent users value. */
    val messageList: StateFlow<List<Message>> =
        combineStates(_messages!!, latestUsers) { messageMap, userMap -> messageMap.values.updateUsers(userMap) }

    /** a list of messages sorted by message.createdAt */
    private val sortedVisibleMessages: StateFlow<List<Message>> =
        messagesTransformation(messageList)

    override val messagesState: StateFlow<MessagesState> =
        combineStates(loading, sortedVisibleMessages) { loading: Boolean, messages: List<Message> ->
            when {
                loading -> MessagesState.Loading
                messages.isEmpty() -> MessagesState.OfflineNoResults
                else -> MessagesState.Result(messages)
            }
        }

    private fun messagesTransformation(messages: StateFlow<Collection<Message>>): StateFlow<List<Message>> {
        return combineStates(messages, userFlow) { messageCollection, user ->
            messageCollection.asSequence()
                .filter { it.parentId == null || it.showInChannel }
                .filter { it.user.id == user?.id || !it.shadowed }
                .filter { hideMessagesBefore == null || it.wasCreatedAfter(hideMessagesBefore) }
                .sortedBy { it.createdAt ?: it.createdLocallyAt }
                .toList()
        }
    }

    /** The date of the last typing event. */
    var lastStartTypingEvent: Date? = null
    internal var keystrokeParentMessageId: String? = null

    internal val visibleMessages: StateFlow<Map<String, Message>> = messageList.mapState { messages ->
        messages.filter { message -> hideMessagesBefore == null || message.wasCreatedAfter(hideMessagesBefore) }
            .associateBy(Message::id)
    }

    /** Sorted version of messages. */
    val sortedMessages: StateFlow<List<Message>> = visibleMessages.mapState { messagesMap ->
        messagesMap.values.sortedBy { message -> message.createdAt ?: message.createdLocallyAt }
    }

    override val repliedMessage: StateFlow<Message?> = _repliedMessage!!

    override val quotedMessagesMap: StateFlow<Map<String, List<String>>> = _quotedMessagesMap!!

    /** Channel config data */
    override val channelConfig: StateFlow<Config> = _channelConfig!!

    override val messages: StateFlow<List<Message>> = sortedVisibleMessages

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

    val lastMarkReadEvent: StateFlow<Date?> = read.mapState { it?.lastRead }

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

    override val lastSentMessageDate: StateFlow<Date?> = _lastSentMessageDate!!

    override fun toChannel(): Channel {
        // recreate a channel object from the various observables.
        val channelData = channelData.value

        val messages = sortedMessages.value
        val cachedMessages = cachedLatestMessages.value.values.toList()
        val members = members.value
        val watchers = watchers.value
        val reads = rawReads.value.values.toList()
        val watcherCount = watcherCount.value
        val insideSearch = insideSearch.value

        val channel = channelData
            .toChannel(messages, cachedMessages, members, reads, watchers, watcherCount, insideSearch)
        return channel.copy(
            config = channelConfig.value,
            hidden = hidden.value,
            isInsideSearch = insideSearch,
            cachedLatestMessages = cachedLatestMessages.value.values.toList(),
        ).syncUnreadCountWithReads()
    }

    /**
     * Sets the channel to be loading older messages.
     *
     * @param isLoading Boolean.
     */
    fun setLoadingOlderMessages(isLoading: Boolean) {
        _loadingOlderMessages?.value = isLoading
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
     * Sets the date of the last message sent by the current user.
     *
     * @param lastSentMessageDate The date of the last message.
     */
    fun setLastSentMessageDate(lastSentMessageDate: Date?) {
        _lastSentMessageDate?.value = lastSentMessageDate
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
        val membersMap = members.associateBy(Member::getUserId)
        _members?.apply { value = value + membersMap }
    }

    /**
     * Add a member
     *
     * @param member The member to be added.
     */
    fun addMember(member: Member) {
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
        _watchers?.let { upsertWatchers((it.value - user.id).values.toList(), watchersCount) }
    }

    fun deleteMessage(message: Message, updateCount: Boolean = true) {
        _messages?.apply { value = value - message.id }

        if (updateCount) {
            _countedMessage?.remove(message.id)
        }
    }

    fun upsertWatchers(watchers: List<User>, watchersCount: Int) {
        _watchers?.apply {
            value = value + watchers.associateBy(User::id)
            _watcherCount?.value = watchersCount.takeUnless { it < 0 } ?: value.size
        }
    }

    /**
     * Upsert message in the channel.
     *
     * @param message message to be upserted.
     */
    fun upsertMessage(message: Message, updateCount: Boolean = true) {
        _messages?.apply { value = value + (message.id to message) }

        if (updateCount) {
            _countedMessage?.add(message.id)
        }
    }

    fun upsertUserPresence(user: User) {
        _members?.value?.get(user.id)?.copy(user = user)?.let { upsertMembers(listOf(it)) }
        user.takeIf { _watchers?.value?.any { it.key == user.id } == true }
            ?.let { upsertWatchers(listOf(it), watcherCount.value) }
        _channelData?.value?.takeIf { it.createdBy.id == user.id }
            ?.let { setChannelData(it.copy(createdBy = user)) }
        _messages?.apply { value = value.values.updateUsers(mapOf(user.id to user)).associateBy { it.id } }
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
        _messages?.apply { value = value.filter { it.value.wasCreatedAfter(date) } }
    }

    fun upsertMessages(updatedMessages: Collection<Message>, updateCount: Boolean = true) {
        _messages?.apply { value += updatedMessages.associateBy(Message::id) }

        if (updateCount) {
            _countedMessage?.addAll(updatedMessages.map { it.id })
        }
    }

    fun setMessages(messages: List<Message>) {
        _messages?.value = messages.associateBy(Message::id)
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
        _countedMessage?.clear()
    }

    fun insertCountedMessages(ids: List<String>) {
        _countedMessage?.addAll(ids)
    }

    override fun getMessageById(id: String): Message? = _messages?.value?.get(id)

    internal fun destroy() {
        _messages = null
        _countedMessage = null
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
        _lastSentMessageDate = null
        _channelConfig = null
    }

    private companion object {
        private const val OFFSET_EVENT_TIME = 5L
    }
}
