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

package io.getstream.chat.android.common.messagelist

import com.getstream.sdk.chat.utils.extensions.getCreatedAtOrThrow
import com.getstream.sdk.chat.utils.extensions.isModerationFailed
import com.getstream.sdk.chat.utils.extensions.shouldShowMessageFooter
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.enqueue
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.wasCreatedAfter
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.common.extensions.isError
import io.getstream.chat.android.common.extensions.isSystem
import io.getstream.chat.android.common.model.messsagelist.DateSeparatorItem
import io.getstream.chat.android.common.model.messsagelist.MessageItem
import io.getstream.chat.android.common.model.messsagelist.MessageListItem
import io.getstream.chat.android.common.model.messsagelist.SystemMessageItem
import io.getstream.chat.android.common.model.messsagelist.ThreadSeparatorItem
import io.getstream.chat.android.common.model.messsagelist.TypingItem
import io.getstream.chat.android.common.state.Copy
import io.getstream.chat.android.common.state.Delete
import io.getstream.chat.android.common.state.DeletedMessageVisibility
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.MessageFooterVisibility
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.Pin
import io.getstream.chat.android.common.state.React
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.common.state.Resend
import io.getstream.chat.android.common.state.ThreadReply
import io.getstream.chat.android.common.state.messagelist.MessageFocusRemoved
import io.getstream.chat.android.common.state.messagelist.MessageFocused
import io.getstream.chat.android.common.state.messagelist.MessagePosition
import io.getstream.chat.android.common.state.messagelist.MyOwn
import io.getstream.chat.android.common.state.messagelist.NewMessageState
import io.getstream.chat.android.common.state.messagelist.Other
import io.getstream.chat.android.common.state.messagelist.SelectedMessageFailedModerationState
import io.getstream.chat.android.common.state.messagelist.SelectedMessageOptionsState
import io.getstream.chat.android.common.state.messagelist.SelectedMessageReactionsPickerState
import io.getstream.chat.android.common.state.messagelist.SelectedMessageReactionsState
import io.getstream.chat.android.common.state.messagelist.SelectedMessageState
import io.getstream.chat.android.common.util.ClipboardHandler
import io.getstream.chat.android.core.internal.InternalStreamChatApi
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
import io.getstream.chat.android.common.state.Flag as FlagMessage

/**
 * Controller responsible for handling message list state. It acts as a central place for core business logic and state
 * required to show the message list, message thread and handling message actions.
 *
 * @param cid The channel id in the format messaging:123.
 * @param clipboardHandler [ClipboardHandler] used to copy messages.
 * @param messageId The message id to which we want to scroll to when opening the message list.
 * @param messageLimit The limit of messages being fetched with each page od data.
 * @param chatClient The client used to communicate with the API.
 * @param deletedMessageVisibility The [DeletedMessageVisibility] to be applied to the list.
 * @param showSystemMessages Determines if the system messages should be shown or not.
 * @param messageFooterVisibility Determines if and when the message footer is visible or not.
 * @param enforceUniqueReactions Determines whether the user can send only a single or multiple reactions to a message.
 * @param dateSeparatorHandler Determines the visibility of date separators inside the message list.
 * @param threadDateSeparatorHandler Determines the visibility of date separators inside the thread.
 * @param messagePositionHandler Determines the position of the message inside a group.
 */
public class MessageListController(
    private val cid: String,
    private val clipboardHandler: ClipboardHandler,
    private val messageId: String? = null,
    public val messageLimit: Int = DEFAULT_MESSAGES_LIMIT,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
    private val showSystemMessages: Boolean = true,
    private val messageFooterVisibility: MessageFooterVisibility = MessageFooterVisibility.WithTimeDifference(),
    private val enforceUniqueReactions: Boolean = true,
    private val dateSeparatorHandler: DateSeparatorHandler = DateSeparatorHandler.getDefaultDateSeparator(),
    private val threadDateSeparatorHandler: DateSeparatorHandler = DateSeparatorHandler.getDefaultThreadDateSeparator(),
    private val messagePositionHandler: MessagePositionHandler = MessagePositionHandler.defaultHandler(),
) {

    /**
     * The logger used to print to errors, warnings, information and other things to log.
     */
    private val logger: TaggedLogger = StreamLog.getLogger("MessageListController")

    /**
     * Creates a [CoroutineScope] that allows us to cancel the ongoing work when the parent ViewModel is disposed.
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
            messageLimit = if (messageId != null) 0 else messageLimit,
            coroutineScope = scope,
        )

    /**
     * Gives us information about the online state of the device.
     */
    public val connectionState: StateFlow<ConnectionState> = chatClient.clientState.connectionState

    /**
     * Gives us information about the logged in user state.
     */
    public val user: StateFlow<User?> = chatClient.clientState.user

    /**
     * Holds information about the abilities the current user is able to exercise in the given channel.
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
        .distinctUntilChanged()
        .stateIn(scope = scope, started = SharingStarted.Eagerly, Channel())

    /**
     * Holds the current [MessageMode] that's used for the messages list. [MessageMode.Normal] by default.
     */
    private val _mode: MutableStateFlow<MessageMode> = MutableStateFlow(MessageMode.Normal)
    public val mode: StateFlow<MessageMode> = _mode

    /**
     * Gives us information if we're currently in the [MessageMode.MessageThread] mode.
     */
    public val isInThread: Boolean
        get() = _mode.value is MessageMode.MessageThread

    /**
     * Emits error events.
     */
    private val _errorEvents: MutableStateFlow<ErrorEvent?> = MutableStateFlow(null)
    public val errorEvents: StateFlow<ErrorEvent?> = _errorEvents

    // TODO separate unreads to message list unreads and thread unreads after
    //  https://github.com/GetStream/stream-chat-android/pull/4122 has been merged in
    public val unreadCount: StateFlow<Int> = channelState.filterNotNull()
        .flatMapLatest { it.unreadCount }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = 0)

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
    private val _messageListState: MutableStateFlow<MessageListState> =
        MutableStateFlow(MessageListState(isLoading = true))
    public val messageListState: StateFlow<MessageListState> = _messageListState

    /**
     * Current state of the thread message list.
     */
    private val _threadListState: MutableStateFlow<MessageListState> =
        MutableStateFlow(MessageListState(isLoading = true))
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
    }.stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = MessageListState(isLoading = true))

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
     * Set of currently active [MessageAction]s. Used to show things like edit, reply, delete and similar actions.
     */
    private val _messageActions: MutableStateFlow<Set<MessageAction>> = MutableStateFlow(emptySet())
    public val messageActions: StateFlow<Set<MessageAction>> = _messageActions

    /**
     * [MessagePositionHandler] that determines the message position inside the group.
     */
    private var _messagePositionHandler: MutableStateFlow<MessagePositionHandler> =
        MutableStateFlow(messagePositionHandler)

    /**
     * Evaluates whether and when date separators should be added to the message list.
     */
    private val _dateSeparatorHandler: MutableStateFlow<DateSeparatorHandler> = MutableStateFlow(dateSeparatorHandler)

    /**
     * Evaluates whether and when thread date separators should be added to the message list.
     */
    private val _threadDateSeparatorHandler: MutableStateFlow<DateSeparatorHandler> =
        MutableStateFlow(threadDateSeparatorHandler)

    /**
     * Determines whether we should show system messages or not.
     */
    private val _showSystemMessagesState: MutableStateFlow<Boolean> = MutableStateFlow(showSystemMessages)
    public val showSystemMessagesState: StateFlow<Boolean> = _showSystemMessagesState

    /**
     * Regulates the message footer visibility.
     */
    private val _messageFooterVisibilityState: MutableStateFlow<MessageFooterVisibility> =
        MutableStateFlow(messageFooterVisibility)
    public val messageFooterVisibilityState: StateFlow<MessageFooterVisibility> = _messageFooterVisibilityState

    /**
     * Regulates the visibility of deleted messages.
     */
    private val _deletedMessageVisibilityState: MutableStateFlow<DeletedMessageVisibility> =
        MutableStateFlow(deletedMessageVisibility)
    public val deletedMessageVisibilityState: StateFlow<DeletedMessageVisibility> = _deletedMessageVisibilityState

    /**
     * Represents the message we wish to scroll to.
     */
    private var focusedMessage: MutableStateFlow<Message?> = MutableStateFlow(null)

    /**
     * Holds the information of which needs to be removed from focus.
     */
    private var removeFocusedMessageJob: Pair<String, Job>? = null

    /**
     * Whether the user is inside search or not.
     */
    public val isInsideSearch: StateFlow<Boolean> = channelState.filterNotNull()
        .flatMapLatest { it.insideSearch }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = false)

    /**
     * [Job] that's used to keep the thread data loading operations. We cancel it when the user goes
     * out of the thread state.
     */
    private var threadJob: Job? = null

    /**
     * We start observing messages and if the message list screen was started after searching for a message, it will
     * load the message if it is not in the list and scroll to it.
     */
    init {
        observeMessagesListState()

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
    @Suppress("MagicNumber")
    private fun observeMessagesListState() {
        channelState.filterNotNull().flatMapLatest { channelState ->
            combine(
                channelState.messagesState,
                channelState.reads,
                _showSystemMessagesState,
                _dateSeparatorHandler,
                _deletedMessageVisibilityState,
                _messageFooterVisibilityState,
                _messagePositionHandler,
                typingUsers,
                focusedMessage
            ) { data ->
                val state = data[0] as MessagesState
                val reads = data[1] as List<ChannelUserRead>
                val showSystemMessages = data[2] as Boolean
                val dateSeparatorHandler = data[3] as DateSeparatorHandler
                val deletedMessageVisibility = data[4] as DeletedMessageVisibility
                val messageFooterVisibility = data[5] as MessageFooterVisibility
                val messagePositionHandler = data[6] as MessagePositionHandler
                val typingUsers = data[7] as List<User>
                val focusedMessage = data[8] as Message?

                when (state) {
                    is MessagesState.Loading,
                    is MessagesState.NoQueryActive,
                    -> _messageListState.value.copy(isLoading = true)
                    is MessagesState.OfflineNoResults -> _messageListState.value.copy(isLoading = false)
                    is MessagesState.Result -> _messageListState.value.copy(
                        isLoading = false,
                        messages = groupMessages(
                            messages = filterMessagesToShow(
                                messages = state.messages,
                                showSystemMessages = showSystemMessages,
                                deletedMessageVisibility = deletedMessageVisibility
                            ),
                            isInThread = false,
                            reads = reads,
                            dateSeparatorHandler = dateSeparatorHandler,
                            deletedMessageVisibility = deletedMessageVisibility,
                            messageFooterVisibility = messageFooterVisibility,
                            messagePositionHandler = messagePositionHandler,
                            typingUsers = typingUsers,
                            focusedMessage = focusedMessage
                        ),
                    )
                }
            }
        }.catch {
            it.cause?.printStackTrace()
            showEmptyState()
        }.onEach { newState ->
            val newLastMessage =
                (newState.messages.lastOrNull { it is MessageItem } as? MessageItem)?.message

            val newMessageState = getNewMessageState(newLastMessage, lastLoadedMessage)
            _messageListState.value = newState.copy(
                newMessageState = newMessageState
            )
            if (newMessageState != null) lastLoadedMessage = newLastMessage
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

        // TODO separate unreads to message list unreads and thread unreads after
        //  https://github.com/GetStream/stream-chat-android/pull/4122 has been merged in
        unreadCount.onEach {
            _messageListState.value = _messageListState.value.copy(unreadCount = it)
        }.launchIn(scope)

        channelState.filterNotNull().flatMapLatest { it.loadingOlderMessages }.onEach {
            _messageListState.value = _messageListState.value.copy(isLoadingOlderMessages = it)
        }.launchIn(scope)

        channelState.filterNotNull().flatMapLatest { it.loadingNewerMessages }.onEach {
            _messageListState.value = _messageListState.value.copy(isLoadingNewerMessages = it)
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
    @Suppress("MagicNumber")
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
                _showSystemMessagesState,
                _threadDateSeparatorHandler,
                _deletedMessageVisibilityState,
                _messageFooterVisibilityState,
                _messagePositionHandler,
                typingUsers,
                focusedMessage
            ) { data ->
                val messages = data[0] as List<Message>
                val reads = data[1] as List<ChannelUserRead>
                val showSystemMessages = data[2] as Boolean
                val dateSeparatorHandler = data[3] as DateSeparatorHandler
                val deletedMessageVisibility = data[4] as DeletedMessageVisibility
                val messageFooterVisibility = data[5] as MessageFooterVisibility
                val messagePositionHandler = data[6] as MessagePositionHandler
                val typingUsers = data[7] as List<User>

                _threadListState.value.copy(
                    isLoading = false,
                    messages = groupMessages(
                        messages = filterMessagesToShow(
                            messages = messages,
                            showSystemMessages = showSystemMessages,
                            deletedMessageVisibility = deletedMessageVisibility
                        ),
                        isInThread = true,
                        reads = reads,
                        dateSeparatorHandler = dateSeparatorHandler,
                        deletedMessageVisibility = deletedMessageVisibility,
                        messageFooterVisibility = messageFooterVisibility,
                        messagePositionHandler = messagePositionHandler,
                        typingUsers = typingUsers,
                        focusedMessage = null
                    ),
                    parentMessageId = threadId,
                    endOfNewMessagesReached = true
                )
            }.collect { newState ->
                val newLastMessage = (newState.messages.lastOrNull { it is MessageItem } as? MessageItem)?.message
                val newMessageState = getNewMessageState(newLastMessage, lastLoadedThreadMessage)
                _threadListState.value = newState.copy(
                    newMessageState = newMessageState
                )
                if (newMessageState != null) lastLoadedThreadMessage = newLastMessage
            }

            user.onEach {
                _messageListState.value = _threadListState.value.copy(currentUser = it)
            }.launchIn(this)

            endOfOlderMessages.onEach {
                _threadListState.value = _threadListState.value.copy(endOfOldMessagesReached = it)
            }.launchIn(this)

            // TODO separate unreads to message list unreads and thread unreads after
            //  https://github.com/GetStream/stream-chat-android/pull/4122 has been merged in
            unreadCount.onEach {
                _threadListState.value = _messageListState.value.copy(unreadCount = it)
            }.launchIn(scope)
        }
    }

    /**
     * Takes in the available messages for a [Channel] and groups them based on the sender ID. We put the message in a
     * group, where the positions can be [MessagePosition.TOP], [MessagePosition.MIDDLE], [MessagePosition.BOTTOM] or
     * [MessagePosition.NONE] if the message isn't in a group.
     *
     * @param messages The messages we need to group.
     * @param isInThread If we are in inside a thread.
     * @param reads The list of read states.
     * @param deletedMessageVisibility Determines visibility of deleted messages.
     * @param dateSeparatorHandler Handler used to determine when the date separator should be visible.
     * @param messageFooterVisibility Determines when the message footer should be visible.
     * @param messagePositionHandler Determines the message position inside a group of messages.
     * @param typingUsers The list of the users currently typing.
     * @param focusedMessage The message we wish to scroll/focus in center of the screen.
     *
     * @return A list of [MessageListItem]s, each containing a position.
     */
    private fun groupMessages(
        messages: List<Message>,
        isInThread: Boolean,
        reads: List<ChannelUserRead>,
        deletedMessageVisibility: DeletedMessageVisibility,
        dateSeparatorHandler: DateSeparatorHandler,
        messageFooterVisibility: MessageFooterVisibility,
        messagePositionHandler: MessagePositionHandler,
        typingUsers: List<User>,
        focusedMessage: Message?,
    ): List<MessageListItem> {
        val parentMessageId = (_mode.value as? MessageMode.MessageThread)?.parentMessage?.id
        val currentUser = user.value
        val groupedMessages = mutableListOf<MessageListItem>()
        val lastRead = reads
            .filter { it.user.id != currentUser?.id }
            .mapNotNull { it.lastRead }
            .maxOrNull()

        val sortedReads = reads
            .filter { it.user.id != currentUser?.id }
            .sortedBy { it.lastRead }

        messages.forEachIndexed { index, message ->
            val user = message.user
            val previousMessage = messages.getOrNull(index - 1)
            val nextMessage = messages.getOrNull(index + 1)

            val shouldAddDateSeparator = dateSeparatorHandler.shouldAddDateSeparator(previousMessage, message)

            val position = messagePositionHandler.handleMessagePosition(
                prevMessage = previousMessage,
                message = message,
                nextMessage = nextMessage,
                isAfterDateSeparator = shouldAddDateSeparator
            )

            val isLastMessageInGroup =
                position.contains(MessagePosition.BOTTOM) || position.contains(MessagePosition.NONE)

            val shouldShowFooter = messageFooterVisibility.shouldShowMessageFooter(
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

                val messageReadBy = message.createdAt?.let { messageCreatedAt ->
                    sortedReads.filter { it.lastRead?.after(messageCreatedAt) ?: false }
                } ?: emptyList()

                val isMessageFocused = message.id == focusedMessage?.id
                if (isMessageFocused) removeMessageFocus(message.id)

                groupedMessages.add(
                    MessageItem(
                        message = message,
                        currentUser = currentUser,
                        groupPosition = position,
                        parentMessageId = parentMessageId,
                        isMine = user.id == currentUser?.id,
                        isInThread = isInThread,
                        isMessageRead = isMessageRead,
                        deletedMessageVisibility = deletedMessageVisibility,
                        showMessageFooter = shouldShowFooter,
                        messageReadBy = messageReadBy,
                        focusState = if (isMessageFocused) MessageFocused else null
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

        if (typingUsers.isNotEmpty()) {
            groupedMessages.add(TypingItem(typingUsers))
        }

        return groupedMessages
    }

    /**
     * Used to filter messages which we should show to the current user.
     *
     * @param messages List of all messages.
     * @param showSystemMessages Whether we should show system messages or not.
     * @param deletedMessageVisibility The visibility of deleted messages. We filter them out if
     * [DeletedMessageVisibility.ALWAYS_HIDDEN].
     *
     * @return Filtered messages.
     */
    private fun filterMessagesToShow(
        messages: List<Message>,
        showSystemMessages: Boolean,
        deletedMessageVisibility: DeletedMessageVisibility,
    ): List<Message> {
        val currentUser = user.value

        return messages.filter {
            val shouldShowIfDeleted = when (deletedMessageVisibility) {
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
     * @param lastMessage The new last message in the list, used for comparison.
     * @param lastLoadedMessage The last currently loaded message, used for comparison.
     */
    private fun getNewMessageState(lastMessage: Message?, lastLoadedMessage: Message?): NewMessageState? {

        val lastLoadedMessageDate = lastLoadedMessage?.createdAt ?: lastLoadedMessage?.createdLocallyAt

        return when {
            lastMessage == null -> null
            lastLoadedMessage == null -> getNewMessageStateForMessage(lastMessage)
            lastMessage.wasCreatedAfter(lastLoadedMessageDate) && lastLoadedMessage.id != lastMessage.id ->
                getNewMessageStateForMessage(lastMessage)
            else -> null
        }
    }

    /**
     * @param message The message for which we want to determine the state for.
     *
     * @return Returns the [NewMessageState] depending whether the current user sent the message or not.
     */
    private fun getNewMessageStateForMessage(message: Message): NewMessageState {
        val currentUser = user.value
        return if (message.user.id == currentUser?.id) MyOwn else Other
    }

    /**
     * When the user clicks the scroll to bottom button we need to take the user to the bottom of the newest
     * messages. If the messages are not loaded we need to load them first and then scroll to the bottom of the
     * list.
     *
     * @param messageLimit The size of the message list page to load.
     * @param scrollToBottom Handler that notifies when the message has been loaded.
     */
    public fun scrollToBottom(messageLimit: Int = this.messageLimit, scrollToBottom: () -> Unit) {
        if (isInThread || channelState.value?.endOfNewerMessages?.value == true) {
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

    /**
     * Loads newer messages of a channel following the currently newest loaded message. In case of threads this will
     * do nothing.
     *
     * @param baseMessageId The id of the most new [Message] inside the messages list.
     * @param messageLimit The size of the message list page to load.
     */
    public fun loadNewerMessages(baseMessageId: String, messageLimit: Int = this.messageLimit) {
        if (isInThread ||
            chatClient.clientState.isOffline ||
            channelState.value?.endOfNewerMessages?.value == true
        ) return

        chatClient.loadNewerMessages(cid, baseMessageId, messageLimit).enqueue()
    }

    /**
     * Loads more messages if we have reached the oldest message currently loaded.
     *
     * @param messageLimit The size of the message list page to load.
     */
    public fun loadOlderMessages(messageLimit: Int = this.messageLimit) {
        if (chatClient.clientState.isOffline) return

        _mode.value.run {
            when (this) {
                is MessageMode.Normal -> {
                    if (channelState.value?.endOfOlderMessages?.value == true) return
                    chatClient.loadOlderMessages(cid, messageLimit).enqueue()
                }
                is MessageMode.MessageThread -> threadLoadMore(this)
            }
        }
    }

    /**
     * Load older messages for the specified thread [MessageMode.MessageThread.parentMessage].
     *
     * @param threadMode Current thread mode containing information about the thread.
     * @param messageLimit The size of the message list page to load.
     */
    private fun threadLoadMore(threadMode: MessageMode.MessageThread, messageLimit: Int = this.messageLimit) {
        if (threadMode.threadState != null) {
            chatClient.getRepliesMore(
                messageId = threadMode.parentMessage.id,
                firstId = threadMode.threadState.oldestInThread.value?.id ?: threadMode.parentMessage.id,
                limit = messageLimit,
            ).enqueue()
            _threadListState.value = _threadListState.value.copy(isLoadingOlderMessages = true)
        } else {
            logger.w { "Thread state must be not null for offline plugin thread load more!" }
        }
    }

    /**
     *  Changes the current [_mode] to be [MessageMode.MessageThread] and uses [ChatClient] to get the [ThreadState] for
     *  the current thread.
     *
     * @param parentMessage The message with the thread we want to observe.
     * @param messageLimit The size of the message list page to load.
     */
    public fun enterThreadMode(parentMessage: Message, messageLimit: Int = this.messageLimit) {
        val channelState = channelState.value ?: return
        _messageActions.value = _messageActions.value + Reply(parentMessage)
        val state = chatClient.getRepliesAsState(parentMessage.id, messageLimit)
        _mode.value = MessageMode.MessageThread(parentMessage, state)
        observeThreadMessagesState(
            threadId = state.parentId,
            messages = state.messages,
            endOfOlderMessages = state.endOfOlderMessages,
            reads = channelState.reads
        )
    }

    /**
     * Leaves the thread we're in and switches to [MessageMode.Normal].
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
        focusMessage(messageId)
    }

    /**
     * Sets the focused message to be the message with the given ID, after which it removes it from
     * focus with a delay.
     *
     * @param messageId The ID of the message.
     */
    public fun focusMessage(messageId: String) {
        val message = getMessageWithId(messageId)

        if (message != null) {
            focusedMessage.value = message
        } else {
            loadMessageById(messageId) {
                focusedMessage.value = it.data()
            }
        }
    }

    /**
     * Removes the focus from the message with the given ID.
     *
     * @param messageId The ID of the message.
     */
    private fun removeMessageFocus(messageId: String) {
        if (removeFocusedMessageJob?.first != messageId) {
            removeFocusedMessageJob = messageId to scope.launch {
                delay(REMOVE_MESSAGE_FOCUS_DELAY)

                val messages = messagesState.messages.map {
                    if (it is MessageItem && it.message.id == messageId) {
                        it.copy(focusState = MessageFocusRemoved)
                    } else {
                        it
                    }
                }
                _messageListState.value = _messageListState.value.copy(messages = messages)

                if (focusedMessage.value?.id == messageId) {
                    focusedMessage.value = null
                    removeFocusedMessageJob = null
                }
            }
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
                enterThreadMode(messageAction.message)
            }
            is Delete, is FlagMessage -> {
                _messageActions.value = _messageActions.value + messageAction
            }
            is Copy -> copyMessage(messageAction.message)
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
        _messageActions.value = _messageActions.value - _messageActions.value.filterIsInstance<Delete>().toSet()
        removeOverlay()

        chatClient.deleteMessage(message.id, hard)
            .enqueue(
                onError = { chatError ->
                    logger.e {
                        "Could not delete message: ${chatError.message}, Hard: $hard. " +
                            "Cause: ${chatError.cause?.message}. If you're using OfflinePlugin, the message " +
                            "should be deleted in the database and it will be deleted in the backend when " +
                            "the SDK sync its information."
                    }
                }
            )
    }

    /**
     * Updates the last seen message so we can determine the unread count and mark messages as read.
     *
     * @param message The last seen [Message].
     */
    public fun updateLastSeenMessage(message: Message) {
        val latestMessage: MessageItem? = listState.value.messages.lastOrNull { messageItem ->
            messageItem is MessageItem
        } as? MessageItem

        if (message.id == latestMessage?.message?.id) {
            markLastMessageRead()
        }
    }

    /**
     * Marks that the last message in the list as read. This also sets the unread count to 0.
     */
    public fun markLastMessageRead() {
        cid.cidToTypeAndId().let { (channelType, channelId) ->
            val mode = _mode.value
            if (mode is MessageMode.MessageThread) {
                // TODO sort out thread unreads when
                //  https://github.com/GetStream/stream-chat-android/pull/4122 has been merged in
                // chatClient.markThreadRead(channelType, channelId, mode.parentMessage.id)
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
     */
    public fun flagMessage(message: Message, onResult: (Result<Flag>) -> Unit = {}) {
        _messageActions.value = _messageActions.value - _messageActions.value.filterIsInstance<FlagMessage>().toSet()
        chatClient.flagMessage(message.id).enqueue { response ->
            onResult(response)
            if (response.isError) {
                val error = response.error()
                onActionResult(error, "Unable to flag message: ${error.message}") {
                    ErrorEvent.FlagMessageError(it)
                }
            }
        }
    }

    /**
     * Pins or unpins the message from the current channel based on its state.
     *
     * @param message The message to update the pin state of.
     */
    public fun updateMessagePin(message: Message) {
        if (message.pinned) {
            unpinMessage(message)
        } else {
            pinMessage(message)
        }
    }

    /**
     * Pins the message from the current channel.
     *
     * @param message The message to pin.
     */
    public fun pinMessage(message: Message) {
        chatClient.pinMessage(message).enqueue(onError = { error ->
            onActionResult(error, "Could not pin the message: ${error.message}") {
                ErrorEvent.PinMessageError(it)
            }
        })
    }

    /**
     * Unpins the message from the current channel.
     *
     * @param message The message to unpin.
     */
    public fun unpinMessage(message: Message) {
        chatClient.unpinMessage(message).enqueue(onError = { error ->
            onActionResult(error, "Could not unpin message: ${error.message}") {
                ErrorEvent.UnpinMessageError(it)
            }
        })
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
     */
    public fun muteUser(user: User) {
        chatClient.muteUser(user.id).enqueue(onError = { error ->
            onActionResult(error, "Could not mute the user: ${error.message}") {
                ErrorEvent.MuteUserError(it)
            }
        })
    }

    /**
     * Unmutes the given user.
     *
     * @param user The [User] we wish to unmute.
     */
    public fun unmuteUser(user: User) {
        chatClient.unmuteUser(user.id).enqueue(onError = { error ->
            onActionResult(error, "Could not unmute the user: ${error.message}") {
                ErrorEvent.UnmuteUserError(it)
            }
        })
    }

    /**
     * Triggered when the user selects a reaction for the currently selected message. If the message already has that
     * reaction, from the current user, we remove it. Otherwise we add a new reaction.
     *
     * @param reaction The reaction to add or remove.
     * @param message The currently selected message.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     */
    public fun reactToMessage(reaction: Reaction, message: Message, enforceUnique: Boolean = enforceUniqueReactions) {
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
        return (
            _messageListState.value.messages.firstOrNull {
                it is MessageItem && it.message.id == messageId
            } as? MessageItem
            )?.message
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
     */
    public fun banUser(
        userId: String,
        reason: String? = null,
        timeout: Int? = null,
    ) {
        chatClient.channel(cid).banUser(userId, reason, timeout).enqueue(onError = { error ->
            onActionResult(error, "Unable to ban the user: ${error.message}") {
                ErrorEvent.BlockUserError(it)
            }
        })
    }

    /**
     * Unbans the given user inside this channel.
     *
     * @param userId The ID of the user to be unbanned.
     */
    public fun unbanUser(userId: String) {
        chatClient.channel(cid).unbanUser(userId).enqueue(onError = { error ->
            onActionResult(error, "Unable to unban the user: ${error.message}") {
                ErrorEvent.BlockUserError(it)
            }
        })
    }

    /**
     * Shadow bans the given user inside this channel.
     *
     * @param userId The ID of the user to be shadow banned.
     * @param reason The reason for shadow banning the user.
     * @param timeout The period of time for which the user will be shadow banned, expressed in minutes. A null value
     * signifies that the user will be shadow banned for an indefinite time.
     */
    public fun shadowBanUser(
        userId: String,
        reason: String? = null,
        timeout: Int? = null,
    ) {
        chatClient.channel(cid).shadowBanUser(userId, reason, timeout).enqueue(onError = { error ->
            onActionResult(error, "Unable to shadow ban the user: ${error.message}") {
                ErrorEvent.BlockUserError(it)
            }
        })
    }

    /**
     * Removes the shaddow ban for the given user inside
     * this channel.
     *
     * @param userId The ID of the user for which the shadow ban is removed.
     */
    public fun removeShadowBanFromUser(userId: String) {
        chatClient.channel(cid).removeShadowBan(userId).enqueue(onError = { error ->
            onActionResult(error, "Unable to remove the shadow ban for user: ${error.message}") {
                ErrorEvent.BlockUserError(it)
            }
        })
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

    /**
     * Removes a single [Attachment] from a [Message].
     *
     * @param messageId The [Message] id that contains the attachment.
     * @param attachmentToBeDeleted The [Attachment] to be deleted from the message.
     */
    public fun removeAttachment(messageId: String, attachmentToBeDeleted: Attachment) {
        chatClient.loadMessageById(
            cid,
            messageId
        ).enqueue { result ->
            if (result.isSuccess) {
                val message = result.data()
                message.attachments.removeAll { attachment ->
                    if (attachmentToBeDeleted.assetUrl != null) {
                        attachment.assetUrl == attachmentToBeDeleted.assetUrl
                    } else {
                        val isSame = attachment.imageUrl == attachmentToBeDeleted.imageUrl
                        isSame
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
        _messagePositionHandler.value = messagePositionHandler
    }

    /**
     * Sets the date separator handler which determines when to add date separators.
     * By default, a date separator will be added if the difference between two messages' dates is greater than 4h.
     *
     * @param dateSeparatorHandler The handler to use. If null, [_messageListState] won't contain date separators.
     */
    public fun setDateSeparatorHandler(dateSeparatorHandler: DateSeparatorHandler?) {
        _dateSeparatorHandler.value = dateSeparatorHandler ?: DateSeparatorHandler { _, _ -> false }
    }

    /**
     * Sets thread date separator handler which determines when to add date separators inside the thread.
     * @see setDateSeparatorHandler
     *
     * @param threadDateSeparatorHandler The handler to use. If null, [_messageListState] won't contain date separators.
     */
    public fun setThreadDateSeparatorHandler(threadDateSeparatorHandler: DateSeparatorHandler?) {
        _threadDateSeparatorHandler.value = threadDateSeparatorHandler ?: DateSeparatorHandler { _, _ -> false }
    }

    /**
     * Sets the value used to determine if message footer content is shown.
     * @see MessageFooterVisibility
     *
     * @param messageFooterVisibility Changes the visibility of message footers.
     */
    public fun setMessageFooterVisibility(messageFooterVisibility: MessageFooterVisibility) {
        _messageFooterVisibilityState.value = messageFooterVisibility
    }

    /**
     * Sets the value used to filter deleted messages.
     * @see DeletedMessageVisibility
     *
     * @param deletedMessageVisibility Changes the visibility of deleted messages.
     */
    public fun setDeletedMessageVisibility(deletedMessageVisibility: DeletedMessageVisibility) {
        _deletedMessageVisibilityState.value = deletedMessageVisibility
    }

    /**
     * Sets whether the system messages should be visible.
     *
     * @param showSystemMessages Whether system messages should be visible or not.
     */
    public fun setAreSystemMessagesVisible(showSystemMessages: Boolean) {
        _showSystemMessagesState.value = showSystemMessages
    }

    /**
     * Quality of life function that notifies the result of an action and logs and error in case the action has failed.
     *
     * @param error The [ChatError] thrown if the action fails.
     * @param defaultError The default error to be shown on the screen if we can't get the error from the result.
     * @param onError Handler to wrap [ChatError] into [ErrorEvent] depending on action.
     */
    private fun onActionResult(error: ChatError, defaultError: String, onError: (ChatError) -> ErrorEvent) {
        val errorMessage = error.message ?: error.cause?.message ?: defaultError
        logger.e { errorMessage }
        _errorEvents.value = onError(error)
    }

    /**
     * A class designed for error event propagation.
     *
     * @param chatError Contains the original [Throwable] along with a message.
     */
    public sealed class ErrorEvent(public open val chatError: ChatError) {

        /**
         * When an error occurs while muting a user.
         *
         * @param chatError Contains the original [Throwable] along with a message.
         */
        public data class MuteUserError(override val chatError: ChatError) : ErrorEvent(chatError)

        /**
         * When an error occurs while unmuting a user.
         *
         * @param chatError Contains the original [Throwable] along with a message.
         */
        public data class UnmuteUserError(override val chatError: ChatError) : ErrorEvent(chatError)

        /**
         * When an error occurs while flagging a message.
         *
         * @param chatError Contains the original [Throwable] along with a message.
         */
        public data class FlagMessageError(override val chatError: ChatError) : ErrorEvent(chatError)

        /**
         * When an error occurs while blocking a user.
         *
         * @param chatError Contains the original [Throwable] along with a message.
         */
        public data class BlockUserError(override val chatError: ChatError) : ErrorEvent(chatError)

        /**
         * When an error occurs while pinning a message.
         *
         * @param chatError Contains the original [Throwable] along with a message.
         */
        public data class PinMessageError(override val chatError: ChatError) : ErrorEvent(chatError)

        /**
         * When an error occurs while unpinning a message.
         *
         * @param chatError Contains the original [Throwable] along with a message.
         */
        public data class UnpinMessageError(override val chatError: ChatError) : ErrorEvent(chatError)
    }

    public companion object {
        /**
         * The default limit of messages to load.
         */
        @InternalStreamChatApi
        public const val DEFAULT_MESSAGES_LIMIT: Int = 30

        /**
         * Time after which the focus from message will be removed
         */
        internal const val REMOVE_MESSAGE_FOCUS_DELAY: Long = 2000
    }
}
