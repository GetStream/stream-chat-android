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

package io.getstream.chat.android.compose.viewmodel.messages

import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.messagelist.DateSeparatorHandler
import io.getstream.chat.android.common.messagelist.MessageListController
import io.getstream.chat.android.common.messagelist.MessagePositionHandler
import io.getstream.chat.android.common.state.DeletedMessageVisibility
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.MessageFooterVisibility
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.compose.handlers.ClipboardHandler
import io.getstream.chat.android.compose.state.messages.MessagesState
import io.getstream.chat.android.compose.state.messages.NewMessageState
import io.getstream.chat.android.compose.state.messages.list.CancelGiphy
import io.getstream.chat.android.compose.state.messages.list.GiphyAction
import io.getstream.chat.android.compose.state.messages.list.SendGiphy
import io.getstream.chat.android.compose.state.messages.list.ShuffleGiphy
import io.getstream.chat.android.compose.util.extensions.asState
import io.getstream.chat.android.compose.util.extensions.toComposeState
import io.getstream.chat.android.offline.plugin.state.channel.thread.ThreadState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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
 * @param showSystemMessages Enables or disables system messages in the list.
 * @param deletedMessageVisibility The behavior of deleted messages in the list and if they're visible or not.
 * @param messageFooterVisibility Determines when the message footer should be visible.
 * @param dateSeparatorHandler Determines the visibility of date separators inside the message list.
 * @param threadDateSeparatorHandler Determines the visibility of date separators inside the thread.
 * @param messageListController Controller used to relay the logic and fetch the state.
 */
@Suppress("TooManyFunctions", "LargeClass", "TooManyFunctions")
public class MessageListViewModel(
    public val chatClient: ChatClient,
    private val channelId: String,
    private val clipboardHandler: ClipboardHandler,
    private val messageLimit: Int = DefaultMessageLimit,
    private val enforceUniqueReactions: Boolean = true,
    private val showSystemMessages: Boolean = true,
    private val deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
    private val messageFooterVisibility: MessageFooterVisibility = MessageFooterVisibility.WithTimeDifference(),
    private val dateSeparatorHandler: DateSeparatorHandler = DateSeparatorHandler.getDefaultDateSeparator(),
    private val threadDateSeparatorHandler: DateSeparatorHandler = DateSeparatorHandler.getDefaultThreadDateSeparator(),
    private val messageListController: MessageListController = MessageListController(
        cid = channelId,
        chatClient = chatClient,
        deletedMessageVisibility = deletedMessageVisibility,
        showSystemMessages = showSystemMessages,
        messageFooterVisibility = messageFooterVisibility,
        enforceUniqueReactions = enforceUniqueReactions,
        clipboardHandler = { clipboardHandler.copyMessage(it) },
        dateSeparatorHandler = dateSeparatorHandler,
        threadDateSeparatorHandler = threadDateSeparatorHandler
    ),
) : ViewModel() {

    /**
     * State handler for the UI, which holds all the information the UI needs to render messages.
     *
     * It chooses between [threadMessagesState] and [messagesState] based on if we're in a thread or not.
     */
    public val currentMessagesState: MessagesState
        get() = if (isInThread) threadMessagesState else messagesState

    /**
     * Offset of a focused message needed to show it in the center of the screen.
     */
    private val focusedMessagesOffsetState: MutableStateFlow<Int?> = MutableStateFlow(0)

    /**
     * State of the screen, for [MessageMode.Normal].
     */
    private val messagesState: MessagesState by messageListController.messageListState
        .map { it.toComposeState(focusedMessagesOffsetState) }
        .asState(viewModelScope, MessagesState())

    /**
     * State of the screen, for [MessageMode.MessageThread].
     */
    private val threadMessagesState: MessagesState by messageListController.threadListState
        .map { it.toComposeState(focusedMessagesOffsetState) }
        .asState(viewModelScope, MessagesState())

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
     * Set of currently active [MessageAction]s. Used to show things like edit, reply, delete and similar actions.
     */
    public val messageActions: Set<MessageAction> by messageListController.messageActions.asState(viewModelScope)

    /**
     * Gives us information if we're currently in the [Thread] message mode.
     */
    public val isInThread: Boolean
        get() = messageListController.isInThread

    /**
     * Gives us information if we have selected a message.
     */
    public val isShowingOverlay: Boolean
        get() = currentMessagesState.selectedMessageState != null

    /**
     * Gives us information about the online state of the device.
     */
    public val connectionState: StateFlow<ConnectionState> = messageListController.connectionState

    /**
     * Gives us information about the online state of the device.
     */
    public val isOnline: Flow<Boolean> = messageListController.connectionState.map { it == ConnectionState.CONNECTED }

    /**
     * Gives us information about the logged in user state.
     */
    public val user: StateFlow<User?> = messageListController.user

    /**
     * Determines whether we should show system messages or not.
     */
    public val showSystemMessagesState: Boolean by messageListController.showSystemMessagesState.asState(viewModelScope)

    /**
     * Regulates the message footer visibility.
     */
    public val messageFooterVisibilityState: MessageFooterVisibility by messageListController
        .messageFooterVisibilityState.asState(viewModelScope)

    /**
     * Regulates the visibility of deleted messages.
     */
    public val deletedMessageVisibilityState: DeletedMessageVisibility by messageListController
        .deletedMessageVisibilityState.asState(viewModelScope)

    /**
     * Attempts to update the last seen message in the channel or thread. We only update the last seen message the first
     * time the data loads and whenever we see a message that's newer than the current last seen message.
     *
     * @param message The message that is currently seen by the user.
     */
    public fun updateLastSeenMessage(message: Message) {
        messageListController.updateLastSeenMessage(message)
    }

    /**
     * Loads newer messages of a channel following the currently newest loaded message. In case of threads this will
     * do nothing.
     *
     * @param messageId The id of the newest [Message] inside the messages list.
     * @param messageLimit The limit of messages to be loaded in the page.
     */
    public fun loadNewerMessages(messageId: String, messageLimit: Int = DefaultMessageLimit) {
        messageListController.loadNewerMessages(messageId, messageLimit)
    }

    /**
     * Loads older messages of a channel following the currently oldest loaded message. Also will load older messages
     * of a thread.
     *
     * @param messageLimit The limit of messages to be loaded in the page.
     */
    public fun loadOlderMessages(messageLimit: Int = DefaultMessageLimit) {
        messageListController.loadOlderMessages(messageLimit)
    }

    /**
     * Triggered when the user long taps on and selects a message.
     *
     * @param message The selected message.
     */
    public fun selectMessage(message: Message?) {
        messageListController.selectMessage(message)
    }

    /**
     * Triggered when the user taps on and selects message reactions.
     *
     * @param message The message that contains the reactions.
     */
    public fun selectReactions(message: Message?) {
        messageListController.selectReactions(message)
    }

    /**
     * Triggered when the user taps the show more reactions button.
     *
     * @param message The selected message.
     */
    public fun selectExtendedReactions(message: Message?) {
        messageListController.selectReactions(message)
    }

    /**
     *  Changes the current [messageMode] to be [Thread] with [ThreadState] and Loads thread data using ChatClient
     *  directly. The data is observed by using [ThreadState].
     *
     * @param message The selected message with a thread.
     */
    public fun openMessageThread(message: Message) {
        messageListController.enterThreadMode(message)
    }

    /**
     * Used to dismiss a specific message action, such as delete, reply, edit or something similar.
     *
     * @param messageAction The action to dismiss.
     */
    public fun dismissMessageAction(messageAction: MessageAction) {
        messageListController.dismissMessageAction(messageAction)
    }

    /**
     * Dismisses all message actions, when we cancel them in the rest of the UI.
     */
    public fun dismissAllMessageActions() {
        messageListController.dismissAllMessageActions()
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
        messageListController.performMessageAction(messageAction)
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
        messageListController.flagMessage(message)
    }

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
     * Leaves the thread we're in and resets the state of the [messageMode] and both of the [MessagesState]s.
     */
    public fun leaveThread() {
        messageListController.enterNormalMode()
    }

    /**
     * Resets the [MessagesState]s, to remove the message overlay, by setting 'selectedMessage' to null.
     */
    public fun removeOverlay() {
        messageListController.removeOverlay()
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

    /**
     * Sets a handler which determines the position of a message inside a group.
     *
     * @param messagePositionHandler The handler to use.
     */
    public fun setMessagePositionHandler(messagePositionHandler: MessagePositionHandler) {
        messageListController.setMessagePositionHandler(messagePositionHandler)
    }

    /**
     * Sets the date separator handler which determines when to add date separators.
     * By default, a date separator will be added if the difference between two messages' dates is greater than 4h.
     *
     * @param dateSeparatorHandler The handler to use. If null, the messages list won't contain date separators.
     */
    public fun setDateSeparatorHandler(dateSeparatorHandler: DateSeparatorHandler?) {
        messageListController.setDateSeparatorHandler(dateSeparatorHandler)
    }

    /**
     * Sets thread date separator handler which determines when to add date separators inside the thread.
     *
     * @param threadDateSeparatorHandler The handler to use. If null, the thread messages list won't contain date
     * separators.
     */
    public fun setThreadDateSeparatorHandler(threadDateSeparatorHandler: DateSeparatorHandler?) {
        messageListController.setThreadDateSeparatorHandler(threadDateSeparatorHandler)
    }

    /**
     * Sets the value used to determine if message footer content is shown.
     * @see MessageFooterVisibility
     *
     * @param messageFooterVisibility Changes the visibility of message footers.
     */
    public fun setMessageFooterVisibility(messageFooterVisibility: MessageFooterVisibility) {
        messageListController.setMessageFooterVisibility(messageFooterVisibility)
    }

    /**
     * Sets the value used to filter deleted messages.
     * @see DeletedMessageVisibility
     *
     * @param deletedMessageVisibility Changes the visibility of deleted messages.
     */
    public fun setDeletedMessageVisibility(deletedMessageVisibility: DeletedMessageVisibility) {
        messageListController.setDeletedMessageVisibility(deletedMessageVisibility)
    }

    /**
     * Sets whether the system messages should be visible.
     *
     * @param showSystemMessages Whether system messages should be visible or not.
     */
    public fun setAreSystemMessagesVisible(showSystemMessages: Boolean) {
        messageListController.setAreSystemMessagesVisible(showSystemMessages)
    }

    internal companion object {
        /**
         * The default limit for messages count in requests.
         */
        internal const val DefaultMessageLimit: Int = 30
    }
}
