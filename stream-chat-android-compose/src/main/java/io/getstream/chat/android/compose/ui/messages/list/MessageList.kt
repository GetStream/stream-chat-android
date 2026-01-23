/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResultType
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.rememberMessageListState
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.ReactionSorting
import io.getstream.chat.android.models.ReactionSortingByFirstReactionAt
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageListItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListState
import io.getstream.chat.android.ui.common.state.messages.poll.PollSelectionType
import io.getstream.chat.android.ui.common.state.messages.poll.SelectedPoll

/**
 * Default MessageList component, that relies on [MessageListViewModel] to connect all the data
 * handling operations. It also delegates events to the ViewModel to handle, like long item
 * clicks and pagination.
 *
 * @param viewModel The ViewModel that stores all the data and business logic required to show a
 * list of messages. The user has to provide one in this case, as we require the channelId to start
 * the operations.
 * @param reactionSorting The sorting of the reactions. Default: [ReactionSortingByFirstReactionAt].
 * @param modifier Modifier for styling.
 * @param contentPadding Padding values to be applied to the message list surrounding the content inside.
 * @param messagesLazyListState State of the lazy list that represents the list of messages. Useful for controlling the
 * scroll state and focused message offset.
 * @param verticalArrangement Vertical arrangement of the regular message list.
 * Default: [Arrangement.Top].
 * @param threadsVerticalArrangement Vertical arrangement of the thread message list.
 * Default: [Arrangement.Bottom].
 * @param threadMessagesStart Thread messages start at the bottom or top of the screen.
 * Default: `null`.
 * @param onThreadClick Handler when the user taps on the message, while there's a thread going.
 * @param onLongItemClick Handler for when the user long taps on a message and selects it.
 * @param onReactionsClick Handler when the user taps on message reactions and selects them.
 * @param onMessagesPageStartReached Handler for pagination when the end of the oldest messages has been reached.
 * @param onLastVisibleMessageChanged Handler that notifies us when the user scrolls and the last visible message
 * changes.
 * @param onScrollToBottom Handler when the user reaches the bottom.
 * @param onGiphyActionClick Handler when the user clicks on a giphy action such as shuffle, send or cancel.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onUserAvatarClick Handler when users avatar is clicked.
 * @param onMessageLinkClick Handler for clicking on a link in the message.
 * @param onMediaGalleryPreviewResult Handler when the user selects an option in the Media Gallery Preview screen.
 * @param onMessagesPageEndReached Handler for pagination when the end of newest messages have been reached.
 * @param onScrollToBottomClicked Handler when the user requests to scroll to the bottom of the messages list.
 * @param onPauseAudioRecordingAttachments Handler for lifecycle events.
 * @param loadingContent Composable that represents the loading content, when we're loading the initial data.
 * @param emptyContent Composable that represents the empty content if there are no messages.
 * @param helperContent Composable that, by default, represents the helper content featuring scrolling behavior based
 * on the list state.
 * @param loadingMoreContent Composable that represents the loading more content, when we're loading the next page.
 * @param itemContent Composable that represents each item in a list. By default, we provide
 * the [MessageContainer] which sets up different message types. Users can override this to provide fully custom UI
 * and behavior.
 */
@Composable
public fun MessageList(
    viewModel: MessageListViewModel,
    reactionSorting: ReactionSorting = ReactionSortingByFirstReactionAt,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    messagesLazyListState: MessagesLazyListState =
        rememberMessageListState(parentMessageId = viewModel.currentMessagesState.value.parentMessageId),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    threadsVerticalArrangement: Arrangement.Vertical = Arrangement.Bottom,
    threadMessagesStart: ThreadMessagesStart? = null,
    onThreadClick: (Message) -> Unit = { viewModel.openMessageThread(it) },
    onLongItemClick: (Message) -> Unit = { viewModel.selectMessage(it) },
    onReactionsClick: (Message) -> Unit = { viewModel.selectReactions(it) },
    onMessagesPageStartReached: () -> Unit = { viewModel.loadOlderMessages() },
    onLastVisibleMessageChanged: (Message) -> Unit = { viewModel.updateLastSeenMessage(it) },
    onScrollToBottom: () -> Unit = { viewModel.clearNewMessageState() },
    onGiphyActionClick: (GiphyAction) -> Unit = { viewModel.performGiphyAction(it) },
    onPollUpdated: (Message, Poll) -> Unit = { message, poll ->
        val selectedPoll = viewModel.pollState.selectedPoll
        if (viewModel.isShowingPollOptionDetails &&
            selectedPoll != null && selectedPoll.poll.id == poll.id
        ) {
            viewModel.updatePollState(poll, message, selectedPoll.pollSelectionType)
        }
    },
    onCastVote: (Message, Poll, Option) -> Unit = { message, poll, option ->
        viewModel.castVote(
            message = message,
            poll = poll,
            option = option,
        )
    },
    onRemoveVote: (Message, Poll, Vote) -> Unit = { message, poll, vote ->
        viewModel.removeVote(
            message = message,
            poll = poll,
            vote = vote,
        )
    },
    selectPoll: (Message, Poll, PollSelectionType) -> Unit = { message, poll, selectionType ->
        viewModel.displayPollMoreOptions(selectedPoll = SelectedPoll(poll, message, selectionType))
    },
    onAddAnswer: (message: Message, poll: Poll, answer: String) -> Unit = { message, poll, answer ->
        viewModel.castAnswer(message, poll, answer)
    },
    onClosePoll: (String) -> Unit = { pollId ->
        viewModel.closePoll(pollId = pollId)
    },
    onAddPollOption: (poll: Poll, option: String) -> Unit = { poll, option ->
        viewModel.addPollOption(poll, option)
    },
    onQuotedMessageClick: (Message) -> Unit = { message ->
        viewModel.scrollToMessage(
            messageId = message.id,
            parentMessageId = message.parentId,
        )
    },
    onUserAvatarClick: ((User) -> Unit)? = null,
    onMessageLinkClick: ((Message, String) -> Unit)? = null,
    onUserMentionClick: (User) -> Unit = {},
    onReply: (Message) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {
        if (it?.resultType == MediaGalleryPreviewResultType.SHOW_IN_CHAT) {
            viewModel.scrollToMessage(
                messageId = it.messageId,
                parentMessageId = it.parentMessageId,
            )
        }
    },
    onMessagesPageEndReached: (String) -> Unit = { viewModel.onBottomEndRegionReached(it) },
    onScrollToBottomClicked: (() -> Unit) -> Unit = { viewModel.scrollToBottom(scrollToBottom = it) },
    onPauseAudioRecordingAttachments: () -> Unit = { viewModel.pauseAudioRecordingAttachments() },
    loadingContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.MessageListLoadingIndicator(modifier)
    },
    emptyContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.MessageListEmptyContent(modifier)
    },
    helperContent: @Composable BoxScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageListHelperContent(
                messageListState = viewModel.currentMessagesState.value,
                messagesLazyListState = messagesLazyListState,
                onScrollToBottomClick = onScrollToBottomClicked,
            )
        }
    },
    loadingMoreContent: @Composable LazyItemScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageListLoadingMoreItemContent()
        }
    },
    itemContent: @Composable LazyItemScope.(MessageListItemState) -> Unit = { messageListItem ->
        with(ChatTheme.componentFactory) {
            MessageListItemContainer(
                messageListItem = messageListItem,
                reactionSorting = reactionSorting,
                onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                onCastVote = onCastVote,
                onRemoveVote = onRemoveVote,
                selectPoll = selectPoll,
                onPollUpdated = onPollUpdated,
                onClosePoll = onClosePoll,
                onAddPollOption = onAddPollOption,
                onThreadClick = onThreadClick,
                onLongItemClick = onLongItemClick,
                onReactionsClick = onReactionsClick,
                onGiphyActionClick = onGiphyActionClick,
                onQuotedMessageClick = onQuotedMessageClick,
                onUserAvatarClick = onUserAvatarClick,
                onMessageLinkClick = onMessageLinkClick,
                onUserMentionClick = onUserMentionClick,
                onAddAnswer = onAddAnswer,
                onReply = onReply,
            )
        }
    },
) {
    MessageList(
        reactionSorting = reactionSorting,
        modifier = modifier,
        contentPadding = contentPadding,
        currentState = viewModel.currentMessagesState.value,
        messagesLazyListState = messagesLazyListState,
        onMessagesPageStartReached = onMessagesPageStartReached,
        verticalArrangement = verticalArrangement,
        threadsVerticalArrangement = threadsVerticalArrangement,
        threadMessagesStart = threadMessagesStart,
        onLastVisibleMessageChanged = onLastVisibleMessageChanged,
        onLongItemClick = onLongItemClick,
        onReactionsClick = onReactionsClick,
        onScrolledToBottom = onScrollToBottom,
        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
        itemContent = itemContent,
        helperContent = helperContent,
        loadingMoreContent = loadingMoreContent,
        loadingContent = loadingContent,
        emptyContent = emptyContent,
        onQuotedMessageClick = onQuotedMessageClick,
        onMessagesPageEndReached = onMessagesPageEndReached,
        onScrollToBottom = onScrollToBottomClicked,
        onPauseAudioRecordingAttachments = onPauseAudioRecordingAttachments,
        onMessageLinkClick = onMessageLinkClick,
        onClosePoll = onClosePoll,
        onAddAnswer = onAddAnswer,
        onReply = onReply,
    )
}

/**
 * The default message container item.
 *
 * @param messageListItemState The state of the message list item.
 * @param reactionSorting The sorting of the reactions.
 * @param onMediaGalleryPreviewResult Handler when the user receives a result from the Media Gallery Preview.
 * @param onThreadClick Handler when the user taps on a thread within a message item.
 * @param onLongItemClick Handler when the user long taps on an item.
 * @param onReactionsClick Handler when the user taps on message reactions.
 * @param onGiphyActionClick Handler when the user taps on Giphy message actions.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onCastVote Handler for casting a vote on an option.
 * @param onClosePoll Handler for closing a poll.
 * @param onPollUpdated Handler for updating a poll.
 * @param onRemoveVote Handler for removing a vote.
 * @param selectPoll Handler for selecting a poll.
 * @param onUserAvatarClick Handler when users avatar is clicked.
 * @param onLinkClick Handler for clicking on a link in the message.
 */
@Suppress("LongParameterList")
@Composable
internal fun LazyItemScope.DefaultMessageContainer(
    messageListItemState: MessageListItemState,
    reactionSorting: ReactionSorting,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onThreadClick: (Message) -> Unit,
    onLongItemClick: (Message) -> Unit,
    onReactionsClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit,
    onPollUpdated: (Message, Poll) -> Unit,
    onCastVote: (Message, Poll, Option) -> Unit,
    onRemoveVote: (Message, Poll, Vote) -> Unit,
    selectPoll: (Message, Poll, PollSelectionType) -> Unit,
    onAddAnswer: (message: Message, poll: Poll, answer: String) -> Unit,
    onClosePoll: (String) -> Unit = { _ -> },
    onAddPollOption: (poll: Poll, option: String) -> Unit,
    onQuotedMessageClick: (Message) -> Unit,
    onUserAvatarClick: ((User) -> Unit)? = null,
    onLinkClick: ((Message, String) -> Unit)? = null,
    onUserMentionClick: (User) -> Unit = {},
    onReply: (Message) -> Unit = {},
) {
    MessageContainer(
        messageListItemState = messageListItemState,
        reactionSorting = reactionSorting,
        onLongItemClick = onLongItemClick,
        onReactionsClick = onReactionsClick,
        onThreadClick = onThreadClick,
        onGiphyActionClick = onGiphyActionClick,
        onPollUpdated = onPollUpdated,
        onCastVote = onCastVote,
        onRemoveVote = onRemoveVote,
        selectPoll = selectPoll,
        onClosePoll = onClosePoll,
        onAddPollOption = onAddPollOption,
        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
        onQuotedMessageClick = onQuotedMessageClick,
        onUserAvatarClick = onUserAvatarClick,
        onLinkClick = onLinkClick,
        onUserMentionClick = onUserMentionClick,
        onAddAnswer = onAddAnswer,
        onReply = onReply,
    )
}

/**
 * The default message list loading indicator.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultMessageListLoadingIndicator(modifier: Modifier) {
    LoadingIndicator(modifier)
}

/**
 * The default empty placeholder that is displayed when there are no messages in the channel.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultMessageListEmptyContent(modifier: Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.stream_compose_message_list_empty_messages),
            style = ChatTheme.typography.body,
            color = ChatTheme.colors.textLowEmphasis,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Clean representation of the MessageList that is decoupled from ViewModels. This components allows
 * users to connect the UI to their own data providers, as it relies on pure state.
 *
 * @param currentState The state of the component, represented by [MessageListState].
 * @param verticalArrangement Vertical arrangement of the regular message list.
 * Default: [Arrangement.Top].
 * @param threadsVerticalArrangement Vertical arrangement of the thread message list.
 * Default: [Arrangement.Bottom].
 * @param threadMessagesStart Thread messages start at the bottom or top of the screen.
 * Default: `null`.
 * @param reactionSorting The sorting of the reactions.
 * @param modifier Modifier for styling.
 * @param contentPadding Padding values to be applied to the message list surrounding the content inside.
 * @param messagesLazyListState State of the lazy list that represents the list of messages. Useful for controlling the
 * scroll state and focused message offset.
 * @param onMessagesPageStartReached Handler for pagination.
 * @param onLastVisibleMessageChanged Handler that notifies us when the user scrolls and the last visible message
 * changes.
 * @param onScrolledToBottom Handler when the user scrolls to the bottom.
 * @param onThreadClick Handler for when the user taps on a message with an active thread.
 * @param onLongItemClick Handler for when the user long taps on an item.
 * @param onReactionsClick Handler when the user taps on message reactions and selects them.
 * @param onMediaGalleryPreviewResult Handler when the user selects an option in the Media Gallery Preview screen.
 * @param onGiphyActionClick Handler when the user clicks on a giphy action such as shuffle, send or cancel.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onMessageLinkClick Handler for clicking on a link in the message.
 * @param onMessagesPageEndReached Handler for pagination when the end of newest messages have been reached.
 * @param onScrollToBottom Handler when the user requests to scroll to the bottom of the messages list.
 * @param onPauseAudioRecordingAttachments Handler for lifecycle events.
 * @param background Composable that represents the background of the message list.
 * @param loadingContent Composable that represents the loading content, when we're loading the initial data.
 * @param emptyContent Composable that represents the empty content if there are no messages.
 * @param helperContent Composable that, by default, represents the helper content featuring scrolling behavior based
 * on the list state.
 * @param loadingMoreContent Composable that represents the loading more content, when we're loading the next page.
 * @param itemModifier Modifier for styling the item container.
 * @param itemContent Composable that represents each item in the list, that the user can override
 * for custom UI and behavior.
 */
@Composable
public fun MessageList(
    currentState: MessageListState,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    threadsVerticalArrangement: Arrangement.Vertical = Arrangement.Bottom,
    threadMessagesStart: ThreadMessagesStart? = null,
    reactionSorting: ReactionSorting,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    messagesLazyListState: MessagesLazyListState =
        rememberMessageListState(parentMessageId = currentState.parentMessageId),
    onMessagesPageStartReached: () -> Unit = {},
    onLastVisibleMessageChanged: (Message) -> Unit = {},
    onScrolledToBottom: () -> Unit = {},
    onPollUpdated: (Message, Poll) -> Unit = { _, _ -> },
    onCastVote: (Message, Poll, Option) -> Unit = { _, _, _ -> },
    onRemoveVote: (Message, Poll, Vote) -> Unit = { _, _, _ -> },
    selectPoll: (Message, Poll, PollSelectionType) -> Unit = { _, _, _ -> },
    onAddAnswer: (message: Message, poll: Poll, answer: String) -> Unit = { _, _, _ -> },
    onClosePoll: (String) -> Unit = { _ -> },
    onAddPollOption: (poll: Poll, option: String) -> Unit = { _, _ -> },
    onThreadClick: (Message) -> Unit = {},
    onLongItemClick: (Message) -> Unit = {},
    onReactionsClick: (Message) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onMessagesPageEndReached: (String) -> Unit = {},
    onScrollToBottom: (() -> Unit) -> Unit = {},
    onPauseAudioRecordingAttachments: () -> Unit = {},
    onUserAvatarClick: ((User) -> Unit)? = null,
    onMessageLinkClick: ((Message, String) -> Unit)? = null,
    onUserMentionClick: (User) -> Unit = { _ -> },
    onReply: (Message) -> Unit = {},
    background: @Composable () -> Unit = {
        ChatTheme.componentFactory.MessageListBackground()
    },
    loadingContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.MessageListLoadingIndicator(modifier)
    },
    emptyContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.MessageListEmptyContent(modifier)
    },
    helperContent: @Composable BoxScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageListHelperContent(
                messageListState = currentState,
                messagesLazyListState = messagesLazyListState,
                onScrollToBottomClick = onScrollToBottom,
            )
        }
    },
    loadingMoreContent: @Composable LazyItemScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageListLoadingMoreItemContent()
        }
    },
    itemModifier: @Composable LazyItemScope.(index: Int, item: MessageListItemState) -> Modifier = { _, _ ->
        with(ChatTheme.componentFactory) {
            messageListItemModifier()
        }
    },
    itemContent: @Composable LazyItemScope.(MessageListItemState) -> Unit = { messageListItem ->
        with(ChatTheme.componentFactory) {
            MessageListItemContainer(
                messageListItem = messageListItem,
                reactionSorting = reactionSorting,
                onPollUpdated = onPollUpdated,
                onCastVote = onCastVote,
                onRemoveVote = onRemoveVote,
                selectPoll = selectPoll,
                onClosePoll = onClosePoll,
                onAddPollOption = onAddPollOption,
                onLongItemClick = onLongItemClick,
                onThreadClick = onThreadClick,
                onReactionsClick = onReactionsClick,
                onGiphyActionClick = onGiphyActionClick,
                onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                onQuotedMessageClick = onQuotedMessageClick,
                onUserAvatarClick = onUserAvatarClick,
                onMessageLinkClick = onMessageLinkClick,
                onUserMentionClick = onUserMentionClick,
                onAddAnswer = onAddAnswer,
                onReply = onReply,
            )
        }
    },
) {
    val isLoading = currentState.isLoading
    val messages = currentState.messageItems

    Box {
        // Draw background behind the messages list
        background()
        // Draw the messages list content
        when {
            isLoading -> loadingContent()
            messages.isNotEmpty() -> {
                Messages(
                    modifier = modifier,
                    contentPadding = contentPadding,
                    messagesState = currentState,
                    messagesLazyListState = messagesLazyListState,
                    onMessagesStartReached = onMessagesPageStartReached,
                    verticalArrangement = verticalArrangement,
                    threadsVerticalArrangement = threadsVerticalArrangement,
                    threadMessagesStart = threadMessagesStart,
                    onLastVisibleMessageChanged = onLastVisibleMessageChanged,
                    onScrolledToBottom = onScrolledToBottom,
                    helperContent = helperContent,
                    loadingMoreContent = loadingMoreContent,
                    itemModifier = itemModifier,
                    itemContent = itemContent,
                    onMessagesEndReached = onMessagesPageEndReached,
                    onScrollToBottom = onScrollToBottom,
                )

                /** Clean up: Pause any playing audio tracks in onPause(). **/
                LifecycleEventEffect(Lifecycle.Event.ON_PAUSE, onEvent = onPauseAudioRecordingAttachments)
            }

            else -> emptyContent()
        }
    }
}
