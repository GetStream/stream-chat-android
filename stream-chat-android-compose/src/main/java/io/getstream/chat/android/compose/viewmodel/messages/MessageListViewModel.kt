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
import com.getstream.sdk.chat.utils.extensions.isModerationFailed
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.messagelist.MessageListController
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
import io.getstream.chat.android.compose.state.messages.NewMessageState
import io.getstream.chat.android.compose.state.messages.Other
import io.getstream.chat.android.compose.state.messages.ScrollToFocusedMessage
import io.getstream.chat.android.compose.state.messages.ScrollToNewestMessages
import io.getstream.chat.android.compose.state.messages.SelectedMessageFailedModerationState
import io.getstream.chat.android.compose.state.messages.SelectedMessageOptionsState
import io.getstream.chat.android.compose.state.messages.SelectedMessageReactionsPickerState
import io.getstream.chat.android.compose.state.messages.SelectedMessageReactionsState
import io.getstream.chat.android.compose.state.messages.SelectedMessageState
import io.getstream.chat.android.compose.state.messages.list.CancelGiphy
import io.getstream.chat.android.compose.state.messages.list.GiphyAction
import io.getstream.chat.android.compose.state.messages.list.MessageItemState
import io.getstream.chat.android.compose.state.messages.list.SendGiphy
import io.getstream.chat.android.compose.state.messages.list.ShuffleGiphy
import io.getstream.chat.android.compose.state.messages.toMessagesState
import io.getstream.chat.android.compose.ui.util.isError
import io.getstream.chat.android.compose.ui.util.isSystem
import io.getstream.chat.android.compose.util.extensions.asState
import io.getstream.chat.android.offline.plugin.state.channel.thread.ThreadState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import io.getstream.chat.android.common.messagelist.GiphyAction as GiphyActionCommon
import io.getstream.chat.android.common.messagelist.CancelGiphy as CancelGiphyCommon
import io.getstream.chat.android.common.messagelist.SendGiphy as SendGiphyCommon
import io.getstream.chat.android.common.messagelist.ShuffleGiphy as ShuffleGiphyCommon

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
    private val messageListController: MessageListController = MessageListController(
        cid = channelId,
        chatClient = chatClient,
        deletedMessageVisibility = deletedMessageVisibility,
        showSystemMessages = showSystemMessages,
        showDateSeparators = showDateSeparators,
        dateSeparatorThresholdMillis = dateSeparatorThresholdMillis,
        messageFooterVisibility = messageFooterVisibility
    ),
) : ViewModel() {

    /**
     * Holds information about the abilities the current user
     * is able to exercise in the given channel.
     *
     * e.g. send messages, delete messages, etc...
     * For a full list @see [io.getstream.chat.android.client.models.ChannelCapabilities].
     */
    private val ownCapabilities: StateFlow<Set<String>> = messageListController.ownCapabilities

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
    public val messageMode: MessageMode by messageListController.mode.asState(viewModelScope)

    /**
     * The information for the current [Channel].
     */
    public val channel: Channel by messageListController.channel.asState(viewModelScope)

    /**
     * The list of typing users.
     */
    public val typingUsers: List<User> by messageListController.typingUsers.asState(viewModelScope)

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
        get() = messageListController.isInThread

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
    public val isOnline: Flow<Boolean> = messageListController.isOnline

    /**
     * Gives us information about the logged in user state.
     */
    public val user: StateFlow<User?> = messageListController.user

    /**
     * [Job] that's used to keep the thread data loading operations. We cancel it when the user goes
     * out of the thread state.
     */
    private var threadJob: Job? = null

    /**
     * Sets up the core data loading operations - such as observing the current channel and loading
     * messages and other pieces of information.
     */
    init {
        observeMessageListState()
        observeThreadListState()
    }

    /**
     * Starts observing the current channel. We take the data exposed by the message list controller and map it to
     * compose list.
     */
    private fun observeMessageListState() {
        viewModelScope.launch {
            messageListController.messageListState.collect { state ->
                messagesState = state.toMessagesState()
            }
        }
    }

    private fun observeThreadListState() {
        viewModelScope.launch {
            messageListController.threadListState.collect { state ->
                threadMessagesState = state.toMessagesState()
            }
        }
    }

    /**
     * Attempts to update the last seen message in the channel or thread. We only update the last seen message the first
     * time the data loads and whenever we see a message that's newer than the current last seen message.
     *
     * @param message The message that is currently seen by the user.
     */
    public fun updateLastSeenMessage(message: Message) {
        val latestMessage: MessageItemState? = currentMessagesState.messageItems.firstOrNull { messageItem ->
            messageItem is MessageItemState
        } as? MessageItemState

        if (message.id == latestMessage?.message?.id) {
            messageListController.markLastMessageRead()
        }
    }

    /**
     * Triggered when the user loads more data by reaching the end of the current messages.
     */
    @Deprecated(
        message = "Deprecated after implementing bi directional pagination.",
        replaceWith = ReplaceWith(
            "loadOlderMessages(messageId: String)",
            "io.getstream.chat.android.compose.viewmodel.messages"
        ),
        level = DeprecationLevel.WARNING
    )
    public fun loadMore() {
        loadOlderMessages()
    }

    /**
     * Loads newer messages of a channel following the currently newest loaded message. In case of threads this will
     * do nothing.
     *
     * @param messageId The id of the newest [Message] inside the messages list.
     */
    public fun loadNewerMessages(messageId: String) {
        if (chatClient.clientState.isOffline || messagesState.startOfMessages) return

        if (messageMode is MessageMode.Normal) {
            messagesState = messagesState.copy(isLoadingMore = true, isLoadingMoreNewMessages = true)
            chatClient.loadNewerMessages(channelId, messageId, messageLimit).enqueue()
        }
    }

    /**
     * Loads older messages of a channel following the currently oldest loaded message. Also will load older messages
     * of a thread.
     */
    public fun loadOlderMessages() {
        if (chatClient.clientState.isOffline || messagesState.endOfMessages) return
        val messageMode = messageMode

        if (messageMode is MessageMode.MessageThread) {
            threadLoadMore(messageMode)
        } else {
            messagesState = messagesState.copy(isLoadingMore = true, isLoadingMoreOldMessages = true)
            chatClient.loadOlderMessages(channelId, messageLimit).enqueue()
        }
    }

    /**
     * Loads newer messages of a channel following the currently newest loaded message. In case of threads this will
     * do nothing.
     *
     * @param messageId The id of the most new [Message] inside the messages list.
     */
    public fun loadNewerMessages(messageId: String, messageLimit: Int = DefaultMessageLimit) {
        messageListController.loadNewerMessages(messageId, messageLimit)
    }

    /**
     * Loads older messages of a channel following the currently oldest loaded message. Also will load older messages
     * of a thread.
     */
    public fun loadOlderMessages(messageLimit: Int = DefaultMessageLimit) {
        messageListController.loadOlderMessages(messageLimit)
    }

    /**
     * Triggered when the user long taps on and selects a message.
     *
     * @param message The selected message.
     * TODO
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
     * TODO
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
     * TODO
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
        messageListController.enterThreadMode(parentMessage)
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

        messageListController.deleteMessage(message, hard)
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

        messageListController.flagMessage(message)
    }

    /**
     * Retries sending a message that has failed to send.
     *
     * @param message The message that will be re-sent.
     */
    private fun resendMessage(message: Message) = messageListController.resendMessage(message)

    /**
     * Copies the message content using the [ClipboardHandler] we provide. This can copy both
     * attachment and text messages.
     *
     * @param message Message with the content to copy.
     */
    private fun copyMessage(message: Message) {
        // TODO
        clipboardHandler.copyMessage(message)
    }

    /**
     * Mutes or unmutes the user that sent a particular message.
     *
     * @param user The user to mute or unmute.
     */
    private fun updateUserMute(user: User) = messageListController.updateUserMute(user)

    /**
     * Mutes the given user inside this channel.
     *
     * @param userId The ID of the user to be muted.
     * @param timeout The period of time for which the user will
     * be muted, expressed in minutes. A null value signifies that
     * the user will be muted for an indefinite time.
     */
    public fun muteUser(userId: String, timeout: Int? = null) {
        messageListController.muteUser(userId, timeout)
    }

    /**
     * Unmutes the given user inside this channel.
     *
     * @param userId The ID of the user to be unmuted.
     */
    public fun unmuteUser(userId: String) {
        messageListController.unmuteUser(userId)
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
        messageListController.banUser(userId = userId, reason = reason, timeout = timeout)
    }

    /**
     * Unbans the given user inside this channel.
     *
     * @param userId The ID of the user to be unbanned.
     */
    public fun unbanUser(userId: String) {
        messageListController.unbanUser(userId)
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
        messageListController.shadowBanUser(userId = userId, reason = reason, timeout = timeout)
    }

    /**
     * Removes the shaddow ban for the given user inside
     * this channel.
     *
     * @param userId The ID of the user for which the shadow
     * ban is removed.
     */
    public fun removeShadowBanFromUser(userId: String) {
        messageListController.removeShadowBanFromUser(userId)
    }

    /**
     * Triggered when the user chooses the [React] action for the currently selected message. If the
     * message already has that reaction, from the current user, we remove it. Otherwise we add a new
     * reaction.
     *
     * @param reaction The reaction to add or remove.
     * @param message The currently selected message.
     */
    private fun reactToMessage(reaction: Reaction, message: Message) =
        messageListController.reactToMessage(reaction, message, enforceUniqueReactions)

    /**
     * Pins or unpins the message from the current channel based on its state.
     *
     * @param message The message to update the pin state of.
     */
    private fun updateMessagePin(message: Message) = messageListController.updateMessagePin(message)

    /**
     * Leaves the thread we're in and resets the state of the [messageMode] and both of the [MessagesState]s.
     *
     * It also cancels the [threadJob] to clean up resources.
     */
    public fun leaveThread() {
        messageListController.enterNormalMode()
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
        messageListController.clearNewMessageState()
    }

    /**
     * Sets the focused message to be the message with the given ID, after which it removes it from
     * focus with a delay.
     *
     * @param messageId The ID of the message.
     */
    public fun focusMessage(messageId: String) {
        messageListController.focusMessage(messageId = messageId)
    }

    /**
     * Returns a message with the given ID from the [currentMessagesState].
     *
     * @param messageId The ID of the selected message.
     * @return The [Message] with the ID, if it exists.
     */
    public fun getMessageWithId(messageId: String): Message? {
        return messageListController.getMessageWithId(messageId)
    }

    /**
     * Executes one of the actions for the given ephemeral giphy message.
     *
     * @param action The action to be executed.
     */
    public fun performGiphyAction(action: GiphyAction) {
        val actionToPerform: GiphyActionCommon = when (action) {
            is CancelGiphy -> CancelGiphyCommon(action.message)
            is SendGiphy -> SendGiphyCommon(action.message)
            is ShuffleGiphy -> ShuffleGiphyCommon(action.message)
        }
        messageListController.performGiphyAction(actionToPerform)
    }

    /**
     * Scrolls to message if in list otherwise get the message from backend. Does not work for threads.
     *
     * @param message The message we wish to scroll to.
     */
    public fun scrollToSelectedMessage(message: Message) {
        messageListController.scrollToMessage(messageId = message.id)
    }

    /**
     * Requests that the list scrolls to the bottom to the newest messages. If the newest messages are loaded will set
     * scroll the list to the bottom. If they are not loaded will request the newest data and once loaded will scroll
     * to the bottom of the list.
     *
     * @param messageLimit The message count we wish to load from the API when loading new messages.
     * @param scrollToBottom Notifies the ui to scroll to the bottom if the newest messages are in the list or have been
     * loaded from the API.
     */
    public fun scrollToBottom(messageLimit: Int = DefaultMessageLimit, scrollToBottom: () -> Unit) {
        messageListController.scrollToBottom(messageLimit, scrollToBottom)
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
    }
}
