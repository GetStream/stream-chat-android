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

package io.getstream.chat.android.ui.viewmodel.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.errors.extractCause
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Flag
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.setMessageForReply
import io.getstream.chat.android.ui.common.feature.messages.list.DateSeparatorHandler
import io.getstream.chat.android.ui.common.feature.messages.list.MessageListController
import io.getstream.chat.android.ui.common.feature.messages.list.MessagePositionHandler
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageFocused
import io.getstream.chat.android.ui.common.state.messages.list.MessageFooterVisibility
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.model.MessageListItemWrapper
import io.getstream.chat.android.ui.utils.extensions.toMessageListItemWrapper
import io.getstream.log.TaggedLogger
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.enqueue
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import io.getstream.chat.android.state.utils.Event as EventWrapper

/**
 * View model class for [MessageListView].
 * Responsible for updating the list of messages.
 * Can be bound to the view using [MessageListViewModel.bindView] function.
 *
 * @param messageListController Controller used to relay the logic and fetch the state.
 */
@OptIn(FlowPreview::class)
@Suppress("TooManyFunctions")
public class MessageListViewModel(
    internal val messageListController: MessageListController,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    /**
     * The logger used to print to errors, warnings, information and other things to log.
     */
    private val logger: TaggedLogger by taggedLogger("Chat:MessageListVM")

    /**
     * Holds information about the current channel and is actively updated.
     */
    public val channelState: StateFlow<ChannelState?> = messageListController.channelState

    /**
     *  The current channel used to load the message list data.
     */
    public val channel: LiveData<Channel> = messageListController.channel.asLiveData()

    /**
     * Holds information about the abilities the current user is able to exercise in the given channel.
     *
     * e.g. send messages, delete messages, etc...
     * For a full list @see [ChannelCapabilities].
     */
    public val ownCapabilities: LiveData<Set<String>> = messageListController.ownCapabilities.asLiveData()

    /**
     * Determines whether we should show system messages or not.
     */
    public val showSystemMessagesState: LiveData<Boolean> = messageListController.showSystemMessagesState.asLiveData()

    /**
     * Regulates the message footer visibility.
     */
    public val messageFooterVisibilityState: LiveData<MessageFooterVisibility> = messageListController
        .messageFooterVisibilityState.asLiveData()

    /**
     * Regulates the visibility of deleted messages.
     */
    public val deletedMessageVisibility: LiveData<DeletedMessageVisibility> =
        messageListController.deletedMessageVisibilityState.asLiveData()

    /**
     * Represents the current state of the message list that is a product of multiple sources.
     */
    private val stateMerger = MediatorLiveData<State>()

    /**
     * Current message list state.
     * @see State
     */
    public val state: LiveData<State> = stateMerger

    /**
     * Whether the user is viewing a thread.
     * @see MessageMode
     */
    public val mode: LiveData<MessageMode> = messageListController.mode.asLiveData()

    /**
     * Emits true if we should are loading more older messages.
     */
    public val loadMoreLiveData: LiveData<Boolean> = messageListController.messageListState
        .map { it.isLoadingOlderMessages }.asLiveData()

    /**
     * The target message that the list should scroll to. Used when scrolling to a pinned message, a message opened from
     * a push notification or similar.
     */
    public val targetMessage: LiveData<Message> = messageListController.listState.map {
        (it.messageItems.firstOrNull { it is MessageItemState && it.focusState == MessageFocused } as? MessageItemState)
            ?.message
            ?: Message()
    }.distinctUntilChanged { old, new -> old.id == new.id }.asLiveData()

    /**
     * Emits error events.
     */
    public val errorEvents: LiveData<EventWrapper<MessageListController.ErrorEvent>> = messageListController.errorEvents
        .filterNotNull().map { EventWrapper(it) }.asLiveData()

    /**
     * The currently logged in user.
     */
    public val user: LiveData<User?> = messageListController.user.asLiveData()

    /**
     * Unread count of the channel or thread depending on [MessageMode].
     */
    public val unreadCount: LiveData<Int> = messageListController.unreadCount.asLiveData()

    /**
     * Emits the status of searching situation. True when inside a search and false otherwise.
     */
    public val insideSearch: LiveData<Boolean> = messageListController.isInsideSearch.asLiveData()

    public val shouldRequestMessagesAtBottom: LiveData<Boolean> = combine(
        messageListController.isInsideSearch,
        messageListController.mode,
    ) { data ->
        val isInsideSearch: Boolean = (data[0] as Boolean)
        val isInThread: Boolean = (data[1] as MessageMode) is MessageMode.MessageThread
        (isInsideSearch || (isInThread && messageListController.threadLoadOrderOlderToNewer))
    }.asLiveData()

    /**
     * Emits the current unread label state.
     */
    public val unreadLabel: LiveData<MessageListController.UnreadLabel> =
        messageListController.unreadLabelState.filterNotNull().asLiveData()

    /**
     * Initializes the full message list state conversion and collection.
     */
    init {
        viewModelScope.launch {
            val listState = messageListController.listState.combine(messageListController.mode) { listState, mode ->
                Pair(listState, mode)
            }
                // TODO - Think of a better solution once we have more capacity to do larger refactors
                // Due to the way we combine upstream flows in MessageListController, we get unnecessary multiple
                // emissions at the start of a collecting a new state. These multiple emissions happen within a
                // millisecond of one another and can force the RecyclerView adapter to skip actions when loading a
                // thread.
                .debounce(5)
                .onStart { State.Loading }
                .map {
                    if (it.first.isLoading) {
                        State.Loading
                    } else {
                        State.Result(
                            it.first.toMessageListItemWrapper(
                                isInThread = it.second is MessageMode.MessageThread,
                                prevNewMessageState = stateMerger.value?.asResultOrNull()
                                    ?.messageListItem?.newMessageState,
                            ),
                        )
                    }
                }.asLiveData()
            stateMerger.addSource(listState) { stateMerger.value = it }
        }
    }

    /**
     * Handles an [Event] coming from the View layer.
     */
    @Suppress("LongMethod", "ComplexMethod")
    public fun onEvent(event: Event) {
        logger.v { "[onEvent] event: $event" }
        when (event) {
            is Event.EndRegionReached -> onEndRegionReached()
            is Event.BottomEndRegionReached -> onBottomEndRegionReached(event.messageId)
            is Event.LastMessageRead -> messageListController.markLastMessageRead()
            is Event.ThreadModeEntered -> onThreadModeEntered(event.parentMessage)
            is Event.OpenThread -> onOpenThread(event.message)
            is Event.BackButtonPressed -> onBackButtonPressed()
            is Event.MarkAsUnreadMessage -> messageListController.markUnread(event.message)
            is Event.DeleteMessage -> messageListController.deleteMessage(event.message, event.hard)
            is Event.PinMessage -> messageListController.pinMessage(event.message)
            is Event.UnpinMessage -> messageListController.unpinMessage(event.message)
            is Event.GiphyActionSelected -> onGiphyActionSelected(event)
            is Event.RetryMessage -> messageListController.resendMessage(event.message)
            is Event.MessageReaction -> onMessageReaction(event.message, event.reactionType)
            is Event.MuteUser -> messageListController.muteUser(event.user)
            is Event.UnmuteUser -> messageListController.unmuteUser(event.user)
            is Event.UnbanUser -> messageListController.unbanUser(event.user.id)
            is Event.ShadowBanUser -> messageListController.shadowBanUser(event.user.id)
            is Event.RemoveShadowBanFromUser -> messageListController.removeShadowBanFromUser(event.user.id)
            is Event.FlagUser -> messageListController.flagUser(event.userId, event.reason, event.customData)
            is Event.UnflagUser -> messageListController.unflagUser(event.userId)
            is Event.RemoveAttachment -> messageListController.removeAttachment(event.messageId, event.attachment)
            is Event.FlagMessage -> messageListController.flagMessage(
                event.message,
                event.reason,
                event.customData,
            ) { result ->
                event.resultHandler(result)
            }
            is Event.BanUser -> messageListController.banUser(
                userId = event.user.id,
                reason = event.reason,
                timeout = event.timeout,
            )
            is Event.ReplyMessage -> chatClient.setMessageForReply(event.cid, event.repliedMessage).enqueue(
                onError = { streamError ->
                    logger.e {
                        "Could not reply message: ${streamError.message}. " +
                            "Cause: ${streamError.extractCause()}"
                    }
                },
            )
            is Event.DownloadAttachment -> event.downloadAttachmentCall().enqueue(
                onError = { streamError ->
                    logger.e {
                        "Attachment download error: ${streamError.message}. " +
                            "Cause: ${streamError.extractCause()}"
                    }
                },
            )
            is Event.ShowMessage -> messageListController.scrollToMessage(
                messageId = event.messageId,
                parentMessageId = event.parentMessageId,
            )
            is Event.ReplyAttachment -> messageListController.loadMessageById(event.repliedMessageId) { result ->
                when (result) {
                    is Result.Success -> onEvent(Event.ReplyMessage(event.cid, result.value))
                    is Result.Failure -> {
                        val error = result.value
                        logger.e {
                            "Could not load message to reply: ${error.message}. Cause: ${error.extractCause()}"
                        }
                    }
                }
            }
            is Event.HideUnreadLabel -> when (event.navigateToFirstUnreadMessage) {
                true -> messageListController.scrollToFirstUnreadMessage()
                false -> messageListController.disableUnreadLabelButton()
            }
            is Event.BlockUser -> messageListController.blockUser(event.userId)
            is Event.PollOptionUpdated -> messageListController.updatePollOption(
                message = event.message,
                poll = event.poll,
                option = event.option,
            )
            is Event.PollClosed -> messageListController.closePoll(event.poll)
        }
    }

    /**
     * Returns a message with the given ID from the messages list.
     *
     * @param messageId The ID of the selected message.
     * @return The [Message] with the ID, if it exists.
     */
    public fun getMessageById(messageId: String): Message? =
        messageListController.getMessageFromListStateById(messageId)

    /**
     * When the user clicks the scroll to bottom button we need to take the user to the bottom of the newest
     * messages. If the messages are not loaded we need to load them first and then scroll to the bottom of the
     * list.
     *
     * @param messageLimit The limit of messages to load when loading newest messages.
     * @param scrollToBottom The handler that notifies when the data has been loaded to scroll to the bottom.
     */
    public fun scrollToBottom(messageLimit: Int = messageListController.messageLimit, scrollToBottom: () -> Unit) {
        messageListController.scrollToBottom(messageLimit, scrollToBottom)
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
     * @see setDateSeparatorHandler
     *
     * @param threadDateSeparatorHandler The handler to use. If null, the thread message list won't contain date
     * separators.
     */
    public fun setThreadDateSeparatorHandler(threadDateSeparatorHandler: DateSeparatorHandler?) {
        messageListController.setThreadDateSeparatorHandler(threadDateSeparatorHandler)
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
     * Handles the send, shuffle and cancel Giphy actions.
     *
     * @param event The type of action the user has selected.
     */
    private fun onGiphyActionSelected(event: Event.GiphyActionSelected) {
        messageListController.performGiphyAction(event.action)
    }

    /**
     * Loads more messages if we have reached the oldest message currently loaded.
     */
    private fun onEndRegionReached() {
        messageListController.loadOlderMessages()
    }

    /**
     * Loads more messages if we have reached the newest messages currently loaded and we are handling search.
     *
     * @param baseMessageId The id of the currently newest loaded [Message].
     * @param messageLimit The limit of messages to be loaded in the next page.
     */
    private fun onBottomEndRegionReached(
        baseMessageId: String?,
        messageLimit: Int = messageListController.messageLimit,
    ) {
        logger.i { "[onBottomEndRegionReached] baseMessageId: $baseMessageId, messageLimit: $messageLimit" }
        if (baseMessageId != null) {
            messageListController.loadNewerMessages(baseMessageId, messageLimit)
        } else {
            logger.e { "[onBottomEndRegionReached] there's no base message to request more message at bottom of limit" }
        }
    }

    /**
     * Evaluates whether a navigation event should occur or if we should switch from thread mode back to normal mode.
     */
    private fun onBackButtonPressed() {
        mode.value?.run {
            when {
                this is MessageMode.Normal || messageListController.isStartedForThread -> {
                    stateMerger.postValue(State.NavigateUp)
                }
                this is MessageMode.MessageThread -> {
                    onNormalModeEntered()
                }
            }
        }
    }

    /**
     * Handles an event to move to thread mode.
     *
     * @param parentMessage The message with the thread we want to observe.
     */
    private fun onThreadModeEntered(parentMessage: Message) {
        viewModelScope.launch {
            messageListController.enterThreadMode(parentMessage)
        }
    }

    /**
     * Handles an event to open a thread.
     *
     * @param message The message to open the thread for.
     */
    private fun onOpenThread(message: Message) {
        viewModelScope.launch {
            messageListController.openRelatedThread(message)
        }
    }

    /**
     * Handles reacting to messages while taking into account if unique reactions are enforced.
     *
     * @param message The message the user is reacting to.
     * @param reactionType The exact reaction type.
     */
    private fun onMessageReaction(message: Message, reactionType: String) {
        val reaction = Reaction(
            messageId = message.id,
            type = reactionType,
            score = 1,
        )
        messageListController.reactToMessage(reaction, message)
    }

    /**
     * Called when upon initialization or when exiting thread mode.
     */
    private fun onNormalModeEntered() {
        messageListController.enterNormalMode()
    }

    /**
     * Sets the value used to filter deleted messages.
     * @see DeletedMessageVisibility
     *
     * @param deletedMessageVisibility Determines the visibility of deleted messages.
     */
    public fun setDeletedMessageVisibility(deletedMessageVisibility: DeletedMessageVisibility) {
        messageListController.setDeletedMessageVisibility(deletedMessageVisibility)
    }

    /**
     * Sets the value used to determine if message footer content is shown.
     * @see MessageFooterVisibility
     *
     * @param messageFooterVisibility Determines the visibility of message footers.
     */
    public fun setMessageFooterVisibility(messageFooterVisibility: MessageFooterVisibility) {
        messageListController.setMessageFooterVisibility(messageFooterVisibility)
    }

    /**
     * Sets whether the system messages should be visible.
     *
     * @param showSystemMessages Whether system messages should be visible or not.
     */
    public fun setAreSystemMessagesVisible(showSystemMessages: Boolean) {
        messageListController.setSystemMessageVisibility(showSystemMessages)
    }

    /**
     * Clears the [MessageListController] coroutine scope.
     */
    override fun onCleared() {
        messageListController.onCleared()
        super.onCleared()
    }

    /**
     * The current state of the message list.
     */
    public sealed class State {

        /**
         * Signifies that the message list is loading.
         */
        public data object Loading : State()

        /**
         * Signifies that the messages have successfully loaded.
         *
         * @param messageListItem Contains the requested messages along with additional information.
         */
        public data class Result(val messageListItem: MessageListItemWrapper) : State()

        /**
         * Signals that the View should navigate back.
         */
        public data object NavigateUp : State()
    }

    private fun State.asResultOrNull(): State.Result? = this as? State.Result

    /**
     * Represents events coming from the View class.
     */
    public sealed class Event {

        /**
         * When the back button is pressed.
         */
        public data object BackButtonPressed : Event()

        /**
         * When the oldest loaded message in the list has been reached.
         */
        public data object EndRegionReached : Event()

        /**
         * When the newest loaded message in the list has been reached and there's still newer messages to be loaded.
         */
        public data class BottomEndRegionReached(val messageId: String?) : Event()

        /**
         * When the newest message in the channel has been read.
         */
        public data object LastMessageRead : Event()

        /**
         * When the users enters thread mode.
         *
         * @param parentMessage The original message the thread was spun off from.
         */
        public data class ThreadModeEntered(val parentMessage: Message) : Event()

        /**
         * When the user
         */
        public data class OpenThread(val message: Message) : Event()

        /**
         * When the user deletes a message.
         *
         * @param message The message to be deleted.
         * @param hard Determines whether the message will be soft or hard deleted.
         *
         * Soft delete - Deletes the message on the client side but it remains available
         * via server-side export functions.
         * Hard delete - message is deleted everywhere.
         */
        public data class DeleteMessage(val message: Message, val hard: Boolean = false) : Event()

        /**
         * When the user flags a message.
         *
         * @param message The message to be flagged.
         * @param resultHandler Lambda function that handles the result of the operation.
         * e.g. if the message was successfully flagged or not.
         */
        public data class FlagMessage(
            val message: Message,
            val reason: String?,
            val customData: Map<String, String>,
            val resultHandler: ((Result<Flag>) -> Unit) = { },
        ) : Event()

        /**
         * When the user pins a message.
         *
         * @param message The message to be pinned.
         */
        public data class PinMessage(val message: Message) : Event()

        /**
         * When the user unpins a message.
         *
         * @param message The message to be unpinned.
         */
        public data class UnpinMessage(val message: Message) : Event()

        /**
         * When the user marks a message as unread.
         *
         * @param message The message to be marked as unread.
         */
        public data class MarkAsUnreadMessage(val message: Message) : Event()

        /**
         * When the user selects a Giphy message.
         * e.g. send, shuffle or cancel.
         *
         * @param action The Giphy action. e.g. send, shuffle or cancel.
         */
        public data class GiphyActionSelected(val action: GiphyAction) : Event()

        /**
         * Retry sending a message that has failed to send.
         *
         * @param message The message that will be re-sent.
         */
        public data class RetryMessage(val message: Message) : Event()

        /**
         * When the user leaves a reaction to a message.
         *
         * @param message The message the user is reacting to
         * @param reactionType The reaction type.
         */
        public data class MessageReaction(
            val message: Message,
            val reactionType: String,
        ) : Event()

        /**
         * When the user mutes a user.
         *
         * @param user The user to be muted.
         */
        public data class MuteUser(val user: User) : Event()

        /**
         * When the user unmutes a user.
         *
         * @param user The user to be unmuted.
         */
        public data class UnmuteUser(val user: User) : Event()

        /**
         * When the user bans another user.
         *
         * @param user The user to be blocked.
         * @param reason The reason for banning the user.
         * @param timeout The time until the user is automatically unbanned, expressed in minutes.
         * A null value represent an indefinite ban.
         */
        public data class BanUser(
            val user: User,
            val reason: String? = null,
            val timeout: Int? = null,
        ) : Event()

        /**
         * When the user unbans another user.
         *
         * @param user The user to be blocked.
         */
        public data class UnbanUser(val user: User) : Event()

        /**
         * When the user shadow bans another user.
         *
         * @param user The user to be blocked.
         * @param reason The reason for shadow banning the user.
         * @param timeout The time until the shadow ban is automatically removed
         * for the given user, expressed in minutes.
         * A null value represent an indefinite ban.
         */
        public data class ShadowBanUser(
            val user: User,
            val reason: String? = null,
            val timeout: Int? = null,
        ) : Event()

        /**
         * When the user removes the shadow ban from another user.
         *
         * @param user The user to be blocked.
         */
        public data class RemoveShadowBanFromUser(val user: User) : Event()

        /**
         * Event for flagging a user.
         *
         * @param userId The ID of the user to flag.
         * @param reason The reason for flagging the user.
         * @param customData Additional key-value data submitted with the request.
         */
        public data class FlagUser(
            val userId: String,
            val reason: String? = null,
            val customData: Map<String, String> = emptyMap(),
        ) : Event()

        /**
         * Event for flagging a user.
         *
         * @param userId The ID of the user to un-flag.
         */
        public data class UnflagUser(val userId: String) : Event()

        /**
         * When the user replies to a message.
         *
         * @param cid The full channel id, i.e. "messaging:123".
         * @param repliedMessage The message the user is replying to.
         */
        public data class ReplyMessage(val cid: String, val repliedMessage: Message) : Event()

        /**
         * When the user is replying to a single attachment.
         * Usually triggered when replying from gallery.
         *
         * @param cid The full channel id, i.e. "messaging:123".
         * @param repliedMessageId The message the user is replying to.
         */
        public data class ReplyAttachment(val cid: String, val repliedMessageId: String) : Event()

        /**
         * When the user downloads an attachment.
         *
         * @param downloadAttachmentCall A handler for downloading that returns a [Call]
         * with the option of asynchronous operation.
         */
        public data class DownloadAttachment(val downloadAttachmentCall: () -> Call<Unit>) : Event()

        /**
         * When we need to display a particular message to the user.
         * Usually triggered by clicking on pinned messages and replied messages.
         *
         * @param messageId The id of the message we need to navigate to.
         * @param parentMessageId The ID of the parent [Message] if the message we want to scroll to is in a thread.
         * If the message we want to scroll to is not in a thread, you can pass in a null value.
         */
        public data class ShowMessage(
            val messageId: String,
            val parentMessageId: String?,
        ) : Event()

        /**
         * When the user removes an attachment from a message that was previously sent.
         *
         * @param messageId The message from which an attachment will be deleted.
         * @param attachment The attachment to be deleted.
         */
        public data class RemoveAttachment(val messageId: String, val attachment: Attachment) : Event()

        /**
         * When the Unread Label Button should be hidden.
         *
         * @param navigateToFirstUnreadMessage If true, the user will be navigated to the first unread message.
         */
        public data class HideUnreadLabel(val navigateToFirstUnreadMessage: Boolean) : Event()

        /**
         * Block a user.
         *
         * @param userId the id of the user that is blocked.
         */
        public data class BlockUser(val userId: String) : Event()

        /**
         * When the user updates a poll option.
         *
         * @param message The message containing the poll.
         * @param poll The poll to be updated.
         * @param option The option to be updated.
         */
        public data class PollOptionUpdated(
            val message: Message,
            val poll: Poll,
            val option: Option,
        ) : Event()

        /**
         * When the user closes a poll.
         *
         * @param poll The poll to be closed.
         */
        public data class PollClosed(val poll: Poll) : Event()
    }
}
