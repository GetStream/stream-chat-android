package io.getstream.chat.android.common.messagelist

import com.getstream.sdk.chat.utils.extensions.getCreatedAtOrThrow
import com.getstream.sdk.chat.utils.extensions.isModerationFailed
import com.getstream.sdk.chat.utils.extensions.shouldShowMessageFooter
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.enqueue
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.common.extensions.isError
import io.getstream.chat.android.common.extensions.isSystem
import io.getstream.chat.android.common.model.ClipboardHandler
import io.getstream.chat.android.common.model.DateSeparatorHandler
import io.getstream.chat.android.common.model.DateSeparatorItem
import io.getstream.chat.android.common.model.MessageFocusRemoved
import io.getstream.chat.android.common.model.MessageFocused
import io.getstream.chat.android.common.model.MessageItem
import io.getstream.chat.android.common.model.MessageListItem
import io.getstream.chat.android.common.model.MessageListState
import io.getstream.chat.android.common.model.MessagePosition
import io.getstream.chat.android.common.model.MessagePositionHandler
import io.getstream.chat.android.common.model.MyOwn
import io.getstream.chat.android.common.model.NewMessageState
import io.getstream.chat.android.common.model.Other
import io.getstream.chat.android.common.model.SelectedMessageFailedModerationState
import io.getstream.chat.android.common.model.SelectedMessageOptionsState
import io.getstream.chat.android.common.model.SelectedMessageReactionsPickerState
import io.getstream.chat.android.common.model.SelectedMessageReactionsState
import io.getstream.chat.android.common.model.SelectedMessageState
import io.getstream.chat.android.common.model.SystemMessageItem
import io.getstream.chat.android.common.model.ThreadSeparatorItem
import io.getstream.chat.android.common.state.Copy
import io.getstream.chat.android.common.state.Delete
import io.getstream.chat.android.common.state.DeletedMessageVisibility
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.MessageFooterVisibility
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.MuteUser
import io.getstream.chat.android.common.state.Pin
import io.getstream.chat.android.common.state.React
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.common.state.Resend
import io.getstream.chat.android.common.state.ThreadReply
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.offline.extensions.cancelEphemeralMessage
import io.getstream.chat.android.offline.extensions.getRepliesAsState
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.offline.extensions.loadMessageById
import io.getstream.chat.android.offline.extensions.loadNewerMessages
import io.getstream.chat.android.offline.extensions.loadNewestMessages
import io.getstream.chat.android.offline.extensions.loadOlderMessages
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.MessagesState
import io.getstream.chat.android.offline.plugin.state.channel.thread.ThreadState
import io.getstream.logging.StreamLog
import io.getstream.logging.TaggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Controller responsible for handling message list state. It acts as a central place for core business logic and state
 * required to show the message list, message thread and handling message actions.
 *
 * @param cid The channel id in the format messaging:123.
 * @param chatClient The client used to communicate with the API.
 * @param deletedMessageVisibility The [DeletedMessageVisibility] to be applied to the list.
 * @param showSystemMessages Determines if the system messages should be shown or not.
 * @param showDateSeparators Determines whether the date separators are shown or not.
 * @param dateSeparatorThresholdMillis The time between two messages after which the date separator will be visible.
 * @param messageFooterVisibility Determines if and when the message footer is visible or not.
 * @param enforceUniqueReactions Determines whether the user can send only a single or multiple reactions to a message.
 */
public class MessageListController(
    private val cid: String,
    private val messageId: String? = null,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val deletedMessageVisibility: DeletedMessageVisibility,
    private val showSystemMessages: Boolean,
    private val showDateSeparators: Boolean,
    private val dateSeparatorThresholdMillis: Long,
    private val messageFooterVisibility: MessageFooterVisibility,
    private val enforceUniqueReactions: Boolean,
    private val clipboardHandler: ClipboardHandler,
) {

    /**
     * The logger used to print to errors, warnings, information
     * and other things to log.
     */
    private val logger: TaggedLogger = StreamLog.getLogger("MessageListController")

    /**
     * Creates a [CoroutineScope] that allows us to cancel the ongoing work when the parent
     * ViewModel is disposed.
     *
     * We use the [DispatcherProvider.Immediate] variant here to make sure the UI updates don't go through to process of
     * dispatching events.
     */
    private val scope = CoroutineScope(DispatcherProvider.Immediate)

    /**
     * Holds information about the current channel and is actively updated.
     */
    public val channelState: StateFlow<ChannelState?> =
        chatClient.watchChannelAsState(
            cid = cid,
            messageLimit = DEFAULT_MESSAGES_LIMIT,
            coroutineScope = scope
        )

    /**
     * Gives us information about the online state of the device.
     */
    public val connectionState: StateFlow<ConnectionState> by chatClient.clientState::connectionState

    /**
     * Gives us information about the logged in user state.
     */
    public val user: StateFlow<User?>
        get() = chatClient.clientState.user

    /**
     * Gives us information about the online state of the device.
     */
    public val isOnline: Flow<Boolean>
        get() = chatClient.clientState.connectionState.map { it == ConnectionState.CONNECTED }

    /**
     * Holds information about the abilities the current user
     * is able to exercise in the given channel.
     *
     * e.g. send messages, delete messages, etc...
     * For a full list @see [io.getstream.chat.android.client.models.ChannelCapabilities].
     */
    public val ownCapabilities: StateFlow<Set<String>> = channelState.filterNotNull()
        .flatMapLatest { it.channelData }
        .map { it.ownCapabilities }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = setOf())

    /**
     * The information for the current [Channel].
     */
    public val channel: StateFlow<Channel> = channelState.filterNotNull()
        .map { it.toChannel() }
        .onEach { channel ->
            chatClient.notifications.dismissChannelNotifications(
                channelType = channel.type,
                channelId = channel.id
            )
        }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, Channel())

    /**
     * Holds the current [MessageMode] that's used for the messages list. [MessageMode.Normal] by default.
     */
    private val _mode: MutableStateFlow<MessageMode> = MutableStateFlow(MessageMode.Normal)
    public val mode: StateFlow<MessageMode> = _mode

    /**
     * Gives us information if we're currently in the [Thread] message mode.
     */
    public val isInThread: Boolean
        get() = _mode.value is MessageMode.MessageThread

    /**
     * The unread message count for the channel when the [_mode] is [MessageMode.Normal].
     */
    public val channelUnreadCount: StateFlow<Int> = channelState.filterNotNull()
        .flatMapLatest { it.channelUnreadCount }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = 0)

    /**
     * The unread message count of a thread when the [_mode] is [MessageMode.MessageThread].
     */
    public val threadUnreadCount: StateFlow<Int> = channelState.filterNotNull()
        .flatMapLatest { it.threadsUnreadCount }
        .combine(_mode) { threadCounts, mode ->
            if (mode is MessageMode.MessageThread) {
                threadCounts[mode.parentMessage.id] ?: 0
            } else {
                0
            }
        }.stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = 0)

    /**
     * Unread count for channel or thread depending on the state of [_mode].
     */
    public val unreadCount: StateFlow<Int> = _mode.flatMapLatest {
        if (it is MessageMode.Normal) {
            channelUnreadCount
        } else {
            threadUnreadCount
        }
    }.stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = 0)

    /**
     * The list of typing users.
     */
    public val typingUsers: StateFlow<List<User>> = channelState.filterNotNull()
        .flatMapLatest { it.typing }
        .map { it.users }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = emptyList())

    /**
     * Current state of the message list.
     */
    private val _messageListState: MutableStateFlow<MessageListState> = MutableStateFlow(MessageListState())
    public val messageListState: StateFlow<MessageListState> = _messageListState

    /**
     * Current state of the thread message list.
     */
    private val _threadListState: MutableStateFlow<MessageListState> = MutableStateFlow(MessageListState())
    public val threadListState: StateFlow<MessageListState> = _threadListState

    /**
     * Current state of the message list depending on the [MessageMode] the list is in.
     */
    public val listState: StateFlow<MessageListState> = _mode.flatMapLatest {
        if (it is MessageMode.MessageThread) {
            _threadListState
        } else {
            _messageListState
        }
    }.stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = MessageListState())

    /**
     * State of the list depending of the mode, thread or normal.
     */
    private val messagesState: MessageListState
        get() = if (isInThread) _threadListState.value else _messageListState.value

    /**
     * Represents the last loaded message in the list, for comparison when determining the [NewMessageState] for the
     * screen.
     */
    private var lastLoadedMessage: Message? = null

    /**
     * Represents the last loaded message in the thread, for comparison when determining the [NewMessageState] for the
     * screen.
     */
    private var lastLoadedThreadMessage: Message? = null

    /**
     * Set of currently active [MessageAction]s. Used to show things like edit, reply, delete and
     * similar actions.
     */
    private val _messageActions: MutableStateFlow<Set<MessageAction>> = MutableStateFlow(emptySet())
    public val messageActions: StateFlow<Set<MessageAction>> = _messageActions

    /**
     * [MessagePositionHandler] that determines the message position inside the group.
     */
    private var messagePositionHandler: MessagePositionHandler = MessagePositionHandler.defaultHandler()

    /**
     * Evaluates whether date separators should be added to the message list.
     */
    private var dateSeparatorHandler: DateSeparatorHandler? =
        DateSeparatorHandler { previousMessage: Message?, message: Message ->
            if (!showDateSeparators) {
                false
            } else if (previousMessage == null) {
                true
            } else {
                val timeDifference = message.getCreatedAtOrThrow().time - previousMessage.getCreatedAtOrThrow().time
                timeDifference > dateSeparatorThresholdMillis
            }
        }

    /**
     * Evaluates whether thread separators should be added to the message list.
     */
    private var threadDateSeparatorHandler: DateSeparatorHandler? =
        DateSeparatorHandler { previousMessage: Message?, message: Message ->
            if (previousMessage == null) {
                false
            } else {
                (message.getCreatedAtOrThrow().time - previousMessage.getCreatedAtOrThrow().time) > SEPARATOR_TIME_MILLIS
            }
        }

    /**
     * Regulates the message footer visibility.
     */
    private val _messageFooterVisibility: MutableStateFlow<MessageFooterVisibility> =
        MutableStateFlow(messageFooterVisibility)

    /**
     * Regulates the visibility of deleted messages.
     */
    public val _deletedMessageVisibility: MutableStateFlow<DeletedMessageVisibility> =
        MutableStateFlow(deletedMessageVisibility)

    /**
     * Represents the message we wish to scroll to.
     */
    private var scrollToMessage: Message? = null

    /**
     * TODO
     */
    public val isInsideSearch: StateFlow<Boolean> = channelState.filterNotNull()
        .flatMapLatest { it.insideSearch }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = false)

    /**
     * [Job] that's used to keep the thread data loading operations. We cancel it when the user goes
     * out of the thread state.
     */
    private var threadJob: Job? = null

    init {
        observeMessagesState()
        messageId?.takeUnless { it.isNullOrBlank() }?.let { messageId ->
            scope.launch {
                _messageListState
                    .onCompletion { scrollToMessage(messageId) }
                    .first { it.messages.isNotEmpty() }
            }
        }
    }

    /**
     * Start observing messages for a given channel, groups and filers them to be show on the ui.
     */
    private fun observeMessagesState() {
        channelState.filterNotNull().flatMapLatest { channelState ->
            combine(
                channelState.messagesState,
                channelState.reads,
                _messageFooterVisibility,
                _deletedMessageVisibility
            ) { state, reads, _, _ ->
                when (state) {
                    is MessagesState.Loading,
                    is MessagesState.NoQueryActive,
                    -> _messageListState.value.copy(isLoading = true)
                    MessagesState.OfflineNoResults -> _messageListState.value.copy(
                        isLoading = false
                    )
                    is MessagesState.Result -> _messageListState.value.copy(
                        isLoading = false,
                        isLoadingNewerMessages = false,
                        isLoadingOlderMessages = false,
                        messages = groupMessages(
                            filterMessagesToShow(state.messages),
                            isInThread = isInThread,
                            reads = reads
                        ),
                    )
                }
            }
        }.catch {
            it.cause?.printStackTrace()
            showEmptyState()
        }.onEach { newState ->
            val newLastMessage =
                (newState.messages.firstOrNull { it is MessageItem } as? MessageItem)?.message

            val hasNewMessage = lastLoadedMessage != null &&
                _messageListState.value.messages.isNotEmpty() &&
                newLastMessage?.id != lastLoadedMessage?.id

            _messageListState.value = if (hasNewMessage) {
                newState.copy(newMessageState = getNewMessageState(newLastMessage, lastLoadedMessage))
            } else {
                newState
            }

            _messageListState.value.messages
                .firstOrNull { it is MessageItem && it.message.id == scrollToMessage?.id }?.let {
                    focusMessage((it as MessageItem).message.id)
                }

            lastLoadedMessage = newLastMessage
        }.launchIn(scope)

        channelState.filterNotNull().flatMapLatest { it.endOfOlderMessages }.onEach {
            _messageListState.value = _messageListState.value.copy(endOfOldMessagesReached = it)
        }.launchIn(scope)

        channelState.filterNotNull().flatMapLatest { it.endOfNewerMessages }.onEach {
            _messageListState.value = _messageListState.value.copy(endOfNewMessagesReached = it)
        }.launchIn(scope)

        user.onEach {
            _messageListState.value = _messageListState.value.copy(currentUser = it)
        }.launchIn(scope)

        channelUnreadCount.onEach {
            _messageListState.value = _messageListState.value.copy(unreadCount = it)
        }.launchIn(scope)
    }

    /**
     * Observes the currently active thread. In process, this
     * creates a [threadJob] that we can cancel once we leave the thread.
     *
     * @param threadId The message id with the thread we want to observe.
     * @param messages State flow source of thread messages.
     * @param endOfOlderMessages State flow of flag which show if we reached the end of available messages.
     * @param reads State flow source of read states.
     */
    private fun observeThreadMessagesState(
        threadId: String,
        messages: StateFlow<List<Message>>,
        endOfOlderMessages: StateFlow<Boolean>,
        reads: StateFlow<List<ChannelUserRead>>,
    ) {
        threadJob = scope.launch {
            combine(
                messages,
                reads,
            ) { messages, reads ->
                _threadListState.value.copy(
                    isLoading = false,
                    messages = groupMessages(
                        messages = filterMessagesToShow(messages),
                        isInThread = true,
                        reads = reads,
                    ),
                    parentMessageId = threadId,
                    isLoadingNewerMessages = false,
                    isLoadingOlderMessages = false,
                )
            }.collect { newState ->
                val newLastMessage =
                    (newState.messages.firstOrNull { it is MessageItem } as? MessageItem)?.message
                _threadListState.value = newState.copy(
                    newMessageState = getNewMessageState(newLastMessage, lastLoadedThreadMessage)
                )
                lastLoadedThreadMessage = newLastMessage
            }

            user.onEach {
                _messageListState.value = _threadListState.value.copy(currentUser = it)
            }.launchIn(this)

            endOfOlderMessages.onEach {
                _threadListState.value = _threadListState.value.copy(endOfOldMessagesReached = it)
            }.launchIn(this)

            threadUnreadCount.onEach {
                _threadListState.value = _threadListState.value.copy(unreadCount = it)
            }.launchIn(this)
        }
    }

    /**
     * Takes in the available messages for a [Channel] and groups them based on the sender ID. We put the message in a
     * group, where the positions can be [MessagePosition.TOP], [MessagePosition.MIDDLE],
     * [MessagePosition.BOTTOM] or [MessagePosition.NONE] if the message isn't in a group.
     *
     * @param messages The messages we need to group.
     * @param isInThread If we are in inside a thread.
     * @param reads The list of read states.
     *
     * @return A list of [MessageListItem]s, each containing a position.
     */
    private fun groupMessages(
        messages: List<Message>,
        isInThread: Boolean,
        reads: List<ChannelUserRead>,
    ): List<MessageListItem> {
        val parentMessageId = (_mode.value as? MessageMode.MessageThread)?.parentMessage?.id
        val currentUser = user.value
        val groupedMessages = mutableListOf<MessageListItem>()
        val lastRead = reads
            .filter { it.user.id != currentUser?.id }
            .mapNotNull { it.lastRead }
            .maxOrNull()

        messages.forEachIndexed { index, message ->
            val user = message.user
            val previousMessage = messages.getOrNull(index - 1)
            val nextMessage = messages.getOrNull(index + 1)

            val shouldAddDateSeparator = if (isInThread) {
                threadDateSeparatorHandler
            } else {
                dateSeparatorHandler
            }?.shouldAddDateSeparator(previousMessage, message) ?: false

            val position = messagePositionHandler.handleMessagePosition(previousMessage,
                message,
                nextMessage,
                shouldAddDateSeparator
            )

            val isLastMessageInGroup =
                position.contains(MessagePosition.BOTTOM) || position.contains(MessagePosition.NONE)

            val shouldShowFooter = _messageFooterVisibility.value.shouldShowMessageFooter(
                message = message,
                isLastMessageInGroup = isLastMessageInGroup,
                nextMessage = nextMessage
            )

            if (shouldAddDateSeparator) {
                groupedMessages.add(DateSeparatorItem(message.getCreatedAtOrThrow()))
            }

            if (message.isSystem() || message.isError()) {
                groupedMessages.add(SystemMessageItem(message = message))
            } else {
                val isMessageRead = message.createdAt
                    ?.let { lastRead != null && it <= lastRead }
                    ?: false

                groupedMessages.add(
                    MessageItem(
                        message = message,
                        currentUser = currentUser,
                        groupPosition = position,
                        parentMessageId = parentMessageId,
                        isMine = user.id == currentUser?.id,
                        isInThread = isInThread,
                        isMessageRead = isMessageRead,
                        deletedMessageVisibility = _deletedMessageVisibility.value,
                        showMessageFooter = shouldShowFooter
                    )
                )
            }

            if (index == 0 && isInThread) {
                groupedMessages.add(
                    ThreadSeparatorItem(
                        date = message.createdAt ?: message.createdLocallyAt ?: Date(),
                        messageCount = message.replyCount
                    )
                )
            }
        }

        return groupedMessages
    }

    /**
     * Used to filter messages which we should show to the current user.
     *
     * @param messages List of all messages.
     *
     * @return Filtered messages.
     */
    private fun filterMessagesToShow(messages: List<Message>): List<Message> {
        val currentUser = user.value

        return messages.filter {
            val shouldShowIfDeleted = when (_deletedMessageVisibility.value) {
                DeletedMessageVisibility.ALWAYS_VISIBLE -> true
                DeletedMessageVisibility.VISIBLE_FOR_CURRENT_USER -> {
                    !(it.deletedAt != null && it.user.id != currentUser?.id)
                }
                else -> it.deletedAt == null
            }
            val isSystemMessage = it.isSystem() || it.isError()

            shouldShowIfDeleted || (isSystemMessage && showSystemMessages)
        }
    }

    /**
     * Builds the [NewMessageState] for the UI, whenever the message state changes. This is used to
     * allow us to show the user a floating button giving them the option to scroll to the bottom
     * when needed.
     *
     * @param lastMessage Last message in the list, used for comparison.
     * @param lastLoadedMessage The last currently loaded message, used for comparison.
     */
    private fun getNewMessageState(lastMessage: Message?, lastLoadedMessage: Message?): NewMessageState? {
        val currentUser = user.value

        return if (lastMessage != null && lastLoadedMessage != null && lastMessage.id != lastLoadedMessage.id) {
            if (lastMessage.user.id == currentUser?.id) {
                MyOwn
            } else {
                Other
            }
        } else {
            null
        }
    }

    /**
     * When the user clicks the scroll to bottom button we need to take the user to the bottom of the newest
     * messages. If the messages are not loaded we need to load them first and then scroll to the bottom of the
     * list.
     *
     * @param messageLimit The size of the message list page to load.
     * @param scrollToBottom Handler that notifies when the message has been loaded.
     */
    public fun scrollToBottom(messageLimit: Int = DEFAULT_MESSAGES_LIMIT, scrollToBottom: () -> Unit) {
        if (_mode.value is MessageMode.MessageThread) {
            scrollToBottom()
        } else {
            if (channelState.value?.endOfNewerMessages?.value == true) {
                scrollToBottom()
            } else {
                chatClient.loadNewestMessages(cid, messageLimit).enqueue { result ->
                    if (result.isSuccess) {
                        scrollToBottom()
                    } else {
                        val error = result.error()
                        logger.e { "Could not load newest messages. Cause: ${error.cause?.message}" }
                    }
                }
            }
        }
    }

    /**
     * Loads newer messages of a channel following the currently newest loaded message. In case of threads this will
     * do nothing.
     *
     * @param baseMessageId The id of the most new [Message] inside the messages list.
     * @param messageLimit The size of the message list page to load.
     *
     * @return Whether the data is being loaded or not.
     */
    public fun loadNewerMessages(baseMessageId: String, messageLimit: Int = DEFAULT_MESSAGES_LIMIT) {
        if (_mode.value !is MessageMode.Normal ||
            chatClient.clientState.isOffline ||
            channelState.value?.endOfNewerMessages?.value == true
        ) return

        _messageListState.value = _messageListState.value.copy(isLoadingNewerMessages = true)

        chatClient.loadNewerMessages(cid, baseMessageId, messageLimit).enqueue()
    }

    /**
     * Loads more messages if we have reached the oldest message currently loaded.
     *
     * @param messageLimit The size of the message list page to load.
     */
    public fun loadOlderMessages(messageLimit: Int = DEFAULT_MESSAGES_LIMIT) {
        if (chatClient.clientState.isOffline) return

        _mode.value.run {
            when (this) {
                is MessageMode.Normal -> {
                    if (channelState.value?.endOfOlderMessages?.value == true) return
                    _messageListState.value = _messageListState.value.copy(isLoadingOlderMessages = true)
                    chatClient.loadOlderMessages(cid, messageLimit).enqueue()
                }
                is MessageMode.MessageThread -> threadLoadMore(this)
            }
        }
    }

    /**
     * Load older messages for the specified thread [MessageMode.MessageThread.parentMessage].
     *
     * @param threadMode Current thread mode.
     */
    private fun threadLoadMore(threadMode: MessageMode.MessageThread) {
        if (threadMode.threadState != null) {
            chatClient.getRepliesMore(
                messageId = threadMode.parentMessage.id,
                firstId = threadMode.threadState.oldestInThread.value?.id ?: threadMode.parentMessage.id,
                limit = DEFAULT_MESSAGES_LIMIT,
            ).enqueue()
        } else {
            logger.w { "Thread state must be not null for offline plugin thread load more!" }
        }
    }

    /**
     *  Changes the current [_mode] to be [MessageMode.MessageThread] with and uses [ChatClient] to get the
     *  [ThreadState] for the current thread.
     *
     * @param parentMessage The message with the thread we want to observe.
     */
    public fun enterThreadMode(parentMessage: Message) {
        val channelState = channelState.value ?: return
        val state = chatClient.getRepliesAsState(parentMessage.id, DEFAULT_MESSAGES_LIMIT)
        _mode.value = MessageMode.MessageThread(parentMessage, state)
        observeThreadMessagesState(
            threadId = state.parentId,
            messages = state.messages,
            endOfOlderMessages = state.endOfOlderMessages,
            reads = channelState.reads
        )
    }

    /**
     * Leaves the thread we're in.
     */
    public fun enterNormalMode() {
        _mode.value = MessageMode.Normal
        _threadListState.value = MessageListState()
        lastLoadedThreadMessage = null
        threadJob?.cancel()
    }

    /**
     * Loads a given [Message] with a single page around it.
     *
     * @param messageId The id of the [Message] we wish to load.
     * @param onResult Handler that notifies the result of the load action.
     */
    public fun loadMessageById(messageId: String, onResult: (Result<Message>) -> Unit = {}) {
        chatClient.loadMessageById(cid, messageId).enqueue { result -> onResult(result) }
    }

    /**
     * Scrolls to selected message. If the message is not currently in the list it will first load a page with the
     * message in the middle of it, add it to the list and then notify to scroll to the message.
     *
     * @param messageId The id of the [Message] we wish to scroll to.
     */
    public fun scrollToMessage(messageId: String) {
        if (isInThread) return
        val message = getMessageWithId(messageId)

        if (message != null) {
            scrollToMessage = message
            focusMessage(messageId)
        } else {
            loadMessageById(messageId) {
                scrollToMessage = it.data()
                focusMessage(messageId)
            }
        }
    }

    /**
     * Sets the focused message to be the message with the given ID, after which it removes it from
     * focus with a delay.
     *
     * @param messageId The ID of the message.
     */
    public fun focusMessage(messageId: String) {
        val messages = messagesState.messages.map {
            if (it is MessageItem && it.message.id == messageId) {
                it.copy(focusState = MessageFocused)
            } else {
                it
            }
        }

        scope.launch {
            updateMessages(messages)
            delay(REMOVE_MESSAGE_FOCUS_DELAY)
            removeMessageFocus(messageId)
        }
    }

    /**
     * Removes the focus from the message with the given ID.
     *
     * @param messageId The ID of the message.
     */
    private fun removeMessageFocus(messageId: String) {
        val messages = messagesState.messages.map {
            if (it is MessageItem && it.message.id == messageId) {
                it.copy(focusState = MessageFocusRemoved)
            } else {
                it
            }
        }

        if (scrollToMessage?.id == messageId) {
            scrollToMessage = null
        }

        updateMessages(messages)
    }

    /**
     * Updates the current message state with new messages.
     *
     * @param messages The list of new message items.
     * */
    private fun updateMessages(messages: List<MessageListItem>) {
        if (isInThread) {
            this._threadListState.value = _threadListState.value.copy(messages = messages)
        } else {
            this._messageListState.value = _messageListState.value.copy(messages = messages)
        }
    }

    /**
     * Triggered when the user long taps on and selects a message.
     *
     * @param message The selected message.
     */
    public fun selectMessage(message: Message?) {
        if (message != null) {
            changeSelectMessageState(
                if (message.isModerationFailed(chatClient)) {
                    SelectedMessageFailedModerationState(
                        message = message,
                        ownCapabilities = ownCapabilities.value
                    )
                } else {
                    SelectedMessageOptionsState(
                        message = message,
                        ownCapabilities = ownCapabilities.value
                    )
                }

            )
        }
    }

    /**
     * Triggered when the user taps on and selects message reactions.
     *
     * @param message The message that contains the reactions.
     */
    public fun selectReactions(message: Message?) {
        if (message != null) {
            changeSelectMessageState(
                SelectedMessageReactionsState(
                    message = message,
                    ownCapabilities = ownCapabilities.value
                )
            )
        }
    }

    /**
     * Triggered when the user taps the show more reactions button.
     *
     * @param message The selected message.
     */
    public fun selectExtendedReactions(message: Message?) {
        if (message != null) {
            changeSelectMessageState(
                SelectedMessageReactionsPickerState(
                    message = message,
                    ownCapabilities = ownCapabilities.value
                )
            )
        }
    }

    /**
     * Changes the state of [_threadListState] or [_messageListState] depending
     * on the thread mode.
     *
     * @param selectedMessageState The selected message state.
     */
    private fun changeSelectMessageState(selectedMessageState: SelectedMessageState) {
        if (isInThread) {
            _threadListState.value = _threadListState.value.copy(selectedMessageState = selectedMessageState)
        } else {
            _messageListState.value = _messageListState.value.copy(selectedMessageState = selectedMessageState)
        }
    }

    /**
     * Triggered when the user selects a new message action, in the message overlay.
     *
     * We first remove the overlay, after which we consume the event and based on the type of the event,
     * we do different things, such as starting a thread & loading thread data, showing delete or flag
     * events and dialogs, copying the message, muting users and more.
     *
     * @param messageAction The action the user chose.
     */
    public fun performMessageAction(messageAction: MessageAction) {
        removeOverlay()

        when (messageAction) {
            is Resend -> resendMessage(messageAction.message)
            is ThreadReply -> {
                _messageActions.value = _messageActions.value + Reply(messageAction.message)
                enterThreadMode(messageAction.message)
            }
            is Delete, is io.getstream.chat.android.common.state.Flag -> {
                _messageActions.value = _messageActions.value + messageAction
            }
            is Copy -> copyMessage(messageAction.message)
            is MuteUser -> updateUserMute(messageAction.message.user)
            is React -> reactToMessage(messageAction.reaction, messageAction.message, enforceUniqueReactions)
            is Pin -> updateMessagePin(messageAction.message)
            else -> {
                // no op, custom user action
            }
        }
    }

    /**
     * Used to dismiss a specific message action, such as delete, reply, edit or something similar.
     *
     * @param messageAction The action to dismiss.
     */
    public fun dismissMessageAction(messageAction: MessageAction) {
        _messageActions.value = _messageActions.value - messageAction
    }

    /**
     * Dismisses all message actions, when we cancel them in the rest of the UI.
     */
    public fun dismissAllMessageActions() {
        _messageActions.value = emptySet()
    }

    /**
     * Copies the message content using the [ClipboardHandler] we provide. This can copy both
     * attachment and text messages.
     *
     * @param message Message with the content to copy.
     */
    private fun copyMessage(message: Message) {
        clipboardHandler.copyMessage(message)
    }

    /**
     * Resets the [MessagesState]s, to remove the message overlay, by setting 'selectedMessage' to null.
     */
    public fun removeOverlay() {
        _threadListState.value = _threadListState.value.copy(selectedMessageState = null)
        _messageListState.value = _messageListState.value.copy(selectedMessageState = null)
    }

    /**
     * Deletes the given [message].
     *
     * @param message Message to delete.
     * @param hard Whether we do a hard delete or not.
     */
    public fun deleteMessage(message: Message, hard: Boolean = false) {
        _messageActions.value = _messageActions.value - _messageActions.value.filterIsInstance<Delete>()
        removeOverlay()

        chatClient.deleteMessage(message.id, hard)
            .enqueue(
                onError = { chatError ->
                    logger.e {
                        "Could not delete message: ${chatError.message}, Hard: ${hard}. " +
                            "Cause: ${chatError.cause?.message}. If you're using OfflinePlugin, the message " +
                            "should be deleted in the database and it will be deleted in the backend when " +
                            "the SDK sync its information."
                    }
                }
            )
    }

    public fun updateLastSeenMessage(message: Message) {
        val latestMessage: MessageItem? = listState.value.messages.firstOrNull { messageItem ->
            messageItem is MessageItem
        } as? MessageItem

        if (message.id == latestMessage?.message?.id) {
            markLastMessageRead()
        }
    }

    /**
     * Marks that the last message in the list as read.
     */
    public fun markLastMessageRead() {
        cid.cidToTypeAndId().let { (channelType, channelId) ->
            val mode = _mode.value
            if (mode is MessageMode.MessageThread) {
                chatClient.markThreadRead(channelType, channelId, mode.parentMessage.id)
            } else {
                chatClient.markRead(channelType, channelId).enqueue(
                    onError = { chatError ->
                        logger.e {
                            "Could not mark cid: $channelId as read. Error message: ${chatError.message}. " +
                                "Cause message: ${chatError.cause?.message}"
                        }
                    }
                )
            }
        }
    }

    /**
     * Flags the selected message.
     *
     * @param message Message to delete.
     * @param onResult Handler that notifies the flag message result.
     *
     * TODO
     */
    public fun flagMessage(message: Message, onResult: (Result<Flag>) -> Unit = {}) {
        _messageActions.value =
            _messageActions.value - _messageActions.value.filterIsInstance<io.getstream.chat.android.common.state.Flag>()
        chatClient.flagMessage(message.id).enqueue { result ->
            onActionResult(result, "Unable to flag message: ${result.error().message}", onResult)
        }
    }

    /**
     * Pins or unpins the message from the current channel based on its state.
     *
     * @param message The message to update the pin state of.
     * @param onResult Handler that propagates the result of the pin or unpin action.
     */
    public fun updateMessagePin(message: Message, onResult: (Result<Message>) -> Unit = {}) {
        if (message.pinned) {
            unpinMessage(message, onResult)
        } else {
            pinMessage(message, onResult)
        }
    }

    /**
     * Pins the message from the current channel.
     *
     * @param message The message to pin.
     * @param onResult Handler that propagates the result of the pin action.
     */
    public fun pinMessage(message: Message, onResult: (Result<Message>) -> Unit = {}) {
        chatClient.pinMessage(message).enqueue { result ->
            onActionResult(
                result = result,
                defaultError = "Could not pin the message: ${result.error().message}",
                onResult = onResult
            )
        }
    }

    /**
     * Unpins the message from the current channel.
     *
     * @param message The message to unpin.
     * @param onResult Handler that propagates the result of the unpin action.
     */
    public fun unpinMessage(message: Message, onResult: (Result<Message>) -> Unit = {}) {
        chatClient.unpinMessage(message).enqueue { result ->
            onActionResult(
                result = result,
                defaultError = "Could not unpin message: ${result.error().message}",
                onResult = onResult
            )
        }
    }

    /**
     * Resends a failed message.
     *
     * @param message The [Message] to be resent.
     */
    public fun resendMessage(message: Message) {
        val (channelType, channelId) = message.cid.cidToTypeAndId()
        chatClient.sendMessage(channelType, channelId, message)
            .enqueue(onError = { chatError ->
                logger.e {
                    "(Retry) Could not send message: ${chatError.message}. " +
                        "Cause: ${chatError.cause?.message}"
                }
            })
    }

    /**
     * Mutes or unmutes a user for the current user based on the users mute state.
     *
     * @param user The [User] for which to toggle the mute state.
     */
    public fun updateUserMute(user: User) {
        val isUserMuted = chatClient.globalState.muted.value.any { it.target.id == user.id }

        if (isUserMuted) {
            unmuteUser(user)
        } else {
            muteUser(user)
        }
    }

    /**
     * Mutes the given user.
     *
     * @param user The [User] we wish to mute.
     * @param onResult Handler that notifies the result of the mute action.
     */
    public fun muteUser(user: User, onResult: (Result<Mute>) -> Unit = {}) {
        chatClient.muteUser(user.id).enqueue { result ->
            onActionResult(
                result = result,
                defaultError = "Could not mute the user: ${result.error().message}",
                onResult = onResult
            )
        }
    }

    /**
     * Unmutes the given user.
     *
     * @param user The [User] we wish to unmute.
     * @param onResult Handler that notifies the result of the mute action.
     */
    public fun unmuteUser(user: User, onResult: (Result<Unit>) -> Unit = {}) {
        chatClient.unmuteUser(user.id).enqueue { result ->
            onActionResult(
                result = result,
                defaultError = "Could not unmute the user: ${result.error().message}",
                onResult = onResult
            )
        }
    }

    /**
     * Triggered when the user selets a reaction for the currently selected message. If the
     * message already has that reaction, from the current user, we remove it. Otherwise we add a new
     * reaction.
     *
     * @param reaction The reaction to add or remove.
     * @param message The currently selected message.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     */
    public fun reactToMessage(reaction: Reaction, message: Message, enforceUnique: Boolean) {
        if (message.ownReactions.any { it.type == reaction.type }) {
            chatClient.deleteReaction(
                messageId = message.id,
                reactionType = reaction.type,
                cid = cid
            ).enqueue(
                onError = { chatError ->
                    logger.e {
                        "Could not delete reaction for message with id: ${reaction.messageId} " +
                            "Error: ${chatError.message}. Cause: ${chatError.cause?.message}"
                    }
                }
            )
        } else {
            chatClient.sendReaction(
                enforceUnique = enforceUnique,
                reaction = reaction,
                cid = cid
            ).enqueue(
                onError = { chatError ->
                    logger.e {
                        "Could not send reaction for message with id: ${reaction.messageId} " +
                            "Error: ${chatError.message}. Cause: ${chatError.cause?.message}"
                    }
                }
            )
        }
    }

    /**
     * Clears the messages list and shows a clear list state.
     */
    private fun showEmptyState() {
        _messageListState.value = _messageListState.value.copy(isLoading = false, messages = emptyList())
    }

    /**
     * Gets the message if it is inside the list.
     *
     * @param messageId The [Message] id we are looking for.
     *
     * @return [Message] with the request id or null if the message is not in the list.
     */
    public fun getMessageWithId(messageId: String): Message? {
        return (_messageListState.value.messages.firstOrNull { it is MessageItem && it.message.id == messageId } as? MessageItem)?.message
    }

    /**
     * Clears the new messages state and drops the unread count to 0 after the user scrolls to the newest message.
     */
    public fun clearNewMessageState() {
        if (!messagesState.endOfNewMessagesReached) return
        _threadListState.value = _threadListState.value.copy(newMessageState = null, unreadCount = 0)
        _messageListState.value = _messageListState.value.copy(newMessageState = null, unreadCount = 0)
    }

    /**
     * Mutes the given user inside this channel.
     *
     * @param userId The ID of the user to be muted.
     * @param timeout The period of time for which the user will be muted, expressed in minutes. A null value signifies
     * that the user will be muted for an indefinite time.
     */
    public fun muteUser(userId: String, timeout: Int? = null) {
        chatClient.muteUser(userId, timeout)
            .enqueue(onError = { chatError ->
                val errorMessage = chatError.message ?: chatError.cause?.message ?: "Unable to mute the user"
                logger.e { errorMessage }
            })
    }

    /**
     * Unmutes the given user inside this channel.
     *
     * @param userId The ID of the user to be unmuted.
     */
    public fun unmuteUser(userId: String) {
        chatClient.unmuteUser(userId)
            .enqueue(onError = { chatError ->
                val errorMessage = chatError.message ?: chatError.cause?.message ?: "Unable to unmute the user"
                logger.e { errorMessage }
            })
    }

    /**
     * Bans the given user inside this channel.
     *
     * @param userId The ID of the user to be banned.
     * @param reason The reason for banning the user.
     * @param timeout The period of time for which the user will be banned, expressed in minutes. A null value signifies
     * that the user will be banned for an indefinite time.
     * @param onResult Handler to notify when the action finishes.
     */
    public fun banUser(
        userId: String,
        reason: String? = null,
        timeout: Int? = null,
        onResult: (Result<Unit>) -> Unit = {},
    ) {
        chatClient.channel(cid).banUser(userId, reason, timeout).enqueue { result ->
            onActionResult(
                result = result,
                defaultError = "Unable to ban the user: ${result.error().message}",
                onResult = onResult
            )
        }
    }

    /**
     * Unbans the given user inside this channel.
     *
     * @param userId The ID of the user to be unbanned.
     * @param onResult Handler to notify when the action finishes.
     */
    public fun unbanUser(userId: String, onResult: (Result<Unit>) -> Unit = {}) {
        chatClient.channel(cid).unbanUser(userId).enqueue { result ->
            onActionResult(
                result = result,
                defaultError = "Unable to unban the user: ${result.error().message}",
                onResult = onResult
            )
        }
    }

    /**
     * Shadow bans the given user inside this channel.
     *
     * @param userId The ID of the user to be shadow banned.
     * @param reason The reason for shadow banning the user.
     * @param timeout The period of time for which the user will be shadow banned, expressed in minutes. A null value
     * signifies that the user will be shadow banned for an indefinite time.
     * @param onResult Handler to notify when the action finishes.
     */
    public fun shadowBanUser(
        userId: String,
        reason: String? = null,
        timeout: Int? = null,
        onResult: (Result<Unit>) -> Unit = {},
    ) {
        chatClient.channel(cid).shadowBanUser(userId, reason, timeout).enqueue { result ->
            onActionResult(
                result = result,
                defaultError = "Unable to shadow ban the user: ${result.error().message}",
                onResult = onResult
            )
        }
    }

    /**
     * Removes the shaddow ban for the given user inside
     * this channel.
     *
     * @param userId The ID of the user for which the shadow ban is removed.
     * @param onResult Handler to notify when the action finishes.
     */
    public fun removeShadowBanFromUser(userId: String, onResult: (Result<Unit>) -> Unit = {}) {
        chatClient.channel(cid).removeShadowBan(userId).enqueue { result ->
            onActionResult(
                result = result,
                defaultError = "Unable to remove the shadow ban for user: ${result.error().message}",
                onResult = onResult
            )
        }
    }

    /**
     * Executes one of the actions for the given ephemeral giphy message.
     *
     * @param action The action to be executed.
     */
    public fun performGiphyAction(action: GiphyAction) {
        val message = action.message
        when (action) {
            is SendGiphy -> chatClient.sendGiphy(message)
            is ShuffleGiphy -> chatClient.shuffleGiphy(message)
            is CancelGiphy -> chatClient.cancelEphemeralMessage(message)
        }.exhaustive.enqueue(onError = { chatError ->
            logger.e {
                "Could not ${action::class.java.simpleName} giphy for message id: ${message.id}. " +
                    "Error: ${chatError.message}. Cause: ${chatError.cause?.message}"
            }
        })
    }

    // TODO
    public fun removeAttachment(messageId: String, attachment: Attachment) {
        chatClient.loadMessageById(
            cid,
            messageId
        ).enqueue { result ->
            if (result.isSuccess) {
                val message = result.data()
                message.attachments.removeAll { attachment ->
                    if (attachment.assetUrl != null) {
                        attachment.assetUrl == attachment.assetUrl
                    } else {
                        attachment.imageUrl == attachment.imageUrl
                    }
                }

                chatClient.updateMessage(message).enqueue(
                    onError = { chatError ->
                        logger.e {
                            "Could not edit message to remove its attachments: ${chatError.message}. " +
                                "Cause: ${chatError.cause?.message}"
                        }
                    }
                )
            } else {
                logger.e { "Could not load message: ${result.error()}" }
            }
        }
    }

    /**
     * Sets the [MessagePositionHandler] that determines the message position inside a group.
     *
     * @param messagePositionHandler The [MessagePositionHandler] to be used when grouping the list.
     */
    public fun setMessagePositionHandler(messagePositionHandler: MessagePositionHandler) {
        this.messagePositionHandler = messagePositionHandler
    }

    /**
     * Sets the date separator handler which determines when to add date separators.
     * By default, a date separator will be added if the difference between two messages' dates is greater than 4h.
     *
     * @param dateSeparatorHandler The handler to use. If null, [_messageListState] won't contain date separators.
     */
    public fun setDateSeparatorHandler(dateSeparatorHandler: DateSeparatorHandler?) {
        this.dateSeparatorHandler = dateSeparatorHandler
    }

    /**
     * Sets thread date separator handler which determines when to add date separators inside the thread.
     * @see setDateSeparatorHandler
     *
     * @param threadDateSeparatorHandler The handler to use. If null, [_messageListState] won't contain date separators.
     */
    public fun setThreadDateSeparatorHandler(threadDateSeparatorHandler: DateSeparatorHandler?) {
        this.threadDateSeparatorHandler = threadDateSeparatorHandler
    }

    /**
     * Sets the value used to determine if message footer content is shown.
     * @see MessageFooterVisibility
     *
     * @param messageFooterVisibility Changes the visibility of message footers.
     */
    public fun setMessageFooterVisibility(messageFooterVisibility: MessageFooterVisibility) {
        _messageFooterVisibility.value = messageFooterVisibility
    }

    /**
     * Sets the value used to filter deleted messages.
     * @see DeletedMessageVisibility
     *
     * @param deletedMessageVisibility Changes the visibility of deleted messages.
     */
    public fun setDeletedMessageVisibility(deletedMessageVisibility: DeletedMessageVisibility) {
        _deletedMessageVisibility.value = deletedMessageVisibility
    }

    /**
     * Quality of life function that notifies the result of an action and logs and error in case the action has failed.
     *
     * @param result The [Result] of the action.
     * @param onResult Handler that notifies the result of the action.
     * @param defaultError The default error to be shown on the screen if we can't get the error from the result.
     */
    private fun <T : Any> onActionResult(result: Result<T>, defaultError: String, onResult: (Result<T>) -> Unit) {
        onResult(result)
        if (result.isError) {
            val errorMessage = result.error().message ?: result.error().cause?.message ?: defaultError
            logger.e { errorMessage }
        }
    }

    internal companion object {
        /**
         * The default limit of messages to load.
         */
        const val DEFAULT_MESSAGES_LIMIT = 30

        /**
         * The default threshold for showing date separators. If the message difference in millis is equal to this
         * number, then we show a separator, if it's enabled in the list.
         */
        const val SEPARATOR_TIME_MILLIS: Long = 1000 * 60 * 60 * 4

        const val REMOVE_MESSAGE_FOCUS_DELAY: Long = 2000
    }
}