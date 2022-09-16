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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.state.messages.MessagesState
import io.getstream.chat.android.compose.state.messages.MyOwn
import io.getstream.chat.android.compose.state.messages.NewMessageState
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
 * @param lazyListState State of the lazy list that represents the list of messages. Useful for controlling the
 * scroll state.
 * @param onMessagesStartReached Handler for pagination, when the user reaches the start of messages.
 * @param onLastVisibleMessageChanged Handler that notifies us when the user scrolls and the last visible message
 * changes.
 * @param onScrolledToBottom Handler when the user reaches the bottom of the list.
 * @param modifier Modifier for styling.
 * @param contentPadding Padding values to be applied to the message list surrounding the content inside.
 * @param helperContent Composable that, by default, represents the helper content featuring scrolling behavior based
 * on the list state.
 * @param loadingMoreContent Composable that represents the loading more content, when we're loading the next page.
 * @param itemContent Composable that represents the item that displays each message.
 */
@Deprecated(
    message = "Deprecated in favor of new Messages composable implementation which adds support for bidirectional" +
        "pagination and improved scroll to bottom implementation",
    replaceWith = ReplaceWith(
        expression = "public fun Messages(" +
            "    messagesState: MessagesState," +
            "    lazyListState: LazyListState," +
            "    onMessagesStartReached: () -> Unit," +
            "    onLastVisibleMessageChanged: (Message) -> Unit," +
            "    onScrolledToBottom: () -> Unit," +
            "    onMessagesEndReached: (String) -> Unit," +
            "    onScrollToBottom: () -> Unit," +
            "    modifier: Modifier = Modifier," +
            "    contentPadding: PaddingValues," +
            "    helperContent: @Composable BoxScope.() -> Unit," +
            "    loadingMoreContent: @Composable () -> Unit," +
            "    itemContent: @Composable (MessageListItemState) -> Unit)",
        imports = arrayOf("io.getstream.chat.android.compose.ui.messages.list")
    ),
    level = DeprecationLevel.WARNING
)
@Composable
public fun Messages(
    messagesState: MessagesState,
    lazyListState: LazyListState,
    onMessagesStartReached: () -> Unit,
    onLastVisibleMessageChanged: (Message) -> Unit,
    onScrolledToBottom: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    helperContent: @Composable BoxScope.() -> Unit = {
        val coroutineScope = rememberCoroutineScope()
        DefaultMessagesHelperContent(messagesState, lazyListState) {
            coroutineScope.launch {
                coroutineScope.launch {
                    lazyListState.scrollToItem(0)
                }
            }
        }
    },
    loadingMoreContent: @Composable () -> Unit = { DefaultMessagesLoadingMoreIndicator() },
    itemContent: @Composable (MessageListItemState) -> Unit,
) {
    val (_, isLoadingMore, endOfMessages, messages) = messagesState

    var parentSize by remember { mutableStateOf(IntSize(0, 0)) }
    val density = LocalDensity.current

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    val bottomPadding = contentPadding.calculateBottomPadding()
                    val topPadding = contentPadding.calculateTopPadding()

                    val paddingPixels = with(density) {
                        bottomPadding.roundToPx() + topPadding.roundToPx()
                    }

                    parentSize = IntSize(
                        width = it.size.width,
                        height = it.size.height - paddingPixels
                    )
                },
            state = lazyListState,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom,
            reverseLayout = true,
            contentPadding = contentPadding
        ) {
            itemsIndexed(
                messages,
                key = { _, item ->
                    if (item is MessageItemState) item.message.id else item.toString()
                }
            ) { index, item ->
                val messageItemModifier = if (item is MessageItemState && item.focusState == MessageFocused) {
                    Modifier.onGloballyPositioned {
                        if (messagesState.focusedMessageOffset.value == null) {
                            messagesState.calculateMessageOffset(parentSize, it.size)
                        }
                    }
                } else {
                    Modifier
                }

                Box(modifier = messageItemModifier) {
                    itemContent(item)

                    if (item is MessageItemState) {
                        onLastVisibleMessageChanged(item.message)
                    }

                    if (index == 0 && lazyListState.isScrollInProgress) {
                        onScrolledToBottom()
                    }

                    if (!endOfMessages && index == messages.lastIndex &&
                        messages.isNotEmpty() &&
                        lazyListState.isScrollInProgress
                    ) {
                        onMessagesStartReached()
                    }
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
 * Builds a list of message items, based on the [itemContent] parameter and the state provided within
 * [messagesState]. Also handles the pagination events, by propagating the event to the call site.
 *
 * Finally, it handles the scrolling behavior, such as when a new message arrives, be it ours or from
 * someone else.
 *
 * @param messagesState Current state of messages, like messages to display, if we're loading more
 * and if we've reached the end of the list.
 * @param lazyListState State of the lazy list that represents the list of messages. Useful for controlling the
 * scroll state.
 * @param onMessagesStartReached Handler for pagination, when the user reaches the start of messages.
 * @param onLastVisibleMessageChanged Handler that notifies us when the user scrolls and the last visible message
 * changes.
 * @param onScrolledToBottom Handler when the user reaches the bottom of the list.
 * @param onMessagesEndReached Handler for pagination, when the user reaches the end of messages.
 * @param onScrollToBottom Handler when the user requests to scroll to the bottom of the messages list.
 * @param modifier Modifier for styling.
 * @param contentPadding Padding values to be applied to the message list surrounding the content inside.
 * @param helperContent Composable that, by default, represents the helper content featuring scrolling behavior based
 * on the list state.
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
    onMessagesEndReached: (String) -> Unit,
    onScrollToBottom: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    helperContent: @Composable BoxScope.() -> Unit = {
        DefaultMessagesHelperContent(
            messagesState = messagesState,
            lazyListState = lazyListState,
            scrollToBottom = onScrollToBottom
        )
    },
    loadingMoreContent: @Composable () -> Unit = { DefaultMessagesLoadingMoreIndicator() },
    itemContent: @Composable (MessageListItemState) -> Unit,
) {
    val messages = messagesState.messageItems
    val endOfMessages = messagesState.endOfMessages
    val startOfMessages = messagesState.startOfMessages
    val isLoadingMoreNewMessages = messagesState.isLoadingMoreNewMessages
    val isLoadingMoreOldMessages = messagesState.isLoadingMoreOldMessages

    var parentSize = remember { IntSize(0, 0) }
    val density = LocalDensity.current

    /** Marks the bottom most item as read every time it changes. **/
    OnLastVisibleItemChanged(lazyListState) { messageIndex ->
        val message = messagesState.messageItems.getOrNull(messageIndex)

        if (message is MessageItemState) {
            onLastVisibleMessageChanged(message.message)
        }
    }

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    val bottomPadding = contentPadding.calculateBottomPadding()
                    val topPadding = contentPadding.calculateTopPadding()

                    val paddingPixels = with(density) {
                        bottomPadding.roundToPx() + topPadding.roundToPx()
                    }

                    parentSize = IntSize(
                        width = it.size.width,
                        height = it.size.height - paddingPixels
                    )
                },
            state = lazyListState,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom,
            reverseLayout = true,
            contentPadding = contentPadding
        ) {

            if (isLoadingMoreNewMessages && !startOfMessages) {
                item {
                    loadingMoreContent()
                }
            }

            itemsIndexed(
                messages,
                key = { _, item ->
                    if (item is MessageItemState) item.message.id else item.toString()
                }
            ) { index, item ->
                val messageItemModifier = if (item is MessageItemState && item.focusState == MessageFocused) {
                    Modifier.onGloballyPositioned {
                        messagesState.calculateMessageOffset(parentSize, it.size)
                    }
                } else {
                    Modifier
                }

                Box(modifier = messageItemModifier) {
                    itemContent(item)

                    if (index == 0 && lazyListState.isScrollInProgress) {
                        onScrolledToBottom()
                    }

                    if (!endOfMessages &&
                        index == messages.lastIndex &&
                        messages.isNotEmpty() &&
                        lazyListState.isScrollInProgress
                    ) {
                        onMessagesStartReached()
                    }

                    val newestMessageItem = (messages.firstOrNull { it is MessageItemState } as? MessageItemState)
                    if (!startOfMessages &&
                        index == 0 &&
                        messages.isNotEmpty() &&
                        lazyListState.isScrollInProgress
                    ) {
                        newestMessageItem?.message?.id?.let(onMessagesEndReached)
                    }
                }
            }

            if (isLoadingMoreOldMessages && !endOfMessages) {
                item {
                    loadingMoreContent()
                }
            }
        }

        helperContent()
    }
}

/**
 * Used to hoist state in a way that defers reads to a lambda,
 * hence skipping unnecessary recomposition of the parent composable.
 */
@Composable
private fun OnLastVisibleItemChanged(lazyListState: LazyListState, onChanged: (firstVisibleItemIndex: Int) -> Unit) {
    onChanged(lazyListState.firstVisibleItemIndex)
}

/**
 * Represents the default scrolling behavior and UI for [Messages], based on the state of messages and the scroll state.
 *
 * @param messagesState The state of messages, current message list, thread, user and more.
 * @param lazyListState The scrolling state of the list, used to manipulate and trigger scroll events.
 * @param scrollToBottom Handler when the user requests to scroll to the bottom of the messages list.
 */
@Composable
internal fun BoxScope.DefaultMessagesHelperContent(
    messagesState: MessagesState,
    lazyListState: LazyListState,
    scrollToBottom: (() -> Unit) -> Unit,
) {
    val messages = messagesState.messageItems
    val newMessageState = messagesState.newMessageState
    val areNewestMessagesLoaded = messagesState.startOfMessages
    val isMessageInThread = messagesState.parentMessageId != null

    val coroutineScope = rememberCoroutineScope()

    val firstVisibleItemIndex = lazyListState.firstVisibleItemIndex

    val focusedItemIndex = messages.indexOfFirst { it is MessageItemState && it.focusState is MessageFocused }

    val offset by messagesState.focusedMessageOffset.collectAsState()

    LaunchedEffect(newMessageState, focusedItemIndex, offset) {

        if (focusedItemIndex != -1 &&
            !lazyListState.isScrollInProgress
        ) {
            coroutineScope.launch {
                lazyListState.scrollToItem(focusedItemIndex, offset ?: 0)
            }
        }

        val shouldScrollToBottomOnNewMessage = shouldScrollToBottomOnNewMessage(
            focusedItemIndex,
            firstVisibleItemIndex,
            newMessageState,
            areNewestMessagesLoaded,
            lazyListState.isScrollInProgress
        )

        if (shouldScrollToBottomOnNewMessage) {
            coroutineScope.launch {
                if (newMessageState == MyOwn && firstVisibleItemIndex > 5) {
                    lazyListState.scrollToItem(5)
                }
                lazyListState.animateScrollToItem(0)
            }
        }
    }

    if (isScrollToBottomButtonVisible(isMessageInThread, firstVisibleItemIndex, areNewestMessagesLoaded)) {
        MessagesScrollingOption(
            unreadCount = messagesState.unreadCount,
            modifier = Modifier.align(Alignment.BottomEnd),
            onClick = {
                scrollToBottom {
                    coroutineScope.launch {
                        if (firstVisibleItemIndex > 5) {
                            lazyListState.scrollToItem(5)
                        }
                        lazyListState.animateScrollToItem(0)
                    }
                }
            }
        )
    }
}

/**
 * Determines if the list should scroll to the bottom when a new message arrives. If we are focusing on an item we do
 * not wish to take the user off of it, if the newest messages are not loaded then the list will not scroll since the
 * new message will not be in the list and if the user has scrolled further in search of a certain part of messages
 * history we should not break that flow.
 *
 * @param focusedItemIndex The index of the currently focused item.
 * @param firstVisibleItemIndex The index of the first visible item in the messages list.
 * @param newMessageState The [NewMessageState] if a new message has arrived.
 * @param areNewestMessagesLoaded Whether the newest messages are loaded inside the list or not.
 * @param isScrollInProgress If the list is currently scrolling or not.
 *
 * @return Whether the list should scroll to the bottom when a new message arrives or not.
 */
private fun shouldScrollToBottomOnNewMessage(
    focusedItemIndex: Int,
    firstVisibleItemIndex: Int,
    newMessageState: NewMessageState?,
    areNewestMessagesLoaded: Boolean,
    isScrollInProgress: Boolean,
): Boolean {
    newMessageState ?: return false

    return focusedItemIndex == -1 &&
        !isScrollInProgress &&
        areNewestMessagesLoaded &&
        firstVisibleItemIndex < 3
}

/**
 * Determines whether the scroll to the bottom button should be visible or not.
 *
 * @param isInThread If we are currently in a thread or not.
 * @param firstVisibleItemIndex The index of the first visible item in the messages list.
 * @param areNewestMessagesLoaded Whether the newest messages are loaded inside the list or not.
 *
 * @return Whether the scroll to bottom button should be visible or not.
 */
private fun isScrollToBottomButtonVisible(
    isInThread: Boolean,
    firstVisibleItemIndex: Int,
    areNewestMessagesLoaded: Boolean,
): Boolean {
    return if (isInThread) isScrollToBottomButtonVisibleInThread(firstVisibleItemIndex) else
        isScrollToBottomButtonVisibleInMessageList(firstVisibleItemIndex, areNewestMessagesLoaded)
}

/**
 * Determines whether the scroll to bottom button should be visible if thread is currently showing.
 *
 * @param firstVisibleItemIndex The index of the first visible item in the messages list.
 *
 * @return Whether the scroll to bottom button should be visible inside a thread.
 */
private fun isScrollToBottomButtonVisibleInThread(firstVisibleItemIndex: Int): Boolean {
    return shouldScrollToBottomButtonBeVisibleAtIndex(firstVisibleItemIndex)
}

/**
 * Determines whether the scroll to bottom button should be visible if messages list is currently showing.
 *
 * @param firstVisibleItemIndex The index of the first visible item in the messages list.
 * @param areNewestMessagesLoaded Whether the newest messages are currently loaded inside the messages list or not.
 *
 * @return Whether the scroll to bottom button should be visible inside messages list or not.
 */
private fun isScrollToBottomButtonVisibleInMessageList(
    firstVisibleItemIndex: Int,
    areNewestMessagesLoaded: Boolean,
): Boolean {
    return shouldScrollToBottomButtonBeVisibleAtIndex(firstVisibleItemIndex) || !areNewestMessagesLoaded
}

/**
 * Determines whether the scroll to bottom button should be visible given the first visible index.
 *
 * @param firstVisibleItemIndex The index of the first visible item in the messages list.
 *
 * @return Whether the scroll to bottom button should be visible given the first visible item index.
 */
private fun shouldScrollToBottomButtonBeVisibleAtIndex(firstVisibleItemIndex: Int): Boolean {
    return abs(firstVisibleItemIndex) >= 3
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
