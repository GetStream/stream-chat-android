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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResultType
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageItemParams
import io.getstream.chat.android.compose.ui.theme.MessageListBackgroundParams
import io.getstream.chat.android.compose.ui.theme.MessageListEmptyContentParams
import io.getstream.chat.android.compose.ui.theme.MessageListLoadingIndicatorParams
import io.getstream.chat.android.compose.ui.util.rememberMessageListState
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
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
 * @param modifier Modifier for styling.
 * @param contentPadding Padding values to be applied to the message list surrounding the content inside.
 * @param messagesLazyListState State of the lazy list that represents the list of messages. Useful for controlling the
 * scroll state and focused message offset.
 * @param verticalArrangement Vertical arrangement of the regular message list.
 * Default: [Arrangement.Bottom].
 * @param threadsVerticalArrangement Vertical arrangement of the thread message list.
 * Default: [Arrangement.Bottom].
 * @param onThreadClick Handler when the user taps on the message, while there's a thread going.
 * @param onLongItemClick Handler for when the user long taps on a message and selects it.
 * @param onMessagesPageStartReached Handler for pagination when the end of the oldest messages has been reached.
 * @param onLastVisibleMessageChanged Handler that notifies us when the user scrolls and the last visible message
 * changes.
 * @param onScrollToBottom Handler when the user reaches the bottom.
 * @param onMediaGalleryPreviewResult Handler when the user selects an option in the Media Gallery Preview screen.
 * @param onMessagesPageEndReached Handler for pagination when the end of newest messages have been reached.
 * @param onScrollToBottomClicked Handler when the user requests to scroll to the bottom of the messages list.
 * @param onScrollToFirstUnreadClicked Handler when the user taps the scroll-to-first-unread pill.
 * @param onDismissUnreadLabel Handler when the user dismisses the scroll-to-first-unread pill via
 * its close affordance.
 * @param onPauseAudioRecordingAttachments Handler for lifecycle events.
 */
@Composable
public fun MessageList(
    viewModel: MessageListViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    messagesLazyListState: MessagesLazyListState =
        rememberMessageListState(parentMessageId = viewModel.currentMessagesState.value.parentMessageId),
    verticalArrangement: Arrangement.Vertical = Arrangement.Bottom,
    threadsVerticalArrangement: Arrangement.Vertical = Arrangement.Bottom,
    onThreadClick: (Message) -> Unit = {
        if (viewModel.isInThread) {
            viewModel.leaveThread()
        } else {
            viewModel.openMessageThread(it)
        }
    },
    onLongItemClick: (Message) -> Unit = { viewModel.selectMessage(it) },
    onMessagesPageStartReached: () -> Unit = { viewModel.loadOlderMessages() },
    onLastVisibleMessageChanged: (Message) -> Unit = { viewModel.updateLastSeenMessage(it) },
    onScrollToBottom: () -> Unit = { viewModel.clearNewMessageState() },
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
    onScrollToFirstUnreadClicked: () -> Unit = { viewModel.scrollToFirstUnreadMessage() },
    onDismissUnreadLabel: () -> Unit = { viewModel.disableUnreadLabelButton() },
    onPauseAudioRecordingAttachments: () -> Unit = { viewModel.pauseAudioRecordingAttachments() },
) {
    MessageList(
        modifier = modifier,
        contentPadding = contentPadding,
        currentState = viewModel.currentMessagesState.value,
        messagesLazyListState = messagesLazyListState,
        verticalArrangement = verticalArrangement,
        threadsVerticalArrangement = threadsVerticalArrangement,
        onMessagesPageStartReached = onMessagesPageStartReached,
        onLastVisibleMessageChanged = onLastVisibleMessageChanged,
        onScrolledToBottom = onScrollToBottom,
        onMessagesPageEndReached = onMessagesPageEndReached,
        onScrollToBottom = onScrollToBottomClicked,
        onScrollToFirstUnread = onScrollToFirstUnreadClicked,
        onDismissUnreadLabel = onDismissUnreadLabel,
        onPauseAudioRecordingAttachments = onPauseAudioRecordingAttachments,
        messageItemParams = { messageListItem ->
            MessageItemParams(
                messageListItem = messageListItem,
                onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                onCastVote = viewModel::castVote,
                onRemoveVote = viewModel::removeVote,
                selectPoll = { message, poll, selectionType ->
                    viewModel.displayPollMoreOptions(SelectedPoll(poll, message, selectionType))
                },
                onPollUpdated = { message, poll ->
                    val selectedPoll = viewModel.pollState.selectedPoll
                    if (viewModel.isShowingPollOptionDetails &&
                        selectedPoll != null && selectedPoll.poll.id == poll.id
                    ) {
                        viewModel.updatePollState(poll, message, selectedPoll.pollSelectionType)
                    }
                },
                onClosePoll = viewModel::closePoll,
                onAddPollOption = viewModel::addPollOption,
                onThreadClick = onThreadClick,
                onLongItemClick = onLongItemClick,
                onReactionsClick = viewModel::selectReactions,
                onGiphyActionClick = viewModel::performGiphyAction,
                onQuotedMessageClick = { message ->
                    viewModel.scrollToMessage(
                        messageId = message.id,
                        parentMessageId = message.parentId,
                    )
                },
                onAddAnswer = viewModel::castAnswer,
                onReply = onReply,
            )
        },
    )
}

/**
 * The default message item component, which renders each [MessageListItemState]'s subtype.
 *
 * @param messageListItemState The state of the message list item.
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
internal fun LazyItemScope.DefaultMessageItem(
    messageListItemState: MessageListItemState,
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
    onMentionClick: (Mention) -> Unit = {},
) {
    MessageItem(
        messageListItemState = messageListItemState,
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
        onMentionClick = onMentionClick,
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
            style = ChatTheme.typography.bodyDefault,
            color = ChatTheme.colors.textSecondary,
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
 * Default: [Arrangement.Bottom].
 * @param threadsVerticalArrangement Vertical arrangement of the thread message list.
 * Default: [Arrangement.Bottom].
 * @param modifier Modifier for styling.
 * @param contentPadding Padding values to be applied to the message list surrounding the content inside.
 * @param messagesLazyListState State of the lazy list that represents the list of messages. Useful for controlling the
 * scroll state and focused message offset.
 * @param onMessagesPageStartReached Handler for pagination.
 * @param onLastVisibleMessageChanged Handler that notifies us when the user scrolls and the last visible message
 * changes.
 * @param onScrolledToBottom Handler when the user scrolls to the bottom.
 * @param onMessagesPageEndReached Handler for pagination when the end of newest messages have been reached.
 * @param onScrollToBottom Handler when the user requests to scroll to the bottom of the messages list.
 * @param onScrollToFirstUnread Handler when the user taps the scroll-to-first-unread pill.
 * @param onDismissUnreadLabel Handler when the user dismisses the scroll-to-first-unread pill via
 * its close affordance.
 * @param onPauseAudioRecordingAttachments Handler for lifecycle events.
 * @param messageItemParams Factory that builds [MessageItemParams] for each message list item.
 */
@Composable
public fun MessageList(
    currentState: MessageListState,
    verticalArrangement: Arrangement.Vertical = Arrangement.Bottom,
    threadsVerticalArrangement: Arrangement.Vertical = Arrangement.Bottom,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    messagesLazyListState: MessagesLazyListState =
        rememberMessageListState(parentMessageId = currentState.parentMessageId),
    onMessagesPageStartReached: () -> Unit = {},
    onLastVisibleMessageChanged: (Message) -> Unit = {},
    onScrolledToBottom: () -> Unit = {},
    onMessagesPageEndReached: (String) -> Unit = {},
    onScrollToBottom: (() -> Unit) -> Unit = {},
    onScrollToFirstUnread: () -> Unit = {},
    onDismissUnreadLabel: () -> Unit = {},
    onPauseAudioRecordingAttachments: () -> Unit = {},
    messageItemParams: (MessageListItemState) -> MessageItemParams = ::MessageItemParams,
) {
    val isLoading = currentState.isLoading
    val messages = currentState.messageItems

    Box {
        ChatTheme.componentFactory.MessageListBackground(params = MessageListBackgroundParams())
        when {
            isLoading -> ChatTheme.componentFactory.MessageListLoadingIndicator(
                params = MessageListLoadingIndicatorParams(modifier = modifier),
            )

            messages.isNotEmpty() -> {
                Messages(
                    modifier = modifier,
                    contentPadding = contentPadding,
                    messagesState = currentState,
                    messagesLazyListState = messagesLazyListState,
                    onMessagesStartReached = onMessagesPageStartReached,
                    verticalArrangement = verticalArrangement,
                    threadsVerticalArrangement = threadsVerticalArrangement,
                    onLastVisibleMessageChanged = onLastVisibleMessageChanged,
                    onScrolledToBottom = onScrolledToBottom,
                    onMessagesEndReached = onMessagesPageEndReached,
                    onScrollToBottom = onScrollToBottom,
                    onScrollToFirstUnread = onScrollToFirstUnread,
                    onDismissUnreadLabel = onDismissUnreadLabel,
                    itemContent = { messageListItem ->
                        with(ChatTheme.componentFactory) {
                            MessageItem(params = messageItemParams(messageListItem))
                        }
                    },
                )

                LifecycleEventEffect(Lifecycle.Event.ON_PAUSE, onEvent = onPauseAudioRecordingAttachments)
            }

            else -> ChatTheme.componentFactory.MessageListEmptyContent(
                params = MessageListEmptyContentParams(modifier = modifier),
            )
        }
    }
}
