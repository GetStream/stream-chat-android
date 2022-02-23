package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.state.messages.MessagesState
import io.getstream.chat.android.compose.state.messages.MyOwn
import io.getstream.chat.android.compose.state.messages.Other
import io.getstream.chat.android.compose.state.messages.list.MessageFocused
import io.getstream.chat.android.compose.state.messages.list.MessageItemState
import io.getstream.chat.android.compose.state.messages.list.MessageListItemState
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.components.messages.MessagesScrollingOption
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Builds a list of message items, based on the [itemContent] parameter and the state provided within
 * [messagesState]. Also handles the pagination events, by propagating the event to the call site.
 *
 * Finally, it handles the scrolling behavior, such as when a new message arrives, be it ours or from
 * someone else.
 *
 * @param messagesState Current state of messages, like messages to display, if we're loading more
 * and if we've reached the end of the list.
 * @param lazyListState State of the lazy list that represents the list of messages. Useful for controlling the scroll state.
 * @param onMessagesStartReached Handler for pagination, when the user reaches the start of messages.
 * @param onLastVisibleMessageChanged Handler that notifies us when the user scrolls and the last visible message changes.
 * @param onScrolledToBottom Handler when the user reaches the bottom of the list.
 * @param modifier Modifier for styling.
 * @param helperContent Composable that, by default, represents the helper content featuring scrolling behavior based on the list state.
 * @param loadingMoreContent Composable that represents the loading more content, when we're loading the next page.
 * @param itemContent Composable that represents the item that displays each message.
 */
@Composable
public fun Messages(
    messagesState: MessagesState,
    lazyListState: LazyListState,
    onMessagesStartReached: () -> Unit,
    onLastVisibleMessageChanged: (Message) -> Unit,
    onScrolledToBottom: () -> Unit,
    modifier: Modifier = Modifier,
    helperContent: @Composable BoxScope.() -> Unit = {
        DefaultMessagesHelperContent(messagesState, lazyListState)
    },
    loadingMoreContent: @Composable () -> Unit = { DefaultMessagesLoadingMoreIndicator() },
    itemContent: @Composable (MessageListItemState) -> Unit,
) {
    val (_, isLoadingMore, endOfMessages, messages) = messagesState

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom,
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            itemsIndexed(
                messages,
                key = { _, item ->
                    if (item is MessageItemState) item.message.id else item.toString()
                }
            ) { index, item ->
                itemContent(item)

                if (item is MessageItemState) {
                    onLastVisibleMessageChanged(item.message)
                }

                if (index == 0 && lazyListState.isScrollInProgress) {
                    onScrolledToBottom()
                }

                if (!endOfMessages && index == messages.lastIndex && messages.isNotEmpty() && lazyListState.isScrollInProgress) {
                    onMessagesStartReached()
                }
            }

            if (isLoadingMore) {
                item {
                    loadingMoreContent()
                }
            }
        }

        helperContent()
    }
}

/**
 * Represents the default scrolling behavior and UI for [Messages], based on the state of messages and the scroll state.
 *
 * @param messagesState The state of messages, current message list, thread, user and more.
 * @param lazyListState The scrolling state of the list, used to manipulate and trigger scroll events.
 */
@Composable
internal fun BoxScope.DefaultMessagesHelperContent(
    messagesState: MessagesState,
    lazyListState: LazyListState,
) {
    val (_, _, _, messages, _, _, newMessageState) = messagesState
    val coroutineScope = rememberCoroutineScope()

    val firstVisibleItemIndex = lazyListState.firstVisibleItemIndex

    val focusedItemIndex = messages.indexOfFirst { it is MessageItemState && it.focusState is MessageFocused }

    LaunchedEffect(key1 = newMessageState, key2 = firstVisibleItemIndex, focusedItemIndex) {
        if (focusedItemIndex != -1) {
            coroutineScope.launch {
                lazyListState.scrollToItem(focusedItemIndex)
            }
        }
        when {
            !lazyListState.isScrollInProgress && newMessageState == Other && firstVisibleItemIndex < 3 -> {
                coroutineScope.launch {
                    lazyListState.animateScrollToItem(0)
                }
            }
            !lazyListState.isScrollInProgress && newMessageState == MyOwn -> coroutineScope.launch {
                if (firstVisibleItemIndex > 5) {
                    lazyListState.scrollToItem(5)
                }
                lazyListState.animateScrollToItem(0)
            }

            else -> Unit
        }
    }

    if (abs(firstVisibleItemIndex) >= 3) {
        MessagesScrollingOption(
            unreadCount = messagesState.unreadCount,
            modifier = Modifier.align(Alignment.BottomEnd),
            onClick = {
                coroutineScope.launch {
                    if (firstVisibleItemIndex > 5) {
                        lazyListState.scrollToItem(5)
                    }
                    lazyListState.animateScrollToItem(0)
                }
            }
        )
    }
}

/**
 * The default loading more indicator.
 */
@Composable
internal fun DefaultMessagesLoadingMoreIndicator() {
    LoadingIndicator(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp)
    )
}
