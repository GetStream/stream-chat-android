package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.state.messages.MessagesState
import io.getstream.chat.android.compose.state.messages.MyOwn
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
    itemContent: @Composable (MessageListItemState) -> Unit,
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
                    if (item is MessageItemState) item.message.id else item.toString()
                }
            ) { index, item ->
                itemContent(item)

                if (item is MessageItemState) {
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
                    LoadingIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(8.dp)
                    )
                }
            }
        }
        val focusedItemIndex = messages.indexOfFirst { it is MessageItemState && it.focusState is MessageFocused }

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
                MessagesScrollingOption(
                    unreadCount = messagesState.unreadCount,
                    modifier = Modifier.align(Alignment.BottomEnd),
                    onClick = {
                        coroutineScope.launch {
                            if (firstVisibleItemIndex > 5) {
                                currentListState.scrollToItem(5) // TODO - Try a custom animation spec
                            }
                            currentListState.animateScrollToItem(0)
                        }
                    }
                )
            }
        }
    }
}
