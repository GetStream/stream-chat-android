package io.getstream.chat.android.offline.experimental.channel.state

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.channel.ChannelData
import io.getstream.chat.android.offline.message.wasCreatedAfter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Date

@ExperimentalStreamChatApi
internal class ChannelMutableState(
    override val channelType: String,
    override val channelId: String,
    private val scope: CoroutineScope,
    private val userFlow: StateFlow<User?>,
) : ChannelState {

    override val cid: String = "%s:%s".format(channelType, channelId)

    internal val _messages = MutableStateFlow<Map<String, Message>>(emptyMap())
    internal val _watcherCount = MutableStateFlow(0)
    internal val _typing = MutableStateFlow<Map<String, ChatEvent>>(emptyMap())
    internal val _reads = MutableStateFlow<Map<String, ChannelUserRead>>(emptyMap())
    internal val _read = MutableStateFlow<ChannelUserRead?>(null)
    internal val _endOfNewerMessages = MutableStateFlow(false)
    internal val _endOfOlderMessages = MutableStateFlow(false)
    internal val _loading = MutableStateFlow(false)
    internal val _hidden = MutableStateFlow(false)
    internal val _muted = MutableStateFlow(false)
    internal val _watchers = MutableStateFlow<Map<String, User>>(emptyMap())
    internal val _members = MutableStateFlow<Map<String, Member>>(emptyMap())
    internal val _loadingOlderMessages = MutableStateFlow(false)
    internal val _loadingNewerMessages = MutableStateFlow(false)
    internal val _channelData = MutableStateFlow<ChannelData?>(null)
    internal val _oldMessages = MutableStateFlow<Map<String, Message>>(emptyMap())
    internal val lastMessageAt = MutableStateFlow<Date?>(null)
    internal val _repliedMessage = MutableStateFlow<Message?>(null)
    internal val _unreadCount = MutableStateFlow(0)

    internal var hideMessagesBefore: Date? = null
    internal val unfilteredMessages: StateFlow<List<Message>> =
        _messages.map { it.values.toList() }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** a list of messages sorted by message.createdAt */
    private val sortedVisibleMessages: StateFlow<List<Message>> =
        messagesTransformation(_messages).stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _messagesState: StateFlow<MessagesState> =
        _loading.combine(sortedVisibleMessages) { loading: Boolean, messages: List<Message> ->
            when {
                loading -> MessagesState.Loading
                messages.isEmpty() -> MessagesState.OfflineNoResults
                else -> MessagesState.Result(messages)
            }
        }.stateIn(scope, SharingStarted.Eagerly, MessagesState.NoQueryActive)

    private fun messagesTransformation(messages: MutableStateFlow<Map<String, Message>>): StateFlow<List<Message>> {
        return messages.map { messageMap ->
            messageMap.values
                .asSequence()
                .filter { it.parentId == null || it.showInChannel }
                .filter { it.user.id == userFlow.value?.id || !it.shadowed }
                .filter { hideMessagesBefore == null || it.wasCreatedAfter(hideMessagesBefore) }
                .sortedBy { it.createdAt ?: it.createdLocallyAt }
                .toList()
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())
    }

    override val repliedMessage: StateFlow<Message?> = _repliedMessage
    override val messages: StateFlow<List<Message>> = sortedVisibleMessages
    override val messagesState: StateFlow<MessagesState> = _messagesState
    override val oldMessages: StateFlow<List<Message>> = messagesTransformation(_oldMessages)
    override val watcherCount: StateFlow<Int> = _watcherCount
    override val watchers: StateFlow<List<User>> =
        _watchers.map { it.values.sortedBy(User::createdAt) }.stateIn(scope, SharingStarted.Eagerly, emptyList())
    override val typing: StateFlow<TypingEvent> = userFlow
        .filterNotNull()
        .flatMapConcat { currentUser ->
            _typing.map { typingMap ->
                currentUser to typingMap
            }
        }
        .map { (currentUser, typingMap) ->
            val userList = typingMap.values
                .sortedBy(ChatEvent::createdAt)
                .mapNotNull { event ->
                    when (event) {
                        is TypingStartEvent -> event.user.takeIf { user -> user != currentUser }
                        else -> null
                    }
                }

            TypingEvent(channelId, userList)
        }
        .stateIn(scope, SharingStarted.Eagerly, TypingEvent(channelId, emptyList()))

    override val reads: StateFlow<List<ChannelUserRead>> = _reads
        .map { it.values.sortedBy(ChannelUserRead::lastRead) }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val read: StateFlow<ChannelUserRead?> = _read

    override val unreadCount: StateFlow<Int?> = _unreadCount

    override val members: StateFlow<List<Member>> = _members
        .map { it.values.sortedBy(Member::createdAt) }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val channelData: StateFlow<ChannelData> = _channelData.filterNotNull()
        .stateIn(scope, SharingStarted.Eagerly, ChannelData(type = channelType, channelId = channelId))

    override val hidden: StateFlow<Boolean> = _hidden
    override val muted: StateFlow<Boolean> = _muted
    override val loading: StateFlow<Boolean> = _loading
    override val loadingOlderMessages: StateFlow<Boolean> = _loadingOlderMessages
    override val loadingNewerMessages: StateFlow<Boolean> = _loadingNewerMessages
    override val endOfOlderMessages: StateFlow<Boolean> = _endOfOlderMessages
    override val endOfNewerMessages: StateFlow<Boolean> = _endOfNewerMessages
    override var recoveryNeeded: Boolean = false
}

@ExperimentalStreamChatApi
internal fun ChannelState.toMutableState(): ChannelMutableState = this as ChannelMutableState
