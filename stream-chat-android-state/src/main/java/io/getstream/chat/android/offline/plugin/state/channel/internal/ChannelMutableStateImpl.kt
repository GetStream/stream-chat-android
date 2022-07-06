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

package io.getstream.chat.android.offline.plugin.state.channel.internal

import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.extensions.internal.updateUsers
import io.getstream.chat.android.client.extensions.internal.wasCreatedAfter
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.model.channel.ChannelData
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.MessagesState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Date

internal class ChannelMutableStateImpl(
    override val channelType: String,
    override val channelId: String,
    private val scope: CoroutineScope,
    private val userFlow: StateFlow<User?>,
    latestUsers: StateFlow<Map<String, User>>,
) : ChannelMutableState {

    override val cid: String = "%s:%s".format(channelType, channelId)

    private val _messages = MutableStateFlow<Map<String, Message>>(emptyMap())
    private val _typing = MutableStateFlow(TypingEvent(channelId, emptyList()))
    private val _typingChatEvents = MutableStateFlow<Map<String, TypingStartEvent>>(emptyMap())
    private val _rawReads = MutableStateFlow<Map<String, ChannelUserRead>>(emptyMap())
    private val _members = MutableStateFlow<Map<String, Member>>(emptyMap())
    private val _oldMessages = MutableStateFlow<Map<String, Message>>(emptyMap())
    private val _watchers = MutableStateFlow<Map<String, User>>(emptyMap())
    private val _watcherCount = MutableStateFlow(0)
    private val _read = MutableStateFlow<ChannelUserRead?>(null)
    private val _endOfNewerMessages = MutableStateFlow(false)
    private val _endOfOlderMessages = MutableStateFlow(false)
    private val _loading = MutableStateFlow(false)
    private val _hidden = MutableStateFlow(false)
    private val _muted = MutableStateFlow(false)
    private val _channelData = MutableStateFlow<ChannelData?>(null)
    private val _repliedMessage = MutableStateFlow<Message?>(null)
    private val _unreadCount = MutableStateFlow(0)
    private val _membersCount = MutableStateFlow(0)
    private val _insideSearch = MutableStateFlow(false)
    private val _loadingOlderMessages = MutableStateFlow(false)
    private val _loadingNewerMessages = MutableStateFlow(false)
    private val _lastMessageAt = MutableStateFlow<Date?>(null)

    /** raw version of messages. */
    override var rawMessages: Map<String, Message>
        get() = _messages.value
        set(value) { _messages.value = value }

    /** raw version of reads. */
    override var rawReads: Map<String, ChannelUserRead>
        get() = _rawReads.value
        set(value) { _rawReads.value = value }

    /** raw version of reads. */
    override var rawMembers: Map<String, Member>
        get() = _members.value
        set(value) { _members.value = value }

    /** raw version of old messages. */
    override var rawOldMessages: Map<String, Message>
        get() = _oldMessages.value
        set(value) { _oldMessages.value = value }

    /** raw version of old watchers. */
    override var rawWatchers: Map<String, User>
        get() = _watchers.value
        set(value) { _watchers.value = value }

    /** raw version of typing. */
    override var rawTyping: Map<String, TypingStartEvent>
        get() = _typingChatEvents.value
        set(value) { _typingChatEvents.value = value }

    /** the date of the last message */
    override var lastMessageAt: Date?
        get() = _lastMessageAt.value
        set(value) { _lastMessageAt.value = value }

    /** Channel config data. */
    private val _channelConfig: MutableStateFlow<Config> = MutableStateFlow(Config())

    /** the data to hide messages before */
    override var hideMessagesBefore: Date? = null

    /** The raw message list updated by recent users value. */
    override val messageList: StateFlow<List<Message>> =
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

    internal var lastMarkReadEvent: Date? = null
    internal var lastKeystrokeAt: Date? = null
    override var lastStartTypingEvent: Date? = null
    internal var keystrokeParentMessageId: String? = null

    /** Sorted version of messages. */
    override val sortedMessages: StateFlow<List<Message>> = messageList.map {
        it.sortedBy { message -> message.createdAt ?: message.createdLocallyAt }
            .filter { message -> hideMessagesBefore == null || message.wasCreatedAfter(hideMessagesBefore) }
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

    override val read: StateFlow<ChannelUserRead?> = _read

    override val unreadCount: StateFlow<Int?> = _unreadCount

    override val members: StateFlow<List<Member>> = _members
        .combine(latestUsers) { membersMap, usersMap -> membersMap.values.updateUsers(usersMap) }
        .map { it.sortedBy(Member::createdAt) }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val membersCount: StateFlow<Int> = _membersCount

    override val channelData: StateFlow<ChannelData> =
        _channelData.filterNotNull().combine(latestUsers) { channelData, users ->
            if (users.containsKey(channelData.createdBy.id)) {
                channelData.copy(createdBy = users[channelData.createdBy.id] ?: channelData.createdBy)
            } else {
                channelData
            }
        }
            .stateIn(scope, SharingStarted.Eagerly, ChannelData(type = channelType, channelId = channelId))

    override val hidden: StateFlow<Boolean> = _hidden
    override val muted: StateFlow<Boolean> = _muted
    override val loading: StateFlow<Boolean> = _loading
    override val loadingOlderMessages: StateFlow<Boolean> = _loadingOlderMessages
    override val loadingNewerMessages: StateFlow<Boolean> = _loadingNewerMessages
    override val endOfOlderMessages: StateFlow<Boolean> = _endOfOlderMessages
    override val endOfNewerMessages: StateFlow<Boolean> = _endOfNewerMessages
    override var recoveryNeeded: Boolean = false

    override val insideSearch: StateFlow<Boolean> = _insideSearch

    override fun toChannel(): Channel {
        // recreate a channel object from the various observables.
        val channelData = channelData.value

        val messages = sortedMessages.value
        val members = members.value
        val watchers = watchers.value
        val reads = _rawReads.value.values.toList()
        val watcherCount = _watcherCount.value

        val channel = channelData.toChannel(messages, members, reads, watchers, watcherCount)
        channel.config = _channelConfig.value
        channel.unreadCount = _unreadCount.value
        channel.lastMessageAt =
            lastMessageAt ?: messages.lastOrNull()?.let { it.createdAt ?: it.createdLocallyAt }
        channel.hidden = _hidden.value

        return channel
    }

    override fun setLoadingOlderMessages(isLoading: Boolean) {
        _loadingOlderMessages.value = isLoading
    }

    override fun setLoadingNewerMessages(isLoading: Boolean) {
        _loadingNewerMessages.value = isLoading
    }

    override fun setWatcherCount(count: Int) {
        _watcherCount.value = count
    }

    override fun setRead(channelUserRead: ChannelUserRead?) {
        _read.value = channelUserRead
    }

    override fun setEndOfNewerMessages(isEnd: Boolean) {
        _endOfNewerMessages.value = isEnd
    }

    override fun setEndOfOlderMessages(isEnd: Boolean) {
        _endOfOlderMessages.value = isEnd
    }

    override fun setLoading(isLoading: Boolean) {
        _loading.value = isLoading
    }

    override fun setHidden(isHidden: Boolean) {
        _hidden.value = isHidden
    }

    override fun setMuted(isMuted: Boolean) {
        _muted.value = isMuted
    }

    override fun setChannelData(channelData: ChannelData) {
        _channelData.value = channelData
    }

    override fun setRepliedMessage(repliedMessage: Message?) {
        _repliedMessage.value = repliedMessage
    }

    override fun setUnreadCount(count: Int) {
        _unreadCount.value = count
    }

    override fun setMembersCount(count: Int) {
        _membersCount.value = count
    }

    override fun setInsideSearch(isInsideSearch: Boolean) {
        _insideSearch.value = isInsideSearch
    }

    override fun setChannelConfig(channelConfig: Config) {
        _channelConfig.value = channelConfig
    }

    override fun updateTypingEvents(eventsMap: Map<String, TypingStartEvent>, typingEvent: TypingEvent) {
        _typingChatEvents.value = eventsMap
        _typing.value = typingEvent
    }
}

internal fun ChannelState.toMutableState(): ChannelMutableStateImpl = this as ChannelMutableStateImpl
