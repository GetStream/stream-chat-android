package io.getstream.chat.android.common.messagelist

import com.getstream.sdk.chat.utils.extensions.getCreatedAtOrThrow
import com.getstream.sdk.chat.utils.extensions.shouldShowMessageFooter
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.enqueue
import io.getstream.chat.android.client.extensions.cidToTypeAndId
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
import io.getstream.chat.android.common.model.DateSeparatorHandler
import io.getstream.chat.android.common.model.DateSeparatorItem
import io.getstream.chat.android.common.model.MessageItem
import io.getstream.chat.android.common.model.MessageListItem
import io.getstream.chat.android.common.model.MessageListState
import io.getstream.chat.android.common.model.MessagePosition
import io.getstream.chat.android.common.model.MessagePositionHandler
import io.getstream.chat.android.common.model.MyOwn
import io.getstream.chat.android.common.model.NewMessageState
import io.getstream.chat.android.common.model.Other
import io.getstream.chat.android.common.model.SystemMessageItem
import io.getstream.chat.android.common.model.ThreadSeparatorItem
import io.getstream.chat.android.common.state.DeletedMessageVisibility
import io.getstream.chat.android.common.state.MessageFooterVisibility
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.React
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

public class MessageListController(
    private val cid: String,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val deletedMessageVisibility: DeletedMessageVisibility,
    private val showSystemMessages: Boolean,
    private val showDateSeparators: Boolean,
    private val dateSeparatorThresholdMillis: Long,
    private val messageFooterVisibility: MessageFooterVisibility,
) {

    // TODO
    private val _messageFooterVisibility: MutableStateFlow<MessageFooterVisibility> =
        MutableStateFlow(messageFooterVisibility)

    public val _deletedMessageVisibility: MutableStateFlow<DeletedMessageVisibility> =
        MutableStateFlow(deletedMessageVisibility)

    public fun setMessageFooterVisibility(messageFooterVisibility: MessageFooterVisibility) {
        _messageFooterVisibility.value = messageFooterVisibility
    }

    public fun setDeletedMessageVisibility(deletedMessageVisibility: DeletedMessageVisibility) {
        _deletedMessageVisibility.value = deletedMessageVisibility
    }

    /**
     * Evaluates whether date separators should be added to the message list.
     */
    private var dateSeparatorHandler: DateSeparatorHandler =
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
    private var threadDateSeparatorHandler: DateSeparatorHandler =
        DateSeparatorHandler { previousMessage: Message?, message: Message ->
            if (previousMessage == null) {
                false
            } else {
                (message.getCreatedAtOrThrow().time - previousMessage.getCreatedAtOrThrow().time) > SEPARATOR_TIME
            }
        }

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
     * dispatching events. This fixes several bugs where the input state breaks when deleting or typing really fast.
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
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = setOf()
        )

    /**
     * The information for the current [Channel].
     */
    private val _channel: MutableStateFlow<Channel> = MutableStateFlow(Channel())
    public val channel: StateFlow<Channel> = _channel

    /**
     * Holds the current [MessageMode] that's used for the messages list. [MessageMode.Normal] by default.
     */
    private val _mode: MutableStateFlow<MessageMode> = MutableStateFlow(MessageMode.Normal)
    public val mode: StateFlow<MessageMode> = _mode

    // TODO
    public val channelUnreadCount: StateFlow<Int> = channelState.filterNotNull().flatMapLatest { it.channelUnreadCount }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = 0)

    public val threadUnreadCount: StateFlow<Int> = channelState.filterNotNull().flatMapLatest { it.threadsUnreadCount }
        .combine(_mode) { threadCounts, mode ->
            if (mode is MessageMode.MessageThread) {
                threadCounts[mode.parentMessage.id] ?: 0
            } else {
                0
            }
        }.stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = 0)

    public val unreadCount: StateFlow<Int> = _mode.flatMapLatest {
        if (it is MessageMode.Normal) {
            channelUnreadCount
        } else {
            threadUnreadCount
        }
    }.stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = 0)

    /**
     * Gives us information if we're currently in the [Thread] message mode.
     */
    public val isInThread: Boolean
        get() = _mode.value is MessageMode.MessageThread

    /**
     * The list of typing users.
     */
    private val _typingUsers: MutableStateFlow<List<User>> = MutableStateFlow(listOf())
    public val typingUsers: StateFlow<List<User>> = _typingUsers

    // TODO
    private val _messageListState: MutableStateFlow<MessageListState> = MutableStateFlow(MessageListState())
    public val messageListState: StateFlow<MessageListState> = _messageListState

    // TODO
    private val _threadListState: MutableStateFlow<MessageListState> = MutableStateFlow(MessageListState())
    public val threadListState: StateFlow<MessageListState> = _threadListState

    private val messagesState: MessageListState
        get() = if (isInThread) _threadListState.value else _messageListState.value

    // TODO
    private var lastLoadedMessage: Message? = null

    // TODO
    private var lastLoadedThreadMessage: Message? = null

    // TODO
    private var messagePositionHandler: MessagePositionHandler = MessagePositionHandler.defaultHandler()
    public fun setMessagePositionHandler(messagePositionHandler: MessagePositionHandler) {
        this.messagePositionHandler = messagePositionHandler
    }

    /**
     * Sets the date separator handler which determines when to add date separators.
     * By default, a date separator will be added if the difference between two messages' dates is greater than 4h.
     *
     * @param dateSeparatorHandler The handler to use. If null, [messageListData] won't contain date separators.
     */
    public fun setDateSeparatorHandler(dateSeparatorHandler: DateSeparatorHandler) {
        this.dateSeparatorHandler = dateSeparatorHandler
    }

    /**
     * Sets thread date separator handler which determines when to add date separators inside the thread.
     * @see setDateSeparatorHandler
     *
     * @param threadDateSeparatorHandler The handler to use. If null, [messageListData] won't contain date separators.
     */
    public fun setThreadDateSeparatorHandler(threadDateSeparatorHandler: DateSeparatorHandler) {
        this.threadDateSeparatorHandler = threadDateSeparatorHandler
    }

    /**
     * [Job] that's used to keep the thread data loading operations. We cancel it when the user goes
     * out of the thread state.
     * TODO
     */
    private var threadJob: Job? = null

    private var initJob: Job? = null

    init {
        initChannel()
        observeMessages()
        observeTypingUsers()
    }

    // TODO
    private fun initChannel() {
        initJob = scope.launch {
            channelState.filterNotNull().map { it.toChannel() }.collectLatest { channel ->
                chatClient.notifications.dismissChannelNotifications(
                    channelType = channel.type,
                    channelId = channel.id
                )
                setCurrentChannel(channel)
                initJob?.cancel()
            }
        }
    }

    /**
     * Start observing messages for a given channel, groups and filers them to be show on the ui.
     */
    private fun observeMessages() {
        scope.launch {
            channelState.filterNotNull().collectLatest { channelState ->
                combine(
                    channelState.messagesState,
                    channelState.reads,
                    unreadCount,
                    user,
                    _messageFooterVisibility,
                    _deletedMessageVisibility
                ) { data ->
                    val state = data[0] as MessagesState
                    val reads = (data[1] as List<*>).map { it as ChannelUserRead }
                    val unreadCount = data[2] as Int
                    val user = data[3] as User?

                    when (state) {
                        is MessagesState.Loading,
                        is MessagesState.NoQueryActive,
                        -> _messageListState.value.copy(isLoading = true)
                        MessagesState.OfflineNoResults -> _messageListState.value.copy(
                            isLoading = false
                        )
                        is MessagesState.Result -> _messageListState.value.copy(
                            isLoading = false,
                            messages = groupMessages(
                                filterMessagesToShow(state.messages),
                                isInThread = isInThread,
                                reads = reads
                            ),
                            isLoadingNewerMessages = false,
                            isLoadingOlderMessages = false,
                            endOfNewMessagesReached = channelState.endOfNewerMessages.value,
                            endOfOldMessagesReached = channelState.endOfOlderMessages.value,
                            currentUser = user,
                            unreadCount = unreadCount
                        )
                    }
                }.catch {
                    it.cause?.printStackTrace()
                    showEmptyState()
                }.collect { newState ->
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

                    lastLoadedMessage = newLastMessage
                }
            }
        }
    }

    /**
     * Observes the currently active thread. In process, this
     * creates a [threadJob] that we can cancel once we leave the thread.
     *
     * The data consists of the 'messages', 'user' and 'endOfOlderMessages' states,
     * that are combined into one [MessagesState].
     *
     * @param threadId The message id with the thread we want to observe.
     * @param messages State flow source of thread messages.
     * @param endOfOlderMessages State flow of flag which show if we reached the end of available messages.
     * @param reads State flow source of read states.
     */
    private fun observeThreadMessages(
        threadId: String,
        messages: StateFlow<List<Message>>,
        endOfOlderMessages: StateFlow<Boolean>,
        reads: StateFlow<List<ChannelUserRead>>,
    ) {
        threadJob = scope.launch {
            combine(user,
                endOfOlderMessages,
                messages,
                reads,
                unreadCount
            ) { user, endOfOlderMessages, messages, reads, unreadCount ->
                _threadListState.value.copy(
                    isLoading = false,
                    messages = groupMessages(
                        messages = filterMessagesToShow(messages),
                        isInThread = true,
                        reads = reads,
                    ),
                    endOfOldMessagesReached = endOfOlderMessages,
                    currentUser = user,
                    parentMessageId = threadId,
                    isLoadingNewerMessages = false,
                    isLoadingOlderMessages = false,
                    unreadCount = unreadCount
                )
            }.collect { newState ->
                val newLastMessage =
                    (newState.messages.firstOrNull { it is MessageItem } as? MessageItem)?.message
                _threadListState.value = newState.copy(
                    newMessageState = getNewMessageState(newLastMessage, lastLoadedThreadMessage)
                )
                lastLoadedThreadMessage = newLastMessage
            }
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
            }.shouldAddDateSeparator(previousMessage, message)

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
     * Starts observing the list of typing users.
     */
    private fun observeTypingUsers() {
        scope.launch {
            channelState.filterNotNull().flatMapLatest { it.typing }.collectLatest {
                _typingUsers.value = it.users
            }
        }
    }

    /**
     * Builds the [NewMessageState] for the UI, whenever the message state changes. This is used to
     * allow us to show the user a floating button giving them the option to scroll to the bottom
     * when needed.
     *
     * @param lastMessage Last message in the list, used for comparison.
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
     * Sets the current channel, used to show info in the UI.
     */
    public fun setCurrentChannel(channel: Channel) {
        _channel.value = channel
    }

    /**
     * When the user clicks the scroll to bottom button we need to take the user to the bottom of the newest
     * messages. If the messages are not loaded we need to load them first and then scroll to the bottom of the
     * list.
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
     * @param onResult Callback handler for load newer messages result.
     *
     * @return Whether the data is being loaded or not.
     */
    public fun loadNewerMessages(
        baseMessageId: String,
        messageLimit: Int = DEFAULT_MESSAGES_LIMIT,
    ) {
        if (_mode.value !is MessageMode.Normal ||
            chatClient.clientState.isOffline ||
            channelState.value?.endOfNewerMessages?.value == true
        ) return

        _messageListState.value = _messageListState.value.copy(isLoadingNewerMessages = true)

        chatClient.loadNewerMessages(cid, baseMessageId, messageLimit).enqueue()
    }

    /**
     * Loads more messages if we have reached
     * the oldest message currently loaded.
     */
    public fun loadOlderMessages(
        messageLimit: Int = DEFAULT_MESSAGES_LIMIT,
    ) {
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
     * Load older messages for the specified thread [Mode.Thread.parentMessage].
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
     *  Changes the current [_mode] to be [Thread] with [ThreadState] and Loads thread data using ChatClient
     *  directly. The data is observed by using [ThreadState].
     *
     * @param parentMessage The message with the thread we want to observe.
     */
    public fun enterThreadMode(parentMessage: Message) {
        val channelState = channelState.value ?: return
        val state = chatClient.getRepliesAsState(parentMessage.id, DEFAULT_MESSAGES_LIMIT)
        _mode.value = MessageMode.MessageThread(parentMessage, state)
        observeThreadMessages(
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
     * @param message [Message] which we want to load.
     */
    public fun loadMessageWithPage(messageId: String, onResult: (Result<Message>) -> Unit = {}) {
        chatClient.loadMessageById(cid, messageId).enqueue { result -> onResult(result) }
    }

    /**
     * Deletes the given [message].
     *
     * @param message Message to delete.
     * @param hard Whether we do a hard delete or not.
     */
    public fun deleteMessage(message: Message, hard: Boolean = false) {
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

    /**
     * Marks that the last message in the list was read.
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
     */
    public fun flagMessage(message: Message, onResult: (Result<Flag>) -> Unit = {}) {
        chatClient.flagMessage(message.id).enqueue { result ->
            onActionResult(result, onResult, "Unable to flag message: ${result.error().message}")
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
            onActionResult(result, onResult, "Could not pin the message: ${result.error().message}")
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
            onActionResult(result, onResult, "Could not unpin message: ${result.error().message}")
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
            .enqueue(
                onError = { chatError ->
                    logger.e {
                        "(Retry) Could not send message: ${chatError.message}. " +
                            "Cause: ${chatError.cause?.message}"
                    }
                }
            )
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
            onResult(result)
            if (result.isError) {
                logger.e { "Could not mute user: ${result.error().message}" }
            }
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
            onResult(result)
            if (result.isError) {
                logger.e { "Could not unmute user: ${result.error().message}" }
            }
        }
    }

    /**
     * Triggered when the user chooses the [React] action for the currently selected message. If the
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

    private fun showEmptyState() {
        _messageListState.value = _messageListState.value.copy(isLoading = false, messages = emptyList())
    }

    // TODO
    public fun getMessageWithId(messageId: String): Message? {
        return (_messageListState.value.messages.firstOrNull { it is MessageItem && it.message.id == messageId } as? MessageItem)?.message
    }

    // TODO
    public fun clearNewMessageState() {
        if (!messagesState.endOfNewMessagesReached) return
        _threadListState.value = _threadListState.value.copy(newMessageState = null, unreadCount = 0)
        _messageListState.value = _messageListState.value.copy(newMessageState = null, unreadCount = 0)
    }

    // TODO
    /**
     * Mutes the given user inside this channel.
     *
     * @param userId The ID of the user to be muted.
     * @param timeout The period of time for which the user will
     * be muted, expressed in minutes. A null value signifies that
     * the user will be muted for an indefinite time.
     * TODO
     */
    public fun muteUser(
        userId: String,
        timeout: Int? = null,
    ) {
        chatClient.muteUser(userId, timeout)
            .enqueue(onError = { chatError ->
                val errorMessage = chatError.message ?: chatError.cause?.message ?: "Unable to mute the user"
                StreamLog.e("MessageListViewModel.muteUser") { errorMessage }
            })
    }

    /**
     * Unmutes the given user inside this channel.
     *
     * @param userId The ID of the user to be unmuted.
     *
     * TODO
     */
    public fun unmuteUser(userId: String) {
        chatClient.unmuteUser(userId)
            .enqueue(onError = { chatError ->
                val errorMessage = chatError.message ?: chatError.cause?.message ?: "Unable to unmute the user"

                StreamLog.e("MessageListViewModel.unMuteUser") { errorMessage }
            })
    }

    /**
     * Bans the given user inside this channel.
     *
     * @param userId The ID of the user to be banned.
     * @param reason The reason for banning the user.
     * @param timeout The period of time for which the user will
     * be banned, expressed in minutes. A null value signifies that
     * the user will be banned for an indefinite time.
     * TODO
     */
    public fun banUser(
        userId: String,
        reason: String? = null,
        timeout: Int? = null,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        chatClient.channel(cid).banUser(userId, reason, timeout).enqueue { result ->
            onActionResult(result, onResult, "Unable to ban the user")
        }
    }

    /**
     * Unbans the given user inside this channel.
     *
     * @param userId The ID of the user to be unbanned.
     * TODO
     */
    public fun unbanUser(userId: String, onResult: (Result<Unit>) -> Unit = {}) {
        chatClient.channel(cid).unbanUser(userId).enqueue { result ->
            onActionResult(result, onResult, "Unable to unban the user")
        }
    }

    /**
     * Shadow bans the given user inside this channel.
     *
     * @param userId The ID of the user to be shadow banned.
     * @param reason The reason for shadow banning the user.
     * @param timeout The period of time for which the user will
     * be shadow banned, expressed in minutes. A null value signifies that
     * the user will be shadow banned for an indefinite time.
     * TODO
     */
    public fun shadowBanUser(
        userId: String,
        reason: String? = null,
        timeout: Int? = null,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        chatClient.channel(cid).shadowBanUser(userId, reason, timeout).enqueue { result ->
            onActionResult(result, onResult, "Unable to shadow ban the user")
        }
    }

    /**
     * Removes the shaddow ban for the given user inside
     * this channel.
     *
     * @param userId The ID of the user for which the shadow
     * ban is removed.
     * TODO
     */
    public fun removeShadowBanFromUser(userId: String, onResult: (Result<Unit>) -> Unit = {}) {
        chatClient.channel(cid).removeShadowBan(userId).enqueue { result ->
            onActionResult(result, onResult, "Unable to remove the user shadow ban")
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

    private fun <T: Any> onActionResult(result: Result<T>,onResult: (Result<T>) -> Unit, defaultError: String) {
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

        const val SEPARATOR_TIME = 1000 * 60 * 60 * 4
    }
}