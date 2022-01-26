package io.getstream.chat.android.compose.viewmodel.messages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.getstream.sdk.chat.viewmodel.messages.getCreatedAtOrThrow
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.common.state.Copy
import io.getstream.chat.android.common.state.Delete
import io.getstream.chat.android.common.state.Flag
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.MuteUser
import io.getstream.chat.android.common.state.Pin
import io.getstream.chat.android.common.state.React
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.common.state.ThreadReply
import io.getstream.chat.android.compose.handlers.ClipboardHandler
import io.getstream.chat.android.compose.state.messages.MessagesState
import io.getstream.chat.android.compose.state.messages.MyOwn
import io.getstream.chat.android.compose.state.messages.NewMessageState
import io.getstream.chat.android.compose.state.messages.Other
import io.getstream.chat.android.compose.state.messages.SelectedMessageOptionsState
import io.getstream.chat.android.compose.state.messages.SelectedMessageReactionsPickerState
import io.getstream.chat.android.compose.state.messages.SelectedMessageReactionsState
import io.getstream.chat.android.compose.state.messages.SelectedMessageState
import io.getstream.chat.android.compose.state.messages.list.CancelGiphy
import io.getstream.chat.android.compose.state.messages.list.DateSeparatorState
import io.getstream.chat.android.compose.state.messages.list.GiphyAction
import io.getstream.chat.android.compose.state.messages.list.MessageFocusRemoved
import io.getstream.chat.android.compose.state.messages.list.MessageFocused
import io.getstream.chat.android.compose.state.messages.list.MessageItemGroupPosition
import io.getstream.chat.android.compose.state.messages.list.MessageItemState
import io.getstream.chat.android.compose.state.messages.list.MessageListItemState
import io.getstream.chat.android.compose.state.messages.list.SendGiphy
import io.getstream.chat.android.compose.state.messages.list.ShuffleGiphy
import io.getstream.chat.android.compose.state.messages.list.SystemMessageState
import io.getstream.chat.android.compose.state.messages.list.ThreadSeparatorState
import io.getstream.chat.android.compose.ui.util.isError
import io.getstream.chat.android.compose.ui.util.isSystem
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadState
import io.getstream.chat.android.offline.experimental.extensions.asReferenced
import io.getstream.chat.android.offline.extensions.cancelMessage
import io.getstream.chat.android.offline.extensions.loadOlderMessages
import io.getstream.chat.android.offline.model.ConnectionState
import io.getstream.chat.android.offline.thread.ThreadController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * ViewModel responsible for handling all the business logic & state for the list of messages.
 *
 * @param chatClient Used to connect to the API.
 * @param chatDomain Used to connect to the API and fetch the domain status.
 * @param channelId The ID of the channel to load the messages for.
 * @param clipboardHandler Used to copy data from message actions to the clipboard.
 * @param messageLimit The limit of messages being fetched with each page od data.
 * @param enforceUniqueReactions Enables or disables unique message reactions per user.
 * @param showDateSeparators Enables or disables date separator items in the list.
 * @param showSystemMessages Enables or disables system messages in the list.
 * @param dateSeparatorThresholdMillis The threshold in millis used to generate date separator items, if enabled.
 */
public class MessageListViewModel(
    public val chatClient: ChatClient,
    public val chatDomain: ChatDomain,
    private val channelId: String,
    private val clipboardHandler: ClipboardHandler,
    private val messageLimit: Int = DEFAULT_MESSAGE_LIMIT,
    private val enforceUniqueReactions: Boolean = true,
    private val showDateSeparators: Boolean = true,
    private val showSystemMessages: Boolean = true,
    private val dateSeparatorThresholdMillis: Long = TimeUnit.HOURS.toMillis(DATE_SEPARATOR_DEFAULT_HOUR_THRESHOLD),
) : ViewModel() {

    /**
     * State handler for the UI, which holds all the information the UI needs to render messages.
     *
     * It chooses between [threadMessagesState] and [messagesState] based on if we're in a thread or not.
     */
    public val currentMessagesState: MessagesState
        get() = if (isInThread) threadMessagesState else messagesState

    /**
     * State of the screen, for [MessageMode.Normal].
     */
    private var messagesState: MessagesState by mutableStateOf(MessagesState())

    /**
     * State of the screen, for [MessageMode.MessageThread].
     */
    private var threadMessagesState: MessagesState by mutableStateOf(MessagesState())

    /**
     * Holds the current [MessageMode] that's used for the messages list. [MessageMode.Normal] by default.
     */
    public var messageMode: MessageMode by mutableStateOf(MessageMode.Normal)
        private set

    /**
     * The information for the current [Channel].
     */
    public var channel: Channel by mutableStateOf(Channel())
        private set

    /**
     * The list of typing users.
     */
    public var typingUsers: List<User> by mutableStateOf(emptyList())
        private set

    /**
     * Set of currently active [MessageAction]s. Used to show things like edit, reply, delete and
     * similar actions.
     */
    public var messageActions: Set<MessageAction> by mutableStateOf(mutableSetOf())
        private set

    /**
     * Gives us information if we're currently in the [Thread] message mode.
     */
    public val isInThread: Boolean
        get() = messageMode is MessageMode.MessageThread

    /**
     * Gives us information if we have selected a message.
     */
    public val isShowingOverlay: Boolean
        get() = messagesState.selectedMessageState != null || threadMessagesState.selectedMessageState != null

    /**
     * Gives us information about the online state of the device.
     */
    public val connectionState: StateFlow<ConnectionState> by chatDomain::connectionState

    /**
     * Gives us information about the online state of the device.
     */
    public val isOnline: Flow<Boolean>
        get() = chatDomain.connectionState.map { it == ConnectionState.CONNECTED }

    /**
     * Gives us information about the logged in user state.
     */
    public val user: StateFlow<User?>
        get() = chatDomain.user

    /**
     * [Job] that's used to keep the thread data loading operations. We cancel it when the user goes
     * out of the thread state.
     */
    private var threadJob: Job? = null

    /**
     * Represents the last loaded message in the list, for comparison when determining the
     * [NewMessageState] for the screen.
     */
    private var lastLoadedMessage: Message? = null

    /**
     * Represents the latest message we've seen in the channel.
     */
    private var lastSeenChannelMessage: Message? by mutableStateOf(null)

    /**
     * Represents the latest message we've seen in the active thread.
     */
    private var lastSeenThreadMessage: Message? by mutableStateOf(null)

    /**
     * Instance of [ChatLogger] to log exceptional and warning cases in behavior.
     */
    private val logger = ChatLogger.get("MessageListViewModel")

    /**
     * Sets up the core data loading operations - such as observing the current channel and loading
     * messages and other pieces of information.
     */
    init {
        viewModelScope.launch {
            val result =
                chatDomain.watchChannel(channelId, messageLimit)
                    .await()

            if (result.isSuccess) {
                val controller = result.data()

                observeConversation(controller)
                observeTypingUsers(controller)
            } else {
                result.error().cause?.printStackTrace()
                showEmptyState()
            }
        }
    }

    /**
     * Starts observing the current conversation using the [controller]. We observe the
     * 'loadingOlderMessages', 'messagesState', 'user' and 'endOfOlderMessages' states from our
     * controller, as well as build the `newMessageState` using [getNewMessageState] and combine it
     * into a [MessagesState] that holds all the information required for the screen.
     *
     * @param controller The controller for the channel with the current [channelId].
     */
    private fun observeConversation(controller: ChannelController) {
        viewModelScope.launch {
            controller.messagesState
                .combine(user) { state, user ->
                    when (state) {
                        is ChannelController.MessagesState.NoQueryActive,
                        is ChannelController.MessagesState.Loading,
                        -> messagesState.copy(isLoading = true)
                        is ChannelController.MessagesState.OfflineNoResults -> messagesState.copy(
                            isLoading = false,
                            messageItems = emptyList()
                        )
                        is ChannelController.MessagesState.Result -> {
                            messagesState.copy(
                                isLoading = false,
                                messageItems = groupMessages(
                                    messages = filterMessagesToShow(state.messages),
                                    isInThread = false
                                ),
                                isLoadingMore = false,
                                endOfMessages = controller.endOfOlderMessages.value,
                                currentUser = user
                            )
                        }
                    }
                }.collect { newState ->
                    val newLastMessage =
                        (newState.messageItems.firstOrNull { it is MessageItemState } as? MessageItemState)?.message

                    val hasNewMessage = lastLoadedMessage != null &&
                        messagesState.messageItems.isNotEmpty() &&
                        newLastMessage?.id != lastLoadedMessage?.id

                    messagesState = if (hasNewMessage) {
                        val newMessageState = getNewMessageState(newLastMessage)

                        newState.copy(
                            newMessageState = newMessageState,
                            unreadCount = getUnreadMessageCount(newMessageState)
                        )
                    } else {
                        newState
                    }
                    lastLoadedMessage = newLastMessage
                    controller.toChannel().let { channel ->
                        ChatClient.dismissChannelNotifications(channelType = channel.type, channelId = channel.id)
                        setCurrentChannel(channel)
                    }
                }
        }
    }

    /**
     * Starts observing the list of typing users.
     */
    private fun observeTypingUsers(controller: ChannelController) {
        viewModelScope.launch {
            controller.typing.collect {
                typingUsers = it.users
            }
        }
    }

    /**
     * Sets the current channel, used to show info in the UI.
     */
    private fun setCurrentChannel(channel: Channel) {
        this.channel = channel
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
            val isNotDeletedByOtherUser = !(it.deletedAt != null && it.user.id != currentUser?.id)
            val isSystemMessage = it.isSystem() || it.isError()

            isNotDeletedByOtherUser || (isSystemMessage && showSystemMessages)
        }
    }

    /**
     * Builds the [NewMessageState] for the UI, whenever the message state changes. This is used to
     * allow us to show the user a floating button giving them the option to scroll to the bottom
     * when needed.
     *
     * @param lastMessage Last message in the list, used for comparison.
     */
    private fun getNewMessageState(lastMessage: Message?): NewMessageState? {
        val lastLoadedMessage = lastLoadedMessage
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
     * Counts how many messages the user hasn't read already. This is based on what the last message they've seen is,
     * and the current message state.
     *
     * @param newMessageState The state that tells us if there are new messages in the list.
     * @return [Int] which describes how many messages come after the last message we've seen in the list.
     */
    private fun getUnreadMessageCount(newMessageState: NewMessageState? = currentMessagesState.newMessageState): Int {
        if (newMessageState == null || newMessageState == MyOwn) return 0

        val messageItems = currentMessagesState.messageItems
        val lastSeenMessagePosition =
            getLastSeenMessagePosition(if (isInThread) lastSeenThreadMessage else lastSeenChannelMessage)
        var unreadCount = 0

        for (i in 0..lastSeenMessagePosition) {
            val messageItem = messageItems[i]

            if (messageItem is MessageItemState && !messageItem.isMine && messageItem.message.deletedAt == null) {
                unreadCount++
            }
        }

        return unreadCount
    }

    /**
     * Gets the list position of the last seen message in the list.
     *
     * @param lastSeenMessage - The last message we saw in the list.
     * @return [Int] list position of the last message we've seen.
     */
    private fun getLastSeenMessagePosition(lastSeenMessage: Message?): Int {
        if (lastSeenMessage == null) return 0

        return currentMessagesState.messageItems.indexOfFirst {
            it is MessageItemState && it.message.id == lastSeenMessage.id
        }
    }

    /**
     * Attempts to update the last seen message in the channel or thread. We only update the last seen message the first
     * time the data loads and whenever we see a message that's newer than the current last seen message.
     *
     * @param message The message that is currently seen by the user.
     */
    public fun updateLastSeenMessage(message: Message) {
        val lastSeenMessage = if (isInThread) lastSeenThreadMessage else lastSeenChannelMessage

        if (lastSeenMessage == null) {
            updateLastSeenMessageState(message)
            return
        }

        if (message.id == lastSeenMessage.id) return

        val lastSeenMessageDate = lastSeenMessage.createdAt ?: Date()
        val currentMessageDate = message.createdAt ?: Date()

        if (currentMessageDate < lastSeenMessageDate) {
            return
        }
        updateLastSeenMessageState(message)
    }

    /**
     * Updates the state of the last seen message. Based on if we're [isInThread] or not, it updates corresponding state.
     *
     * @param currentMessage The current message the user sees.
     */
    private fun updateLastSeenMessageState(currentMessage: Message) {
        if (isInThread) {
            lastSeenThreadMessage = currentMessage

            threadMessagesState = threadMessagesState.copy(unreadCount = getUnreadMessageCount())
        } else {
            lastSeenChannelMessage = currentMessage

            messagesState = messagesState.copy(unreadCount = getUnreadMessageCount())
        }

        if (ToggleService.isEnabled(ToggleService.TOGGLE_KEY_OFFLINE)) {
            val (channelType, id) = channelId.cidToTypeAndId()
            chatClient.markRead(channelType, id).enqueue()
        } else {
            chatDomain.markRead(channelId).enqueue()
        }
    }

    /**
     * If there's an error, we just set the current state to be empty - 'isLoading' as false and
     * 'messages' as an empty list.
     */
    private fun showEmptyState() {
        messagesState = messagesState.copy(isLoading = false, messageItems = emptyList())
    }

    /**
     * Triggered when the user loads more data by reaching the end of the current messages.
     */
    public fun loadMore() {
        val messageMode = messageMode

        if (messageMode is MessageMode.MessageThread) {
            threadLoadMore(messageMode)
        } else {
            messagesState = messagesState.copy(isLoadingMore = true)
            chatClient.loadOlderMessages(channelId, messageLimit).enqueue()
        }
    }

    /**
     * Load older messages for the specified thread [MessageMode.MessageThread.parentMessage].
     *
     * @param threadMode Current thread mode.
     */
    private fun threadLoadMore(threadMode: MessageMode.MessageThread) {
        threadMessagesState = threadMessagesState.copy(isLoadingMore = true)
        if (ToggleService.isEnabled(ToggleService.TOGGLE_KEY_OFFLINE).not()) {
            chatDomain.threadLoadMore(channelId, threadMode.parentMessage.id, messageLimit)
                .enqueue()
        } else {
            if (threadMode.threadState != null) {
                chatClient.getRepliesMore(
                    messageId = threadMode.parentMessage.id,
                    firstId = threadMode.threadState?.oldestInThread?.value?.id ?: threadMode.parentMessage.id,
                    limit = DEFAULT_MESSAGE_LIMIT,
                ).enqueue()
            } else {
                threadMessagesState = threadMessagesState.copy(isLoadingMore = false)
                logger.logW("Thread state must be not null for offline plugin thread load more!")
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
            changeSelectMessageState(SelectedMessageOptionsState(message))
        }
    }

    /**
     * Triggered when the user taps on and selects message reactions.
     *
     * @param message The message that contains the reactions.
     */
    public fun selectReactions(message: Message?) {
        if (message != null) {
            changeSelectMessageState(SelectedMessageReactionsState(message))
        }
    }

    /**
     * Triggered when the user taps the show more reactions button.
     *
     * @param message The selected message.
     */
    public fun selectExtendedReactions(message: Message?) {
        if (message != null) {
            changeSelectMessageState(SelectedMessageReactionsPickerState(message))
        }
    }

    /**
     * Changes the state of [threadMessagesState] or [messagesState] depending
     * on the thread mode.
     *
     * @param selectedMessageState The selected message state.
     * */
    private fun changeSelectMessageState(selectedMessageState: SelectedMessageState) {
        if (isInThread) {
            threadMessagesState = threadMessagesState.copy(selectedMessageState = selectedMessageState)
        } else {
            messagesState = messagesState.copy(selectedMessageState = selectedMessageState)
        }
    }

    /**
     * Triggered when the user taps on a message that has a thread active.
     *
     * @param message The selected message with a thread.
     */
    public fun openMessageThread(message: Message) {
        loadThread(message)
    }

    /**
     * Used to dismiss a specific message action, such as delete, reply, edit or something similar.
     *
     * @param messageAction The action to dismiss.
     */
    public fun dismissMessageAction(messageAction: MessageAction) {
        this.messageActions = messageActions - messageAction
    }

    /**
     * Dismisses all message actions, when we cancel them in the rest of the UI.
     */
    public fun dismissAllMessageActions() {
        this.messageActions = emptySet()
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
            is ThreadReply -> {
                messageActions = messageActions + Reply(messageAction.message)
                loadThread(messageAction.message)
            }
            is Delete, is Flag -> {
                messageActions = messageActions + messageAction
            }
            is Copy -> copyMessage(messageAction.message)
            is MuteUser -> updateUserMute(messageAction.message.user)
            is React -> reactToMessage(messageAction.reaction, messageAction.message)
            is Pin -> updateMessagePin(messageAction.message)
            else -> {
                // no op, custom user action
            }
        }
    }

    /**
     * Loads the thread data.
     *
     * @param parentMessage The message with the thread we want to observe.
     */
    private fun loadThread(parentMessage: Message) {
        if (ToggleService.isEnabled(ToggleService.TOGGLE_KEY_OFFLINE)) {
            loadThreadWithOfflinePlugin(parentMessage)
        } else {
            loadThreadWithChatDomain(parentMessage)
        }
    }

    /**
     * Changes the current [messageMode] to be [Thread] and loads thread data using ChatDomain approach.
     * The data is loaded by fetching the [ThreadController] first, based on the [parentMessage], after which we observe
     * specific data from the thread.
     *
     * @param parentMessage The message with the thread we want to observe.
     */
    private fun loadThreadWithChatDomain(parentMessage: Message) {
        messageMode = MessageMode.MessageThread(parentMessage)
        chatDomain.getThread(channelId, parentMessage.id).enqueue { result ->
            if (result.isSuccess) {
                val controller = result.data()
                observeThreadMessages(controller.threadId, controller.messages, controller.endOfOlderMessages)
            } else {
                messageMode = MessageMode.Normal
            }
        }
    }

    /**
     *  Changes the current [messageMode] to be [Thread] with [ThreadState] and Loads thread data using ChatClient
     *  directly. The data is observed by using [ThreadState].
     *
     * @param parentMessage The message with the thread we want to observe.
     */
    private fun loadThreadWithOfflinePlugin(parentMessage: Message) {
        val threadState = chatClient.asReferenced().getReplies(parentMessage.id).asState(viewModelScope)
        messageMode = MessageMode.MessageThread(parentMessage, threadState)
        observeThreadMessages(threadState.parentId, threadState.messages, threadState.endOfOlderMessages)
    }

    /**
     * Observes the currently active thread data, based on our [ThreadController]. In process, this
     * creates a [threadJob] that we can cancel once we leave the thread.
     *
     * The data consists of the 'loadingOlderMessages', 'messages' and 'endOfOlderMessages' states,
     * that are combined into one [MessagesState].
     *
     * @param threadId The message id with the thread we want to observe.
     * @param messages State flow source of thread messages.
     * @param endOfOlderMessages State flow of flag which show if we reached the end of available messages.
     */
    private fun observeThreadMessages(
        threadId: String,
        messages: StateFlow<List<Message>>,
        endOfOlderMessages: StateFlow<Boolean>,
    ) {
        threadJob = viewModelScope.launch {
            messages.combine(user) { messages, user -> messages to user }
                .combine(endOfOlderMessages) { (messages, user), endOfOlderMessages ->
                    threadMessagesState.copy(
                        isLoading = false,
                        messageItems = groupMessages(
                            messages = filterMessagesToShow(messages),
                            isInThread = true
                        ),
                        isLoadingMore = false,
                        endOfMessages = endOfOlderMessages,
                        currentUser = user,
                        parentMessageId = threadId
                    )
                }.collect { newState -> threadMessagesState = newState }
        }
    }

    /**
     * Takes in the available messages for a [Channel] and groups them based on the sender ID. We put the message in a
     * group, where the positions can be [MessageItemGroupPosition.Top], [MessageItemGroupPosition.Middle],
     * [MessageItemGroupPosition.Bottom] or [MessageItemGroupPosition.None] if the message isn't in a group.
     *
     * @param messages The messages we need to group.
     * @param isInThread If we are in inside a thread.
     * @return A list of [MessageListItemState]s, each containing a position.
     */
    private fun groupMessages(messages: List<Message>, isInThread: Boolean): List<MessageListItemState> {
        val parentMessageId = (messageMode as? MessageMode.MessageThread)?.parentMessage?.id
        val currentUser = user.value
        val groupedMessages = mutableListOf<MessageListItemState>()

        messages.forEachIndexed { index, message ->
            val user = message.user
            val previousMessage = messages.getOrNull(index - 1)
            val nextMessage = messages.getOrNull(index + 1)

            val previousUser = previousMessage?.user
            val nextUser = nextMessage?.user

            val willSeparateNextMessage =
                nextMessage?.let { shouldAddDateSeparator(message, it) } ?: false

            val position = when {
                previousUser != user && nextUser == user && !willSeparateNextMessage -> MessageItemGroupPosition.Top
                previousUser == user && nextUser == user && !willSeparateNextMessage -> MessageItemGroupPosition.Middle
                previousUser == user && nextUser != user -> MessageItemGroupPosition.Bottom
                else -> MessageItemGroupPosition.None
            }

            if (shouldAddDateSeparator(previousMessage, message)) {
                groupedMessages.add(DateSeparatorState(message.getCreatedAtOrThrow()))
            }

            if (message.isSystem() || message.isError()) {
                groupedMessages.add(SystemMessageState(message = message))
            } else {
                groupedMessages.add(
                    MessageItemState(
                        message = message,
                        currentUser = currentUser,
                        groupPosition = position,
                        parentMessageId = parentMessageId,
                        isMine = user.id == currentUser?.id,
                        isInThread = isInThread
                    )
                )
            }

            if (index == 0 && isInThread) {
                groupedMessages.add(ThreadSeparatorState(message.replyCount))
            }
        }

        return groupedMessages.reversed()
    }

    /**
     * Decides if we need to add a date separator or not.
     *
     * If the user disables them, we don't add any separators, otherwise we check if there are previous messages or if
     * the time difference between two messages is higher than the threshold.
     *
     * @param previousMessage The previous message.
     * @param message The current message.
     * @return If we should add a date separator to the list.
     */
    private fun shouldAddDateSeparator(previousMessage: Message?, message: Message): Boolean {
        return if (!showDateSeparators) {
            false
        } else if (previousMessage == null) {
            true
        } else {
            val timeDifference = message.getCreatedAtOrThrow().time - previousMessage.getCreatedAtOrThrow().time

            return timeDifference > dateSeparatorThresholdMillis
        }
    }

    /**
     * Removes the delete actions from our [messageActions], as well as the overlay, before deleting
     * the selected message.
     *
     * @param message Message to delete.
     */
    @JvmOverloads
    public fun deleteMessage(message: Message, hard: Boolean = false) {
        messageActions = messageActions - messageActions.filterIsInstance<Delete>()
        removeOverlay()

        chatDomain.deleteMessage(message, hard).enqueue()
    }

    /**
     * Removes the flag actions from our [messageActions], as well as the overlay, before flagging
     * the selected message.
     *
     * @param message Message to delete.
     */
    public fun flagMessage(message: Message) {
        messageActions = messageActions - messageActions.filterIsInstance<Flag>()
        removeOverlay()

        chatClient.flagMessage(message.id).enqueue()
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
     * Mutes or unmutes the user that sent a particular message.
     *
     * @param user The user to mute or unmute.
     */
    private fun updateUserMute(user: User) {
        val isUserMuted = chatDomain.muted.value.any { it.target.id == user.id }

        if (isUserMuted) {
            chatClient.unmuteUser(user.id)
        } else {
            chatClient.muteUser(user.id)
        }.enqueue()
    }

    /**
     * Triggered when the user chooses the [React] action for the currently selected message. If the
     * message already has that reaction, from the current user, we remove it. Otherwise we add a new
     * reaction.
     *
     * @param reaction The reaction to add or remove.
     * @param message The currently selected message.
     */
    private fun reactToMessage(reaction: Reaction, message: Message) {
        if (message.ownReactions.any { it.messageId == reaction.messageId && it.type == reaction.type }) {
            chatDomain.deleteReaction(channelId, reaction).enqueue()
        } else {
            chatDomain.sendReaction(channelId, reaction, enforceUniqueReactions).enqueue()
        }
    }

    /**
     * Pins or unpins the message from the current channel based on its state.
     *
     * @param message The message to update the pin state of.
     */
    private fun updateMessagePin(message: Message) {
        val updateCall = if (message.pinned) {
            chatClient.unpinMessage(message)
        } else {
            chatClient.pinMessage(message = message, expirationDate = null)
        }

        updateCall.enqueue()
    }

    /**
     * Leaves the thread we're in and resets the state of the [messageMode] and both of the [MessagesState]s.
     *
     * It also cancels the [threadJob] to clean up resources.
     */
    public fun leaveThread() {
        messageMode = MessageMode.Normal
        messagesState = messagesState.copy(selectedMessageState = null)
        threadMessagesState = MessagesState()
        lastSeenThreadMessage = null
        threadJob?.cancel()
    }

    /**
     * Resets the [MessagesState]s, to remove the message overlay, by setting 'selectedMessage' to null.
     */
    public fun removeOverlay() {
        threadMessagesState = threadMessagesState.copy(selectedMessageState = null)
        messagesState = messagesState.copy(selectedMessageState = null)
    }

    /**
     * Clears the [NewMessageState] from our UI state, after the user taps on the "Scroll to bottom"
     * or "New Message" actions in the list or simply scrolls to the bottom.
     */
    public fun clearNewMessageState() {
        threadMessagesState = threadMessagesState.copy(newMessageState = null, unreadCount = 0)
        messagesState = messagesState.copy(newMessageState = null, unreadCount = 0)
    }

    /**
     * Sets the focused message to be the message with the given ID, after which it removes it from
     * focus with a delay.
     *
     * @param messageId The ID of the message.
     */
    public fun focusMessage(messageId: String) {
        val messages = currentMessagesState.messageItems.map {
            if (it is MessageItemState && it.message.id == messageId) {
                it.copy(focusState = MessageFocused)
            } else {
                it
            }
        }

        viewModelScope.launch {
            updateMessages(messages)
            delay(2000)
            removeMessageFocus(messageId)
        }
    }

    /**
     * Removes the focus from the message with the given ID.
     *
     * @param messageId The ID of the message.
     */
    private fun removeMessageFocus(messageId: String) {
        val messages = currentMessagesState.messageItems.map {
            if (it is MessageItemState && it.message.id == messageId) {
                it.copy(focusState = MessageFocusRemoved)
            } else {
                it
            }
        }

        updateMessages(messages)
    }

    /**
     * Updates the current message state with new messages.
     *
     * @param messages The list of new message items.
     */
    private fun updateMessages(messages: List<MessageListItemState>) {
        if (isInThread) {
            this.threadMessagesState = threadMessagesState.copy(messageItems = messages)
        } else {
            this.messagesState = messagesState.copy(messageItems = messages)
        }
    }

    /**
     * Returns a message with the given ID from the [currentMessagesState].
     *
     * @param messageId The ID of the selected message.
     * @return The [Message] with the ID, if it exists.
     */
    public fun getMessageWithId(messageId: String): Message? {
        val messageItem =
            currentMessagesState.messageItems.firstOrNull { it is MessageItemState && it.message.id == messageId }

        return (messageItem as? MessageItemState)?.message
    }

    /**
     * Executes one of the actions for the given ephemeral giphy message.
     *
     * @param action The action to be executed.
     */
    public fun performGiphyAction(action: GiphyAction) {
        val message = action.message
        when (action) {
            is SendGiphy -> chatDomain.sendGiphy(message)
            is ShuffleGiphy -> chatDomain.shuffleGiphy(message)
            is CancelGiphy -> chatClient.cancelMessage(message)
        }.exhaustive.enqueue()
    }

    internal companion object {
        /**
         * The default threshold for showing date separators. If the message difference in hours is equal to this number, then
         * we show a separator, if it's enabled in the list.
         */
        internal const val DATE_SEPARATOR_DEFAULT_HOUR_THRESHOLD: Long = 4

        /**
         * The default limit for messages count in requests.
         */
        internal const val DEFAULT_MESSAGE_LIMIT: Int = 30
    }
}
