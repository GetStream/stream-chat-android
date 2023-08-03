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

package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResultType
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.rememberMessageListState
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageListItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListState

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
 * @param threadMessagesStart Thread messages start at the bottom or top of the screen.
 * Default: [ThreadMessagesStart.BOTTOM].
 * @param onThreadClick Handler when the user taps on the message, while there's a thread going.
 * @param onLongItemClick Handler for when the user long taps on a message and selects it.
 * @param onReactionsClick Handler when the user taps on message reactions and selects them.
 * @param onMessagesPageStartReached Handler for pagination when the end of the oldest messages has been reached.
 * @param onLastVisibleMessageChanged Handler that notifies us when the user scrolls and the last visible message
 * changes.
 * @param onScrollToBottom Handler when the user reaches the bottom.
 * @param onGiphyActionClick Handler when the user clicks on a giphy action such as shuffle, send or cancel.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onMediaGalleryPreviewResult Handler when the user selects an option in the Media Gallery Preview screen.
 * @param onMessagesPageEndReached Handler for pagination when the end of newest messages have been reached.
 * @param onScrollToBottomClicked Handler when the user requests to scroll to the bottom of the messages list.
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
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    messagesLazyListState: MessagesLazyListState =
        rememberMessageListState(parentMessageId = viewModel.currentMessagesState.parentMessageId),
    threadMessagesStart: ThreadMessagesStart = ThreadMessagesStart.BOTTOM,
    onThreadClick: (Message) -> Unit = { viewModel.openMessageThread(it) },
    onLongItemClick: (Message) -> Unit = { viewModel.selectMessage(it) },
    onReactionsClick: (Message) -> Unit = { viewModel.selectReactions(it) },
    onMessagesPageStartReached: () -> Unit = { viewModel.loadOlderMessages() },
    onLastVisibleMessageChanged: (Message) -> Unit = { viewModel.updateLastSeenMessage(it) },
    onScrollToBottom: () -> Unit = { viewModel.clearNewMessageState() },
    onGiphyActionClick: (GiphyAction) -> Unit = { viewModel.performGiphyAction(it) },
    onQuotedMessageClick: (Message) -> Unit = { message ->
        viewModel.scrollToMessage(
            messageId = message.id,
            parentMessageId = message.parentId,
        )
    },
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {
        if (it?.resultType == MediaGalleryPreviewResultType.SHOW_IN_CHAT) {
            viewModel.scrollToMessage(
                messageId = it.messageId,
                parentMessageId = it.parentMessageId,
            )
        }
    },
    onMessagesPageEndReached: (String) -> Unit = { viewModel.loadNewerMessages(it) },
    onScrollToBottomClicked: (() -> Unit) -> Unit = { viewModel.scrollToBottom(scrollToBottom = it) },
    loadingContent: @Composable () -> Unit = { DefaultMessageListLoadingIndicator(modifier) },
    emptyContent: @Composable () -> Unit = { DefaultMessageListEmptyContent(modifier) },
    helperContent: @Composable BoxScope.() -> Unit = {
        DefaultMessagesHelperContent(
            messagesState = viewModel.currentMessagesState,
            messagesLazyListState = messagesLazyListState,
            scrollToBottom = onScrollToBottomClicked,
        )
    },
    loadingMoreContent: @Composable () -> Unit = { DefaultMessagesLoadingMoreIndicator() },
    itemContent: @Composable (MessageListItemState) -> Unit = { messageListItem ->
        DefaultMessageContainer(
            messageListItemState = messageListItem,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onThreadClick = onThreadClick,
            onLongItemClick = onLongItemClick,
            onReactionsClick = onReactionsClick,
            onGiphyActionClick = onGiphyActionClick,
            onQuotedMessageClick = onQuotedMessageClick,
        )
    },
) {
    MessageList(
        modifier = modifier,
        contentPadding = contentPadding,
        currentState = viewModel.currentMessagesState,
        messagesLazyListState = messagesLazyListState,
        onMessagesPageStartReached = onMessagesPageStartReached,
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
    )
}

/**
 * The default message container item.
 *
 * @param messageListItemState The state of the message list item.
 * @param onMediaGalleryPreviewResult Handler when the user receives a result from the Media Gallery Preview.
 * @param onThreadClick Handler when the user taps on a thread within a message item.
 * @param onLongItemClick Handler when the user long taps on an item.
 * @param onReactionsClick Handler when the user taps on message reactions.
 * @param onGiphyActionClick Handler when the user taps on Giphy message actions.
 * @param onQuotedMessageClick Handler for quoted message click action.
 */
@Composable
internal fun DefaultMessageContainer(
    messageListItemState: MessageListItemState,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onThreadClick: (Message) -> Unit,
    onLongItemClick: (Message) -> Unit,
    onReactionsClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit,
    onQuotedMessageClick: (Message) -> Unit,
) {
    MessageContainer(
        messageListItemState = messageListItemState,
        onLongItemClick = onLongItemClick,
        onReactionsClick = onReactionsClick,
        onThreadClick = onThreadClick,
        onGiphyActionClick = onGiphyActionClick,
        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
        onQuotedMessageClick = onQuotedMessageClick,
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
        modifier = modifier.background(color = ChatTheme.colors.appBackground),
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
 * @param threadMessagesStart Thread messages start at the bottom or top of the screen.
 * Default: [ThreadMessagesStart.BOTTOM].
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
 * @param onMessagesPageEndReached Handler for pagination when the end of newest messages have been reached.
 * @param onScrollToBottom Handler when the user requests to scroll to the bottom of the messages list.
 * @param loadingContent Composable that represents the loading content, when we're loading the initial data.
 * @param emptyContent Composable that represents the empty content if there are no messages.
 * @param helperContent Composable that, by default, represents the helper content featuring scrolling behavior based
 * on the list state.
 * @param loadingMoreContent Composable that represents the loading more content, when we're loading the next page.
 * @param itemContent Composable that represents each item in the list, that the user can override
 * for custom UI and behavior.
 */
@Composable
public fun MessageList(
    currentState: MessageListState,
    threadMessagesStart: ThreadMessagesStart = ThreadMessagesStart.BOTTOM,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    messagesLazyListState: MessagesLazyListState =
        rememberMessageListState(parentMessageId = currentState.parentMessageId),
    onMessagesPageStartReached: () -> Unit = {},
    onLastVisibleMessageChanged: (Message) -> Unit = {},
    onScrolledToBottom: () -> Unit = {},
    onThreadClick: (Message) -> Unit = {},
    onLongItemClick: (Message) -> Unit = {},
    onReactionsClick: (Message) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onMessagesPageEndReached: (String) -> Unit = {},
    onScrollToBottom: (() -> Unit) -> Unit = {},
    loadingContent: @Composable () -> Unit = { DefaultMessageListLoadingIndicator(modifier) },
    emptyContent: @Composable () -> Unit = { DefaultMessageListEmptyContent(modifier) },
    helperContent: @Composable BoxScope.() -> Unit = {
        DefaultMessagesHelperContent(
            messagesState = currentState,
            messagesLazyListState = messagesLazyListState,
            scrollToBottom = onScrollToBottom,
        )
    },
    loadingMoreContent: @Composable () -> Unit = { DefaultMessagesLoadingMoreIndicator() },
    itemContent: @Composable (MessageListItemState) -> Unit = {
        DefaultMessageContainer(
            messageListItemState = it,
            onLongItemClick = onLongItemClick,
            onThreadClick = onThreadClick,
            onReactionsClick = onReactionsClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
        )
    },
) {
    val isLoading = currentState.isLoading
    val messages = currentState.messageItems

    when {
        isLoading -> loadingContent()
        messages.isNotEmpty() -> Messages(
            modifier = modifier,
            contentPadding = contentPadding,
            messagesState = currentState,
            messagesLazyListState = messagesLazyListState,
            onMessagesStartReached = onMessagesPageStartReached,
            threadMessagesStart = threadMessagesStart,
            onLastVisibleMessageChanged = onLastVisibleMessageChanged,
            onScrolledToBottom = onScrolledToBottom,
            helperContent = helperContent,
            loadingMoreContent = loadingMoreContent,
            itemContent = itemContent,
            onMessagesEndReached = onMessagesPageEndReached,
            onScrollToBottom = onScrollToBottom,
        )
        else -> emptyContent()
    }
}
