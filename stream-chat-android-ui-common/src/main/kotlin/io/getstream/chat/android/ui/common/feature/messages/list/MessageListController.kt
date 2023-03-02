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

package io.getstream.chat.android.ui.common.feature.messages.list

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.enqueue
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.extractCause
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.wasCreatedAfter
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isError
import io.getstream.chat.android.client.utils.message.isGiphy
import io.getstream.chat.android.client.utils.message.isSystem
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Flag
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessagesState
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.cancelEphemeralMessage
import io.getstream.chat.android.state.extensions.getMessageUsingCache
import io.getstream.chat.android.state.extensions.getRepliesAsState
import io.getstream.chat.android.state.extensions.getRepliesAsStateCall
import io.getstream.chat.android.state.extensions.globalState
import io.getstream.chat.android.state.extensions.loadMessageById
import io.getstream.chat.android.state.extensions.loadNewerMessages
import io.getstream.chat.android.state.extensions.loadNewestMessages
import io.getstream.chat.android.state.extensions.loadOlderMessages
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.state.plugin.state.channel.thread.ThreadState
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import io.getstream.chat.android.ui.common.helper.ClipboardHandler
import io.getstream.chat.android.ui.common.state.messages.Copy
import io.getstream.chat.android.ui.common.state.messages.Delete
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Pin
import io.getstream.chat.android.ui.common.state.messages.React
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.Resend
import io.getstream.chat.android.ui.common.state.messages.ThreadReply
import io.getstream.chat.android.ui.common.state.messages.list.CancelGiphy
import io.getstream.chat.android.ui.common.state.messages.list.DateSeparatorItemState
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageFocusRemoved
import io.getstream.chat.android.ui.common.state.messages.list.MessageFocused
import io.getstream.chat.android.ui.common.state.messages.list.MessageFooterVisibility
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListState
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.common.state.messages.list.MyOwn
import io.getstream.chat.android.ui.common.state.messages.list.NewMessageState
import io.getstream.chat.android.ui.common.state.messages.list.Other
import io.getstream.chat.android.ui.common.state.messages.list.SelectedMessageFailedModerationState
import io.getstream.chat.android.ui.common.state.messages.list.SelectedMessageOptionsState
import io.getstream.chat.android.ui.common.state.messages.list.SelectedMessageReactionsPickerState
import io.getstream.chat.android.ui.common.state.messages.list.SelectedMessageReactionsState
import io.getstream.chat.android.ui.common.state.messages.list.SelectedMessageState
import io.getstream.chat.android.ui.common.state.messages.list.SendGiphy
import io.getstream.chat.android.ui.common.state.messages.list.ShuffleGiphy
import io.getstream.chat.android.ui.common.state.messages.list.SystemMessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.ThreadDateSeparatorItemState
import io.getstream.chat.android.ui.common.state.messages.list.TypingItemState
import io.getstream.chat.android.ui.common.utils.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.ui.common.utils.extensions.isModerationFailed
import io.getstream.chat.android.ui.common.utils.extensions.shouldShowMessageFooter
import io.getstream.log.TaggedLogger
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
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
import io.getstream.chat.android.ui.common.state.messages.Flag as FlagMessage

/**
 * Controller responsible for handling message list state. It acts as a central place for core business logic and state
 * required to show the message list, message thread and handling message actions.
 *
 * @param cid The channel id in the format messaging:123.
 * @param clipboardHandler [ClipboardHandler] used to copy messages.
 * @param messageId The message id to which we want to scroll to when opening the message list.
 * @param parentMessageId The ID of the parent [Message] if the message we want to scroll to is in a thread. If the
 * message we want to scroll to is not in a thread, you can pass in a null value.
 * @param messageLimit The limit of messages being fetched with each page od data.
 * @param chatClient The client used to communicate with the API.
 * @param clientState The current state of the SDK.
 * @param deletedMessageVisibility The [DeletedMessageVisibility] to be applied to the list.
 * @param showSystemMessages Determines if the system messages should be shown or not.
 * @param messageFooterVisibility Determines if and when the message footer is visible or not.
 * @param enforceUniqueReactions Determines whether the user can send only a single or multiple reactions to a message.
 * If it is true the new reaction will override the old reaction.
 * @param dateSeparatorHandler Determines the visibility of date separators inside the message list.
 * @param threadDateSeparatorHandler Determines the visibility of date separators inside the thread.
 * @param messagePositionHandler Determines the position of the message inside a group.
 */
public class MessageListController(
    private val cid: String,
    private val clipboardHandler: ClipboardHandler,
    private val messageId: String? = null,
    private val parentMessageId: String? = null,
    public val messageLimit: Int = DEFAULT_MESSAGES_LIMIT,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val clientState: ClientState = chatClient.clientState,
    globalState: GlobalState = chatClient.globalState,
    private val deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
    private val showSystemMessages: Boolean = true,
    private val messageFooterVisibility: MessageFooterVisibility = MessageFooterVisibility.WithTimeDifference(),
    private val enforceUniqueReactions: Boolean = true,
    private val dateSeparatorHandler: DateSeparatorHandler = DateSeparatorHandler.getDefaultDateSeparatorHandler(),
    private val threadDateSeparatorHandler: DateSeparatorHandler =
        DateSeparatorHandler.getDefaultThreadDateSeparatorHandler(),
    private val messagePositionHandler: MessagePositionHandler = MessagePositionHandler.defaultHandler(),
) {

    /**
     * The logger used to print to errors, warnings, information and other things to log.
     */
    private val logger: TaggedLogger by taggedLogger("MessageListController")

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
            // Using 0 as limit will skip insertion of new messages if we are loading a specific message when
            // opening the MessageList screen. Prevents race condition where wrong messages might get loaded.
            messageLimit = if (messageId != null) 0 else messageLimit,
            coroutineScope = scope,
        )

    /**
     * Gives us information about the online state of the device.
     */
    public val connectionState: StateFlow<ConnectionState> = clientState.connectionState

    /**
     * Gives us information about the logged in user state.
     */
    public val user: StateFlow<User?> = globalState.user

    /**
     * Holds information about the abilities the current user is able to exercise in the given channel.
     *
     * e.g. send messages, delete messages, etc...
     * For a full list @see [ChannelCapabilities].
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
     * Holds the information of which message needs to be removed from focus.
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

        messageId?.takeUnless { it.isBlank() }?.let { messageId ->
            scope.launch {
                if (parentMessageId != null) {
                    enterThreadSequential(parentMessageId)
                }

                listState
                    .onCompletion {
                        when {
                            _mode.value is MessageMode.Normal -> {
                                focusChannelMessage(messageId)
                            }
                            _mode.value is MessageMode.MessageThread && parentMessageId != null -> {
                                focusThreadMessage(
                                    threadMessageId = messageId,
                                    parentMessageId = parentMessageId
                                )
                            }
                        }
                    }
                    .first { it.messageItems.isNotEmpty() }
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
                focusedMessage,
                channelState.endOfNewerMessages
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
                val endOfNewerMessages = data[9] as Boolean

                when (state) {
                    is MessagesState.Loading,
                    is MessagesState.NoQueryActive,
                    -> _messageListState.value.copy(isLoading = true)
                    is MessagesState.OfflineNoResults -> _messageListState.value.copy(isLoading = false)
                    is MessagesState.Result -> _messageListState.value.copy(
                        isLoading = false,
                        messageItems = groupMessages(
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
                            focusedMessage = focusedMessage,
                        ),
                        endOfNewMessagesReached = endOfNewerMessages
                    )
                }
            }.distinctUntilChanged()
        }.catch {
            it.cause?.printStackTrace()
            showEmptyState()
        }.onEach { newState ->
            if (_messageListState.value.messageItems.isEmpty() &&
                !newState.endOfNewMessagesReached &&
                messageId == null
            ) return@onEach

            val newLastMessage =
                newState.messageItems.lastOrNull { it is MessageItemState || it is SystemMessageItemState }?.let {
                    when (it) {
                        is MessageItemState -> it.message
                        is SystemMessageItemState -> it.message
                        else -> null
                    }
                }

            val newMessageState = getNewMessageState(newLastMessage, lastLoadedMessage)
            _messageListState.value = newState.copy(newMessageState = newMessageState)
            if (newMessageState != null) lastLoadedMessage = newLastMessage
        }.launchIn(scope)

        channelState.filterNotNull().flatMapLatest { it.endOfOlderMessages }.onEach {
            _messageListState.value = _messageListState.value.copy(endOfOldMessagesReached = it)
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
     * @param endOfOlderMessages State flow which signals when end of older messages is reached.
     * @param reads State flow source of read states.
     */
    @Suppress("MagicNumber", "LongMethod")
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
                val focusedMessage = data[8] as Message?

                _threadListState.value.copy(
                    isLoading = false,
                    messageItems = groupMessages(
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
                        focusedMessage = focusedMessage
                    ),
                    parentMessageId = threadId,
                    endOfNewMessagesReached = true
                )
            }.collect { newState ->
                val newLastMessage =
                    (newState.messageItems.lastOrNull { it is MessageItemState } as? MessageItemState)?.message
                val newMessageState = getNewMessageState(newLastMessage, lastLoadedThreadMessage)
                _threadListState.value = newState.copy(newMessageState = newMessageState)
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
     * @return A list of [MessageListItemState]s, each containing a position.
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
    ): List<MessageListItemState> {
        val parentMessageId = (_mode.value as? MessageMode.MessageThread)?.parentMessage?.id
        val currentUser = user.value
        val groupedMessages = mutableListOf<MessageListItemState>()
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
                previousMessage = previousMessage,
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
                groupedMessages.add(DateSeparatorItemState(message.getCreatedAtOrThrow()))
            }

            if (message.isSystem() || message.isError()) {
                groupedMessages.add(SystemMessageItemState(message = message))
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
                    MessageItemState(
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
                    ThreadDateSeparatorItemState(
                        date = message.createdAt ?: message.createdLocallyAt ?: Date(),
                        replyCount = message.replyCount
                    )
                )
            }
        }

        if (typingUsers.isNotEmpty()) {
            groupedMessages.add(TypingItemState(typingUsers))
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
            val shouldNotShowIfDeleted = when (deletedMessageVisibility) {
                DeletedMessageVisibility.ALWAYS_VISIBLE -> true
                DeletedMessageVisibility.VISIBLE_FOR_CURRENT_USER -> {
                    !(it.isDeleted() && it.user.id != currentUser?.id)
                }
                DeletedMessageVisibility.ALWAYS_HIDDEN -> !it.isDeleted()
            }
            val isSystemMessage = it.isSystem() || it.isError()

            shouldNotShowIfDeleted || (isSystemMessage && showSystemMessages)
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
            lastMessage.wasCreatedAfter(lastLoadedMessageDate) &&
                (lastMessage.isGiphy() || lastLoadedMessage.id != lastMessage.id) -> {
                getNewMessageStateForMessage(lastMessage)
            }
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
                when (result) {
                    is Result.Success -> scrollToBottom()
                    is Result.Failure -> {
                        logger.e {
                            "Could not load newest messages. Message: ${result.value.message}. " +
                                "Cause: ${result.value.extractCause()}"
                        }
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
     */
    public fun loadNewerMessages(baseMessageId: String, messageLimit: Int = this.messageLimit) {
        if (isInThread ||
            clientState.isOffline ||
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
        if (clientState.isOffline) return

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
     * Loads older messages for the specified thread [MessageMode.MessageThread.parentMessage].
     *
     * @param threadMode Current thread mode containing information about the thread.
     * @param messageLimit The size of the message list page to load.
     */
    private fun threadLoadMore(threadMode: MessageMode.MessageThread, messageLimit: Int = this.messageLimit) {
        if (_threadListState.value.endOfOldMessagesReached) return

        if (threadMode.threadState != null) {
            chatClient.getRepliesMore(
                messageId = threadMode.parentMessage.id,
                firstId = threadMode.threadState.oldestInThread.value?.id ?: threadMode.parentMessage.id,
                limit = messageLimit,
            ).enqueue {
                _threadListState.value = _threadListState.value.copy(isLoadingOlderMessages = false)
            }
            _threadListState.value = _threadListState.value.copy(isLoadingOlderMessages = true)
        } else {
            logger.w { "Thread state must be not null for offline plugin thread load more!" }
        }
    }

    /**
     *  Changes the current [_mode] to be [MessageMode.MessageThread] and uses [ChatClient] to get the [ThreadState] for
     *  the current thread.
     *
     * @param parentMessage The root [Message] which contains the thread we want to show.
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
     *  Changes the current [_mode] to be [Thread] with [ThreadState] and Loads thread data using ChatClient
     *  directly. The data is observed by using [ThreadState].
     *
     *  The difference between [enterThreadMode] and [enterThreadSequential] is that the latter makes a call to a
     *  [ChatClient] extension function which will return a [ThreadState] instance only once the API call had finished,
     *  while the former calls a different function which returns  a [ThreadState] instance immediately after the API
     *  request has fired, regardless of its completion state.
     *
     * @param parentMessage The message with the thread we want to observe.
     */
    private suspend fun enterThreadSequential(parentMessage: Message) {
        val result = chatClient.getRepliesAsStateCall(parentMessage.id, DEFAULT_MESSAGES_LIMIT).await()
        val channelState = channelState.value ?: return

        when (result) {
            is Result.Success -> {
                val threadState = result.value

                _messageActions.value = _messageActions.value + Reply(parentMessage)
                _mode.value = MessageMode.MessageThread(parentMessage, threadState)

                observeThreadMessagesState(
                    threadId = threadState.parentId,
                    messages = threadState.messages,
                    endOfOlderMessages = threadState.endOfOlderMessages,
                    reads = channelState.reads
                )
            }
            is Result.Failure -> {
                val error = result.value

                logger.e {
                    "[enterThreadSequential] -> Could not load thread: ${error.message}."
                }
            }
        }
    }

    /**
     * Fetches the message with the given ID internally and then calls [lastLoadedThreadMessage].
     *
     * @param parentMessageId The ID of the message we are trying to fetch.
     */
    private suspend fun enterThreadSequential(parentMessageId: String) {
        val result = chatClient.getMessageUsingCache(parentMessageId).await()

        when (result) {
            is Result.Success -> {
                enterThreadSequential(result.value)
            }
            is Result.Failure -> {
                val error = result.value

                logger.e {
                    "[enterThreadSequential] -> Could not get message: ${error.message}."
                }
            }
        }
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
        chatClient.loadMessageById(cid, messageId).enqueue { result ->
            onResult(result)
            if (result is Result.Failure) {
                val error = result.value
                logger.e {
                    "Could not load the message with id: $messageId inside channel: $cid. " +
                        "Error: ${error.extractCause()}. Message: ${error.message}"
                }
            }
        }
    }

    /**
     * Scrolls to the selected message. If the message is not currently in the list it will first load a page with the
     * message in the middle of it, add it to the list and then notify to scroll to the message.
     *
     * @param messageId The ID of the [Message] we wish to scroll to.
     * @param parentMessageId The ID of the parent [Message] if the message we want to scroll to is in a thread. If the
     * message we want to scroll to is not in a thread, you can pass in a null value.
     */
    public fun scrollToMessage(
        messageId: String,
        parentMessageId: String?,
    ) {
        focusMessage(messageId, parentMessageId)
    }

    /**
     * Sets the focused message to be the message with the given ID, after which it removes it from
     * focus with a delay.
     *
     * @param messageId The ID of the message.
     * @param parentMessageId The ID of the parent [Message] if the message we want to scroll to is in a thread. If the
     * message we want to scroll to is not in a thread, you can pass in a null value.
     */
    private fun focusMessage(
        messageId: String,
        parentMessageId: String?,
    ) {
        if (parentMessageId == null) {
            focusChannelMessage(messageId)
        } else {
            focusThreadMessage(
                threadMessageId = messageId,
                parentMessageId = parentMessageId
            )
        }
    }

    /**
     * Loads the messages surrounding the target message we want to focus on and puts the target message in focus.
     *
     * @param messageId The ID of the message we want to focus
     */
    private fun focusChannelMessage(messageId: String) {
        val message = getMessageFromListStateById(messageId)

        if (message != null) {
            focusedMessage.value = message
        } else {
            loadMessageById(messageId) { result ->
                focusedMessage.value = when (result) {
                    is Result.Success -> result.value
                    is Result.Failure -> {
                        val error = result.value

                        logger.e {
                            "[focusChannelMessage] -> Could not load message: ${error.message}."
                        }
                        null
                    }
                }
            }
        }
    }

    /**
     * Enters the thread if it has not already been entered and focuses on the given message.
     *
     * @param threadMessageId The ID of the thread message to be focused.
     * @param parentMessageId The ID of the parent message of the thread.
     */
    private fun focusThreadMessage(
        threadMessageId: String,
        parentMessageId: String
    ) {
        scope.launch {

            val mode = _mode.value
            if (mode !is MessageMode.MessageThread || mode.parentMessage.id != parentMessageId) {
                enterThreadSequential(parentMessageId)
            }

            val threadMessageResult = chatClient.getMessageUsingCache(messageId = threadMessageId).await()

            focusedMessage.value = when (threadMessageResult) {
                is Result.Success -> threadMessageResult.value
                is Result.Failure -> {
                    val error = threadMessageResult.value

                    logger.e {
                        "[focusThreadMessage] -> Could not focus thread parent: ${error.message}."
                    }
                    null
                }
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

                val messages = messagesState.messageItems.map {
                    if (it is MessageItemState && it.message.id == messageId) {
                        it.copy(focusState = MessageFocusRemoved)
                    } else {
                        it
                    }
                }
                _messageListState.value = _messageListState.value.copy(messageItems = messages)

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
            is React -> reactToMessage(messageAction.reaction, messageAction.message)
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
     * Resets the [MessagesState]s, to remove the message overlay, by setting 'selectedMessageState' to null.
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
                            "Cause: ${chatError.extractCause()}. If you're using OfflinePlugin, the message " +
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
        if (message.id == lastLoadedMessage?.id) {
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
                                "Cause: ${chatError.extractCause()}"
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
            if (response is Result.Failure) {
                val error = response.value
                onActionResult(error) {
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
            onActionResult(error) {
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
            onActionResult(error) {
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
                        "Cause: ${chatError.extractCause()}"
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
            onActionResult(error) {
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
            onActionResult(error) {
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
     */
    public fun reactToMessage(reaction: Reaction, message: Message) {
        if (message.ownReactions.any { it.type == reaction.type }) {
            chatClient.deleteReaction(
                messageId = message.id,
                reactionType = reaction.type,
                cid = cid
            ).enqueue(
                onError = { chatError ->
                    logger.e {
                        "Could not delete reaction for message with id: ${reaction.messageId} " +
                            "Error: ${chatError.message}. Cause: ${chatError.extractCause()}"
                    }
                }
            )
        } else {
            chatClient.sendReaction(
                enforceUnique = enforceUniqueReactions,
                reaction = reaction,
                cid = cid
            ).enqueue(
                onError = { chatError ->
                    logger.e {
                        "Could not send reaction for message with id: ${reaction.messageId} " +
                            "Error: ${chatError.message}. Cause: ${chatError.extractCause()}"
                    }
                }
            )
        }
    }

    /**
     * Clears the messages list and shows a clear list state.
     */
    private fun showEmptyState() {
        _messageListState.value = _messageListState.value.copy(isLoading = false, messageItems = emptyList())
    }

    /**
     * Gets the message if it is inside the list.
     *
     * @param messageId The [Message] id we are looking for.
     *
     * @return The [Message] with the given id or null if the message is not in the list.
     */
    public fun getMessageFromListStateById(messageId: String): Message? {
        return (
            listState.value.messageItems.firstOrNull {
                it is MessageItemState && it.message.id == messageId
            } as? MessageItemState
            )?.message
    }

    /**
     * Fetches the message with the given ID. Spares unnecessary API calls by first trying to load
     * the message from the database and only if that fails will it then initiate an API call.
     *
     * @param messageId The ID of the message we are fetching.
     *
     * @return The message with the given ID if it was successfully fetched, null otherwise.
     */
    public suspend fun getMessageById(messageId: String): Message? {
        val result = chatClient.getMessageUsingCache(messageId).await()

        return when (result) {
            is Result.Success -> result.value
            is Result.Failure -> {
                null
            }
        }
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
                val errorMessage = chatError.message
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
                val errorMessage = chatError.message
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
            onActionResult(error) {
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
            onActionResult(error) {
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
            onActionResult(error) {
                ErrorEvent.BlockUserError(it)
            }
        })
    }

    /**
     * Removes the shadow ban for the given user inside
     * this channel.
     *
     * @param userId The ID of the user for which the shadow ban is removed.
     */
    public fun removeShadowBanFromUser(userId: String) {
        chatClient.channel(cid).removeShadowBan(userId).enqueue(onError = { error ->
            onActionResult(error) {
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
                    "Error: ${chatError.message}. Cause: ${chatError.extractCause()}"
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
            when (result) {
                is Result.Success -> {
                    val message = result.value

                    message.attachments.removeAll { attachment ->
                        val imageUrl = attachmentToBeDeleted.imageUrl
                        val assetUrl = attachmentToBeDeleted.assetUrl

                        when {
                            assetUrl != null -> {
                                attachment.assetUrl?.substringBefore("?") ==
                                    assetUrl.substringBefore("?")
                            }
                            imageUrl != null -> {
                                attachment.imageUrl?.substringBefore("?") ==
                                    imageUrl.substringBefore("?")
                            }
                            else -> false
                        }
                    }

                    if (message.text.isBlank() && message.attachments.isEmpty()) {
                        chatClient.deleteMessage(messageId = messageId).enqueue(
                            onError = { chatError ->
                                logger.e {
                                    "Could not remove the attachment and delete the remaining blank message" +
                                        ": ${chatError.message}. Cause: ${chatError.extractCause()}"
                                }
                            }
                        )
                    } else {
                        chatClient.updateMessage(message).enqueue(
                            onError = { chatError ->
                                logger.e {
                                    "Could not edit message to remove its attachments: ${chatError.message}. " +
                                        "Cause: ${chatError.extractCause()}"
                                }
                            }
                        )
                    }
                }
                is Result.Failure -> logger.e { "Could not load message: ${result.value}" }
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
     * Sets the thread date separator handler which determines when to add date separators inside the thread.
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
     * @param areSystemMessagesVisible Whether system messages should be visible or not.
     */
    public fun setSystemMessageVisibility(areSystemMessagesVisible: Boolean) {
        _showSystemMessagesState.value = areSystemMessagesVisible
    }

    /**
     * Quality of life function that notifies the result of an action and logs any error in case the action has failed.
     *
     * @param error The [ChatError] thrown if the action fails.
     * @param onError Handler to wrap [ChatError] into [ErrorEvent] depending on action.
     */
    private fun onActionResult(
        error: ChatError,
        onError: (ChatError) -> ErrorEvent,
    ) {
        val errorMessage = error.message
        logger.e { errorMessage }
        _errorEvents.value = onError(error)
    }

    /**
     * Cancels any pending work when the parent ViewModel is about to be destroyed.
     */
    public fun onCleared() {
        scope.cancel()
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
