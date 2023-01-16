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

@file:Suppress("DEPRECATION_ERROR")

package io.getstream.chat.android.compose.viewmodel.messages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.getstream.sdk.chat.utils.extensions.getCreatedAtOrThrow
import com.getstream.sdk.chat.utils.extensions.isModerationFailed
import com.getstream.sdk.chat.utils.extensions.shouldShowMessageFooter
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.enqueue
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.Copy
import io.getstream.chat.android.common.state.Delete
import io.getstream.chat.android.common.state.DeletedMessageVisibility
import io.getstream.chat.android.common.state.Flag
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.MessageFooterVisibility
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.MuteUser
import io.getstream.chat.android.common.state.Pin
import io.getstream.chat.android.common.state.React
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.common.state.Resend
import io.getstream.chat.android.common.state.ThreadReply
import io.getstream.chat.android.compose.handlers.ClipboardHandler
import io.getstream.chat.android.compose.state.messages.MessagesState
import io.getstream.chat.android.compose.state.messages.MyOwn
import io.getstream.chat.android.compose.state.messages.NewMessageState
import io.getstream.chat.android.compose.state.messages.Other
import io.getstream.chat.android.compose.state.messages.SelectedMessageFailedModerationState
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
import io.getstream.chat.android.offline.extensions.cancelEphemeralMessage
import io.getstream.chat.android.offline.extensions.getRepliesAsState
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.offline.extensions.loadMessageById
import io.getstream.chat.android.offline.extensions.loadOlderMessages
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.thread.ThreadState
import io.getstream.logging.StreamLog
import io.getstream.logging.TaggedLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
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
import java.util.concurrent.TimeUnit

/**
 * ViewModel responsible for handling all the business logic & state for the list of messages.
 *
 * @param chatClient Used to connect to the API.
 * @param channelId The ID of the channel to load the messages for.
 * @param clipboardHandler Used to copy data from message actions to the clipboard.
 * @param messageLimit The limit of messages being fetched with each page od data.
 * @param enforceUniqueReactions Enables or disables unique message reactions per user.
 * @param showDateSeparators Enables or disables date separator items in the list.
 * @param showSystemMessages Enables or disables system messages in the list.
 * @param dateSeparatorThresholdMillis The threshold in millis used to generate date separator items, if enabled.
 * @param deletedMessageVisibility The behavior of deleted messages in the list and if they're visible or not.
 */
@Suppress("TooManyFunctions", "LargeClass", "TooManyFunctions")
public class MessageListViewModel(
    public val chatClient: ChatClient,
    private val channelId: String,
    private val clipboardHandler: ClipboardHandler,
    private val messageLimit: Int = DefaultMessageLimit,
    private val enforceUniqueReactions: Boolean = true,
    private val showDateSeparators: Boolean = true,
    private val showSystemMessages: Boolean = true,
    private val dateSeparatorThresholdMillis: Long = TimeUnit.HOURS.toMillis(DateSeparatorDefaultHourThreshold),
    private val deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
    private val messageFooterVisibility: MessageFooterVisibility = MessageFooterVisibility.WithTimeDifference(),
) : ViewModel() {

    /**
     * Holds information about the current state of the [Channel].
     */
    private val channelState: StateFlow<ChannelState?> = chatClient.watchChannelAsState(
        cid = channelId,
        messageLimit = messageLimit,
        coroutineScope = viewModelScope
    )

    /**
     * Holds information about the abilities the current user
     * is able to exercise in the given channel.
     *
     * e.g. send messages, delete messages, etc...
     * For a full list @see [io.getstream.chat.android.client.models.ChannelCapabilities].
     */
    private val ownCapabilities: StateFlow<Set<String>> =
        channelState.filterNotNull()
            .flatMapLatest { it.channelData }
            .map { it.ownCapabilities }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = setOf()
            )

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
    public val connectionState: StateFlow<ConnectionState> by chatClient.clientState::connectionState

    /**
     * Gives us information about the online state of the device.
     */
    public val isOnline: Flow<Boolean>
        get() = chatClient.clientState.connectionState.map { it == ConnectionState.CONNECTED }

    /**
     * Gives us information about the logged in user state.
     */
    public val user: StateFlow<User?>
        get() = chatClient.clientState.user

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
     * Represents the last loaded message in the thread, for comparison when determining the NewMessage
     */
    private var lastLoadedThreadMessage: Message? = null

    /**
     * Represents the latest message we've seen in the channel.
     */
    private var lastSeenChannelMessage: Message? by mutableStateOf(null)

    /**
     * Represents the latest message we've seen in the active thread.
     */
    private var lastSeenThreadMessage: Message? by mutableStateOf(null)

    /**
     * Represents the message we wish to scroll to.
     */
    private var scrollToMessage: Message? = null

    /**
     * Instance of [TaggedLogger] to log exceptional and warning cases in behavior.
     */
    private val logger = StreamLog.getLogger("Chat:MessageListViewModel")

    /**
     * Sets up the core data loading operations - such as observing the current channel and loading
     * messages and other pieces of information.
     */
    init {
        observeTypingUsers()
        observeMessages()
        observeChannel()
    }

    /**
     * Starts observing the messages in the current channel. We observe the 'messagesState', 'user' and
     * 'endOfOlderMessages' states, as well as build the `newMessageState` using [getNewMessageState]
     * and combine it into a [MessagesState] that holds all the information required for the screen.
     */
    private fun observeMessages() {
        viewModelScope.launch {
            channelState.filterNotNull().collectLatest { channelState ->
                combine(channelState.messagesState, user, channelState.reads) { state, user, reads ->
                    when (state) {
                        is io.getstream.chat.android.offline.plugin.state.channel.MessagesState.NoQueryActive,
                        is io.getstream.chat.android.offline.plugin.state.channel.MessagesState.Loading,
                        -> messagesState.copy(isLoading = true)
                        is io.getstream.chat.android.offline.plugin.state.channel.MessagesState.OfflineNoResults ->
                            messagesState.copy(
                                isLoading = false,
                                messageItems = emptyList(),
                            )
                        is io.getstream.chat.android.offline.plugin.state.channel.MessagesState.Result -> {
                            messagesState.copy(
                                isLoading = false,
                                messageItems = groupMessages(
                                    messages = filterMessagesToShow(state.messages),
                                    isInThread = false,
                                    reads = reads,
                                ),
                                isLoadingMore = false,
                                endOfMessages = channelState.endOfOlderMessages.value,
                                currentUser = user,
                            )
                        }
                    }
                }
                    .catch {
                        it.cause?.printStackTrace()
                        showEmptyState()
                    }
                    .collect { newState ->
                        val newLastMessage =
                            (newState.messageItems.firstOrNull { it is MessageItemState } as? MessageItemState)?.message

                        val hasNewMessage = lastLoadedMessage != null &&
                            messagesState.messageItems.isNotEmpty() &&
                            newLastMessage?.id != lastLoadedMessage?.id

                        messagesState = if (hasNewMessage) {
                            val newMessageState = getNewMessageState(newLastMessage, lastLoadedMessage)

                            newState.copy(
                                newMessageState = newMessageState,
                                unreadCount = getUnreadMessageCount(newMessageState)
                            )
                        } else {
                            newState
                        }

                        messagesState.messageItems.firstOrNull {
                            it is MessageItemState && it.message.id == scrollToMessage?.id
                        }?.let {
                            focusMessage((it as MessageItemState).message.id)
                        }

                        lastLoadedMessage = newLastMessage
                    }
            }
        }
    }

    /**
     * Starts observing the list of typing users.
     */
    private fun observeTypingUsers() {
        viewModelScope.launch {
            channelState.filterNotNull().flatMapLatest { it.typing }.collect {
                typingUsers = it.users
            }
        }
    }

    /**
     * Starts observing the current [Channel] created from [ChannelState]. It emits new data when either
     * channel data, member count or online member count updates.
     */
    private fun observeChannel() {
        viewModelScope.launch {
            channelState.filterNotNull().flatMapLatest { state ->
                combine(
                    state.channelData,
                    state.membersCount,
                    state.watcherCount,
                ) { _, _, _ ->
                    state.toChannel()
                }
            }.collect { channel ->
                chatClient.notifications.dismissChannelNotifications(
                    channelType = channel.type,
                    channelId = channel.id
                )
                setCurrentChannel(channel)
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

        if (message.id == lastSeenMessage.id) {
            return
        }

        val lastSeenMessageDate = lastSeenMessage.createdAt ?: Date()
        val currentMessageDate = message.createdAt ?: Date()

        if (currentMessageDate < lastSeenMessageDate) {
            return
        }
        updateLastSeenMessageState(message)
    }

    /**
     * Updates the state of the last seen message. Updates corresponding state based on [isInThread].
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

        val (channelType, id) = channelId.cidToTypeAndId()

        val latestMessage: MessageItemState? = currentMessagesState.messageItems.firstOrNull { messageItem ->
            messageItem is MessageItemState
        } as? MessageItemState

        if (currentMessage.id == latestMessage?.message?.id) {
            chatClient.markRead(channelType, id).enqueue()
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
        if (chatClient.clientState.isOffline) return
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
        val threadState = threadMode.threadState
        if (threadState != null && !threadState.endOfOlderMessages.value) {
            threadMessagesState = threadMessagesState.copy(isLoadingMore = true)

            chatClient.getRepliesMore(
                messageId = threadMode.parentMessage.id,
                firstId = threadMode.threadState?.oldestInThread?.value?.id ?: threadMode.parentMessage.id,
                limit = DefaultMessageLimit,
            ).enqueue()
        } else {
            threadMessagesState = threadMessagesState.copy(isLoadingMore = false)
            logger.w { "Thread state must be not null for offline plugin thread load more!" }
        }
    }

    /**
     * Loads the selected message we wish to scroll to when the message can't be found in the current list.
     *
     * @param message The selected message we wish to scroll to.
     */
    private fun loadMessage(message: Message) {
        val cid = channelState.value?.cid
        if (cid == null || chatClient.clientState.isOffline) return

        chatClient.loadMessageById(cid, message.id).enqueue()
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
     * Changes the state of [threadMessagesState] or [messagesState] depending
     * on the thread mode.
     *
     * @param selectedMessageState The selected message state.
     */
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
            is Resend -> resendMessage(messageAction.message)
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
     *  Changes the current [messageMode] to be [Thread] with [ThreadState] and Loads thread data using ChatClient
     *  directly. The data is observed by using [ThreadState].
     *
     * @param parentMessage The message with the thread we want to observe.
     */
    private fun loadThread(parentMessage: Message) {
        val threadState = chatClient.getRepliesAsState(parentMessage.id, DefaultMessageLimit)
        val channelState = channelState.value ?: return

        messageMode = MessageMode.MessageThread(parentMessage, threadState)
        observeThreadMessages(
            threadId = threadState.parentId,
            messages = threadState.messages,
            endOfOlderMessages = threadState.endOfOlderMessages,
            reads = channelState.reads
        )
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
        threadJob = viewModelScope.launch {
            combine(user, endOfOlderMessages, messages, reads) { user, endOfOlderMessages, messages, reads ->
                threadMessagesState.copy(
                    isLoading = false,
                    messageItems = groupMessages(
                        messages = filterMessagesToShow(messages),
                        isInThread = true,
                        reads = reads,
                    ),
                    isLoadingMore = false,
                    endOfMessages = endOfOlderMessages,
                    currentUser = user,
                    parentMessageId = threadId
                )
            }.collect { newState ->
                val newLastMessage =
                    (newState.messageItems.firstOrNull { it is MessageItemState } as? MessageItemState)?.message
                threadMessagesState = newState.copy(
                    newMessageState = getNewMessageState(newLastMessage, lastLoadedThreadMessage)
                )
                lastLoadedThreadMessage = newLastMessage
            }
        }
    }

    /**
     * Takes in the available messages for a [Channel] and groups them based on the sender ID. We put the message in a
     * group, where the positions can be [MessageItemGroupPosition.Top], [MessageItemGroupPosition.Middle],
     * [MessageItemGroupPosition.Bottom] or [MessageItemGroupPosition.None] if the message isn't in a group.
     *
     * @param messages The messages we need to group.
     * @param isInThread If we are in inside a thread.
     * @param reads The list of read states.
     *
     * @return A list of [MessageListItemState]s, each containing a position.
     */
    private fun groupMessages(
        messages: List<Message>,
        isInThread: Boolean,
        reads: List<ChannelUserRead>,
    ): List<MessageListItemState> {
        val parentMessageId = (messageMode as? MessageMode.MessageThread)?.parentMessage?.id
        val currentUser = user.value
        val groupedMessages = mutableListOf<MessageListItemState>()
        val lastRead = reads
            .filter { it.user.id != currentUser?.id }
            .mapNotNull { it.lastRead }
            .maxOrNull()

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

            val isLastMessageInGroup =
                position == MessageItemGroupPosition.Bottom || position == MessageItemGroupPosition.None

            val shouldShowFooter = messageFooterVisibility.shouldShowMessageFooter(
                message = message,
                isLastMessageInGroup = isLastMessageInGroup,
                nextMessage = nextMessage
            )

            if (shouldAddDateSeparator(previousMessage, message)) {
                groupedMessages.add(DateSeparatorState(message.getCreatedAtOrThrow()))
            }

            if (message.isSystem() || message.isError()) {
                groupedMessages.add(SystemMessageState(message = message))
            } else {
                val isMessageRead = message.createdAt
                    ?.let { lastRead != null && it <= lastRead }
                    ?: false

                groupedMessages.add(
                    MessageItemState(
                        message = message,
                        currentUser = currentUser,
                        groupPosition = position,
                        parentMessageId = parentMessageId,
                        isMine = user.id == currentUser?.id,
                        isInThread = isInThread,
                        isMessageRead = isMessageRead,
                        shouldShowFooter = shouldShowFooter,
                        deletedMessageVisibility = deletedMessageVisibility
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
    @Suppress("ConvertArgumentToSet")
    public fun deleteMessage(message: Message, hard: Boolean = false) {
        messageActions = messageActions - messageActions.filterIsInstance<Delete>()
        removeOverlay()

        chatClient.deleteMessage(message.id, hard).enqueue()
    }

    /**
     * Removes the flag actions from our [messageActions], as well as the overlay, before flagging
     * the selected message.
     *
     * @param message Message to delete.
     */
    @Suppress("ConvertArgumentToSet")
    public fun flagMessage(message: Message) {
        messageActions = messageActions - messageActions.filterIsInstance<Flag>()
        removeOverlay()

        chatClient.flagMessage(message.id).enqueue()
    }

    /**
     * Retries sending a message that has failed to send.
     *
     * @param message The message that will be re-sent.
     */
    private fun resendMessage(message: Message) {
        val (channelType, channelId) = message.cid.cidToTypeAndId()

        chatClient.sendMessage(channelType, channelId, message).enqueue()
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
        val isUserMuted = chatClient.globalState.muted.value.any { it.target.id == user.id }

        if (isUserMuted) {
            unmuteUser(user.id)
        } else {
            muteUser(user.id)
        }
    }

    /**
     * Mutes the given user inside this channel.
     *
     * @param userId The ID of the user to be muted.
     * @param timeout The period of time for which the user will
     * be muted, expressed in minutes. A null value signifies that
     * the user will be muted for an indefinite time.
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
     */
    public fun banUser(
        userId: String,
        reason: String? = null,
        timeout: Int? = null,
    ) {
        chatClient.channel(channelId).banUser(userId, reason, timeout)
            .enqueue(onError = { chatError ->
                val errorMessage = chatError.message ?: chatError.cause?.message ?: "Unable to ban the user"

                StreamLog.e("MessageListViewModel.banUser") { errorMessage }
            })
    }

    /**
     * Unbans the given user inside this channel.
     *
     * @param userId The ID of the user to be unbanned.
     */
    public fun unbanUser(userId: String) {
        chatClient.channel(channelId).unbanUser(userId)
            .enqueue(onError = { chatError ->
                val errorMessage = chatError.message ?: chatError.cause?.message ?: "Unable to unban the user"

                StreamLog.e("MessageListViewModel.unban") { errorMessage }
            })
    }

    /**
     * Shadow bans the given user inside this channel.
     *
     * @param userId The ID of the user to be shadow banned.
     * @param reason The reason for shadow banning the user.
     * @param timeout The period of time for which the user will
     * be shadow banned, expressed in minutes. A null value signifies that
     * the user will be shadow banned for an indefinite time.
     */
    public fun shadowBanUser(
        userId: String,
        reason: String? = null,
        timeout: Int? = null,
    ) {
        chatClient.channel(channelId).shadowBanUser(userId, reason, timeout)
            .enqueue(onError = { chatError ->
                val errorMessage = chatError.message ?: chatError.cause?.message ?: "Unable to shadow ban the user"

                StreamLog.e("MessageListViewModel.shadowBanUser") { errorMessage }
            })
    }

    /**
     * Removes the shadow ban for the given user inside
     * this channel.
     *
     * @param userId The ID of the user for which the shadow
     * ban is removed.
     */
    public fun removeShadowBanFromUser(userId: String) {
        chatClient.channel(channelId).removeShadowBan(userId)
            .enqueue(onError = { chatError ->
                val errorMessage =
                    chatError.message ?: chatError.cause?.message ?: "Unable to remove the user shadow ban"

                StreamLog.e("MessageListViewModel.removeShadowBanFromUser") { errorMessage }
            })
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
        val channelState = channelState.value ?: return

        if (message.ownReactions.any { it.messageId == reaction.messageId && it.type == reaction.type }) {
            chatClient.deleteReaction(
                messageId = message.id,
                reactionType = reaction.type,
                cid = channelState.cid
            ).enqueue()
        } else {
            chatClient.sendReaction(
                reaction = reaction,
                enforceUnique = enforceUniqueReactions,
                cid = channelState.cid
            ).enqueue()
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
            delay(RemoveMessageFocusDelay)
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

        if (scrollToMessage?.id == messageId) {
            scrollToMessage = null
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
            is SendGiphy -> chatClient.sendGiphy(message)
            is ShuffleGiphy -> chatClient.shuffleGiphy(message)
            is CancelGiphy -> chatClient.cancelEphemeralMessage(message)
        }.exhaustive.enqueue()
    }

    /**
     * Scrolls to message if in list otherwise get the message from backend.
     *
     * @param message The message we wish to scroll to.
     */
    public fun scrollToSelectedMessage(message: Message) {
        val isMessageInList = currentMessagesState.messageItems.firstOrNull {
            it is MessageItemState && it.message.id == message.id
        } != null

        if (isMessageInList) {
            focusMessage(message.id)
        } else {
            scrollToMessage = message
            loadMessage(message = message)
        }
    }

    internal companion object {
        /**
         * The default threshold for showing date separators. If the message difference in hours is equal to this
         * number, then we show a separator, if it's enabled in the list.
         */
        internal const val DateSeparatorDefaultHourThreshold: Long = 4

        /**
         * The default limit for messages count in requests.
         */
        internal const val DefaultMessageLimit: Int = 30

        /**
         * Time in millis, after which the focus is removed.
         */
        private const val RemoveMessageFocusDelay: Long = 2000
    }
}
