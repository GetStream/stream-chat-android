package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.MessagesState
import io.getstream.chat.android.compose.state.messages.MyOwn
import io.getstream.chat.android.compose.state.messages.Other
import io.getstream.chat.android.compose.state.messages.items.Bottom
import io.getstream.chat.android.compose.state.messages.items.MessageItem
import io.getstream.chat.android.compose.state.messages.items.None
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
 * @param viewModel - The ViewModel that stores all the data and business logic required to show a
 * list of messages. The user has to provide one in this case, as we require the channelId to start
 * the operations.
 * @param modifier - Modifier for styling.
 * @param onThreadClick - Handler when the user taps on the message, while there's a thread going.
 * @param onLongItemClick - Handler for when the user long taps on a message and selects it.
 * @param onMessagesStartReached - Handler for pagination.
 * @param onScrollToBottom - Handler when the user reaches the bottom.
 * @param itemContent - Composable that represents each message item in a list. By default, we provide
 * either the [DefaultMessageGroup] or the [DefaultMessageContainer] and connect the the long click handler with it.
 * Users can override this to provide fully custom UI and behavior.
 * */
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun MessageList(
    viewModel: MessageListViewModel,
    modifier: Modifier = Modifier,
    onThreadClick: (Message) -> Unit = { viewModel.onMessageThreadClick(it) },
    onLongItemClick: (Message) -> Unit = { viewModel.onMessageSelected(it) },
    onMessagesStartReached: () -> Unit = { viewModel.onLoadMore() },
    onScrollToBottom: () -> Unit = { viewModel.onScrolledToBottom() },
    itemContent: @Composable (MessageItem) -> Unit = {
        DefaultMessageContainer(
            messageItem = it,
            onThreadClick = onThreadClick,
            onLongItemClick = onLongItemClick,
            currentUser = viewModel.currentMessagesState.currentUser
        )
    },
) {
    MessageList(
        modifier = modifier,
        currentState = viewModel.currentMessagesState,
        onMessagesStartReached = onMessagesStartReached,
        onLongItemClick = onLongItemClick,
        onScrollToBottom = onScrollToBottom,
        itemContent = itemContent
    )
}

/**
 * Clean representation of the MessageList that is decoupled from ViewModels. This components allows
 * users to connect the UI to their own data providers, as it relies on pure state.
 *
 * @param modifier - Modifier for styling.
 * @param currentState - The state of the component, represented by [MessagesState].
 * @param onMessagesStartReached - Handler for pagination.
 * @param onScrollToBottom - Handler when the user scrolls to the bottom.
 * @param onLongItemClick - Handler for when the user long taps on an item.
 * @param onThreadClick - Handler for when the user taps on a message with an active thread.
 * @param itemContent - Composable that represents each item in the list, that the user can override
 * for custom UI and behavior.
 * */
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun MessageList(
    currentState: MessagesState,
    modifier: Modifier = Modifier,
    onMessagesStartReached: () -> Unit = {},
    onScrollToBottom: () -> Unit = {},
    onThreadClick: (Message) -> Unit = {},
    onLongItemClick: (Message) -> Unit = {},
    itemContent: @Composable (MessageItem) -> Unit = {
        DefaultMessageContainer(
            messageItem = it,
            onThreadClick = onThreadClick,
            onLongItemClick = onLongItemClick,
            currentUser = currentState.currentUser
        )
    },
) {
    val (isLoading, _, _, messages) = currentState

    when {
        isLoading -> LoadingView(modifier)
        !isLoading && messages.isNotEmpty() -> Messages(
            modifier = modifier,
            messagesState = currentState,
            onMessagesStartReached = onMessagesStartReached,
            onScrollToBottom = onScrollToBottom,
            itemContent = itemContent
        )
        else -> EmptyView()
    }
}

/**
 * Builds a list of message items, based on the [itemContent] parameter and the state provided within
 * [messagesState]. Also handles the pagination events, by propagating the event to the call site.
 *
 * Finally, it handles the scrolling behavior, such as when a new message arrives, be it ours or from
 * someone else.
 *
 * @param messagesState - Current state of messages, like messages to display, if we're loading more
 * and if we've reached the end of the list.
 * @param onMessagesStartReached - Handler for pagination, when the user reaches the start of messages.
 * @param onScrollToBottom - Handler when the user reaches the bottom of the list.
 * @param itemContent - Composable that represents the item that displays each message.
 * @param modifier - Modifier for styling.
 * */
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
private fun Messages(
    messagesState: MessagesState,
    onMessagesStartReached: () -> Unit,
    onScrollToBottom: () -> Unit,
    itemContent: @Composable (MessageItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (_, isLoadingMore, endOfMessages, messages, _, _, newMessageState, parentMessageId) = messagesState
    val state = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val currentListState = if (parentMessageId != null) rememberLazyListState() else state

    Box(modifier = modifier.background(ChatTheme.colors.appBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = currentListState,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom,
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            itemsIndexed(messages.reversed()) { index, item ->
                itemContent(item)

                if (index == 0 && currentListState.isScrollInProgress) {
                    onScrollToBottom()
                }

                if (!endOfMessages && index == messages.lastIndex && messages.isNotEmpty() && currentListState.isScrollInProgress) {
                    onMessagesStartReached()
                }

                if (item.position == None || item.position == Bottom) {
                    Spacer(Modifier.size(4.dp))
                } else {
                    Spacer(Modifier.size(2.dp))
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

        when {
            newMessageState == MyOwn -> coroutineScope.launch {
                currentListState.scrollToItem(5)
                currentListState.animateScrollToItem(0)
            }

            abs(currentListState.firstVisibleItemIndex) >= 3 -> {
                val title =
                    if (newMessageState == Other) R.string.new_message else R.string.scroll_to_bottom

                MessagesScrollingOption(text = stringResource(id = title)) {
                    coroutineScope.launch {
                        currentListState.scrollToItem(5) // TODO - Try a custom animation spec
                        currentListState.animateScrollToItem(0)
                    }
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun BoxScope.MessagesScrollingOption(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        onClick = onClick
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = text
        )
    }
}