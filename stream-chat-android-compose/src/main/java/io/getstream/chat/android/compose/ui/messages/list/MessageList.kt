package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResultType
import io.getstream.chat.android.compose.state.messages.MessagesState
import io.getstream.chat.android.compose.state.messages.MyOwn
import io.getstream.chat.android.compose.state.messages.items.MessageItem
import io.getstream.chat.android.compose.state.messages.items.MessageListItem
import io.getstream.chat.android.compose.ui.common.EmptyView
import io.getstream.chat.android.compose.ui.common.LoadingView
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Default MessageList component, that relies on [MessageListViewModel] to connect all the data
 * handling operations. It also delegates events to the ViewModel to handle, like long item
 * clicks and pagination.
 *
 * @param viewModel The ViewModel that stores all the data and business logic required to show a
 * list of messages. The user has to provide one in this case, as we require the channelId to start
 * the operations.
 * @param modifier Modifier for styling.
 * @param onThreadClick Handler when the user taps on the message, while there's a thread going.
 * @param onLongItemClick Handler for when the user long taps on a message and selects it.
 * @param onMessagesStartReached Handler for pagination.
 * @param onLastVisibleMessageChanged Handler that notifies us when the user scrolls and the last visible message changes.
 * @param onScrollToBottom Handler when the user reaches the bottom.
 * @param onImagePreviewResult Handler when the user selects an option in the Image Preview screen.
 * @param loadingContent Composable that represents the loading content, when we're loading the initial data.
 * @param emptyContent Composable that represents the empty content if there are no messages.
 * @param itemContent Composable that represents each message item in a list. By default, we provide
 * the [DefaultMessageContainer] and connect the the long click handler with it.
 * Users can override this to provide fully custom UI and behavior.
 */
@Composable
public fun MessageList(
    viewModel: MessageListViewModel,
    modifier: Modifier = Modifier,
    onThreadClick: (Message) -> Unit = { viewModel.openMessageThread(it) },
    onLongItemClick: (Message) -> Unit = { viewModel.selectMessage(it) },
    onMessagesStartReached: () -> Unit = { viewModel.loadMore() },
    onLastVisibleMessageChanged: (Message) -> Unit = { viewModel.updateLastSeenMessage(it) },
    onScrollToBottom: () -> Unit = { viewModel.clearNewMessageState() },
    onImagePreviewResult: (ImagePreviewResult?) -> Unit = {
        if (it?.resultType == ImagePreviewResultType.SHOW_IN_CHAT) {
            viewModel.focusMessage(it.messageId)
        }
    },
    loadingContent: @Composable () -> Unit = { LoadingView(modifier) },
    emptyContent: @Composable () -> Unit = { EmptyView(modifier) },
    itemContent: @Composable (MessageListItem) -> Unit = {
        DefaultMessageItem(
            messageListItem = it,
            onLongItemClick = onLongItemClick,
            onThreadClick = onThreadClick,
            onImagePreviewResult = onImagePreviewResult
        )
    },
) {
    MessageList(
        modifier = modifier,
        currentState = viewModel.currentMessagesState,
        onMessagesStartReached = onMessagesStartReached,
        onLastVisibleMessageChanged = onLastVisibleMessageChanged,
        onLongItemClick = onLongItemClick,
        onScrolledToBottom = onScrollToBottom,
        onImagePreviewResult = onImagePreviewResult,
        itemContent = itemContent,
        loadingContent = loadingContent,
        emptyContent = emptyContent
    )
}

/**
 * Clean representation of the MessageList that is decoupled from ViewModels. This components allows
 * users to connect the UI to their own data providers, as it relies on pure state.
 *
 * @param currentState The state of the component, represented by [MessagesState].
 * @param modifier Modifier for styling.
 * @param onMessagesStartReached Handler for pagination.
 * @param onLastVisibleMessageChanged Handler that notifies us when the user scrolls and the last visible message changes.
 * @param onScrolledToBottom Handler when the user scrolls to the bottom.
 * @param onThreadClick Handler for when the user taps on a message with an active thread.
 * @param onLongItemClick Handler for when the user long taps on an item.
 * @param onImagePreviewResult Handler when the user selects an option in the Image Preview screen.
 * @param loadingContent Composable that represents the loading content, when we're loading the initial data.
 * @param emptyContent Composable that represents the empty content if there are no messages.
 * @param itemContent Composable that represents each item in the list, that the user can override
 * for custom UI and behavior.
 */
@Composable
public fun MessageList(
    currentState: MessagesState,
    modifier: Modifier = Modifier,
    onMessagesStartReached: () -> Unit = {},
    onLastVisibleMessageChanged: (Message) -> Unit = {},
    onScrolledToBottom: () -> Unit = {},
    onThreadClick: (Message) -> Unit = {},
    onLongItemClick: (Message) -> Unit = {},
    onImagePreviewResult: (ImagePreviewResult?) -> Unit = {},
    loadingContent: @Composable () -> Unit = { LoadingView(modifier) },
    emptyContent: @Composable () -> Unit = { EmptyView(modifier) },
    itemContent: @Composable (MessageListItem) -> Unit = {
        DefaultMessageItem(
            messageListItem = it,
            onLongItemClick = onLongItemClick,
            onThreadClick = onThreadClick,
            onImagePreviewResult = onImagePreviewResult
        )
    },
) {
    val (isLoading, _, _, messages) = currentState

    when {
        isLoading -> loadingContent()
        !isLoading && messages.isNotEmpty() -> Messages(
            modifier = modifier,
            messagesState = currentState,
            onMessagesStartReached = onMessagesStartReached,
            onLastVisibleMessageChanged = onLastVisibleMessageChanged,
            onScrolledToBottom = onScrolledToBottom,
            itemContent = itemContent
        )
        else -> emptyContent()
    }
}

/**
 * Builds a list of message items, based on the [itemContent] parameter and the state provided within
 * [messagesState]. Also handles the pagination events, by propagating the event to the call site.
 *
 * Finally, it handles the scrolling behavior, such as when a new message arrives, be it ours or from
 * someone else.
 *
 * @param messagesState Current state of messages, like messages to display, if we're loading more
 * and if we've reached the end of the list.
 * @param onMessagesStartReached Handler for pagination, when the user reaches the start of messages.
 * @param onLastVisibleMessageChanged Handler that notifies us when the user scrolls and the last visible message changes.
 * @param onScrolledToBottom Handler when the user reaches the bottom of the list.
 * @param itemContent Composable that represents the item that displays each message.
 * @param modifier Modifier for styling.
 */
@Composable
public fun Messages(
    messagesState: MessagesState,
    onMessagesStartReached: () -> Unit,
    onLastVisibleMessageChanged: (Message) -> Unit,
    onScrolledToBottom: () -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (MessageListItem) -> Unit,
) {
    val (_, isLoadingMore, endOfMessages, messages, _, _, newMessageState, parentMessageId) = messagesState
    val state = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val currentListState = if (parentMessageId != null) rememberLazyListState() else state

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = currentListState,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom,
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            itemsIndexed(
                messages,
                key = { _, item ->
                    if (item is MessageItem) item.message.id else item.toString()
                }
            ) { index, item ->
                itemContent(item)

                if (item is MessageItem) {
                    onLastVisibleMessageChanged(item.message)
                }

                if (index == 0 && currentListState.isScrollInProgress) {
                    onScrolledToBottom()
                }

                if (!endOfMessages && index == messages.lastIndex && messages.isNotEmpty() && currentListState.isScrollInProgress) {
                    onMessagesStartReached()
                }
            }

            if (isLoadingMore) {
                item {
                    LoadingView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(8.dp)
                    )
                }
            }
        }
        val focusedItemIndex = messages.indexOfFirst { it is MessageItem && it.isFocused }

        if (focusedItemIndex != -1) {
            coroutineScope.launch {
                currentListState.scrollToItem(focusedItemIndex)
            }
        }

        val firstVisibleItemIndex = currentListState.firstVisibleItemIndex

        when {
            newMessageState == MyOwn -> coroutineScope.launch {
                if (firstVisibleItemIndex > 5) {
                    currentListState.scrollToItem(5)
                }
                currentListState.animateScrollToItem(0)
            }

            abs(firstVisibleItemIndex) >= 2 -> {
                MessagesScrollingOption(messagesState.unreadCount) {
                    coroutineScope.launch {
                        if (firstVisibleItemIndex > 5) {
                            currentListState.scrollToItem(5) // TODO - Try a custom animation spec
                        }
                        currentListState.animateScrollToItem(0)
                    }
                }
            }
        }
    }
}

/**
 * Shows an option when the user scrolls away from the bottom of the list. If there are any new messages it also gives
 * the user information on how many messages they haven't read.
 *
 * @param unreadCount The count of unread messages.
 * @param onClick The handler that's triggered when the user taps on the action.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BoxScope.MessagesScrollingOption(
    unreadCount: Int,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .align(BottomEnd)
            .padding(16.dp)
            .wrapContentSize()
    ) {
        Surface(
            modifier = Modifier
                .padding(top = 12.dp)
                .size(48.dp),
            shape = CircleShape,
            elevation = 4.dp,
            indication = rememberRipple(),
            onClick = onClick,
            color = ChatTheme.colors.barsBackground
        ) {
            Icon(
                modifier = Modifier.padding(16.dp),
                painter = painterResource(R.drawable.stream_compose_ic_arrow_down),
                contentDescription = null,
                tint = ChatTheme.colors.primaryAccent
            )
        }

        if (unreadCount != 0) {
            Surface(
                modifier = Modifier
                    .align(TopCenter),
                shape = RoundedCornerShape(16.dp),
                color = ChatTheme.colors.primaryAccent
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    text = unreadCount.toString(),
                    style = ChatTheme.typography.footnoteBold,
                    color = Color.White
                )
            }
        }
    }
}
