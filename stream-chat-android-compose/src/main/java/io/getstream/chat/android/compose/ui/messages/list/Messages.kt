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
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.handlers.LoadMoreHandler
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.list.HasMessageListItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageFocused
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListState
import io.getstream.chat.android.ui.common.state.messages.list.NewMessageState
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
 * @param messagesLazyListState State of the lazy list that represents the list of messages. Useful for controlling the
 * scroll state and focused message offset.
 * @param verticalArrangement Vertical arrangement of the regular message list.
 * Default: [Arrangement.Top].
 * @param threadMessagesStart Thread messages start at the bottom or top of the screen.
 * Default: [ThreadMessagesStart.BOTTOM].
 * @param onMessagesStartReached Handler for pagination, when the user reaches chronologically the start of messages.
 * @param onLastVisibleMessageChanged Handler that notifies us when the user scrolls and the last visible message
 * changes.
 * @param onScrolledToBottom Handler when the user reaches the bottom of the list.
 * @param onMessagesEndReached Handler for pagination, when the user reaches chronologically the end of messages.
 * @param onScrollToBottom Handler when the user requests to scroll to the bottom of the messages list.
 * @param modifier Modifier for styling.
 * @param contentPadding Padding values to be applied to the message list surrounding the content inside.
 * @param helperContent Composable that, by default, represents the helper content featuring scrolling behavior based
 * on the list state.
 * @param loadingMoreContent Composable that represents the loading more content, when we're loading the next page.
 * @param itemModifier Modifier for styling the message item container.
 * @param itemContent Composable that represents the item that displays each message.
 */
@Composable
@Suppress("LongParameterList", "LongMethod", "ComplexMethod")
public fun Messages(
    messagesState: MessageListState,
    messagesLazyListState: MessagesLazyListState,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    threadMessagesStart: ThreadMessagesStart = ThreadMessagesStart.BOTTOM,
    onMessagesStartReached: () -> Unit,
    onLastVisibleMessageChanged: (Message) -> Unit,
    onScrolledToBottom: () -> Unit,
    onMessagesEndReached: (String) -> Unit,
    onScrollToBottom: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    helperContent: @Composable BoxScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageListHelperContent(
                messageListState = messagesState,
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
    itemContent: @Composable LazyItemScope.(MessageListItemState) -> Unit,
) {
    val lazyListState = messagesLazyListState.lazyListState
    val messages = messagesState.messageItems
    val endOfMessages = messagesState.endOfOldMessagesReached
    val startOfMessages = messagesState.endOfNewMessagesReached
    val isLoadingMoreNewMessages = messagesState.isLoadingNewerMessages
    val isLoadingMoreOldMessages = messagesState.isLoadingOlderMessages

    val density = LocalDensity.current

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .testTag("Stream_Messages")
                .fillMaxSize()
                .onSizeChanged {
                    val bottomPadding = contentPadding.calculateBottomPadding()
                    val topPadding = contentPadding.calculateTopPadding()

                    val paddingPixels = with(density) {
                        bottomPadding.roundToPx() + topPadding.roundToPx()
                    }

                    val parentSize = IntSize(
                        width = it.width,
                        height = it.height + paddingPixels,
                    )
                    messagesLazyListState.updateParentSize(parentSize)
                },
            state = lazyListState,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = calculateVerticalArrangement(messagesState, verticalArrangement, threadMessagesStart),
            reverseLayout = true,
            contentPadding = contentPadding,
        ) {
            if (isLoadingMoreNewMessages && !startOfMessages) {
                item {
                    loadingMoreContent()
                }
            }

            itemsIndexed(
                items = messages,
                key = { _, item ->
                    if (item is HasMessageListItemState) item.message.id else item.toString()
                },
            ) { index, item ->
                val messageItemModifier =
                    if (item is MessageItemState && item.focusState == MessageFocused) {
                        Modifier.onSizeChanged {
                            messagesLazyListState.updateFocusedMessageSize(it)
                        }
                    } else {
                        Modifier
                    }

                val itemModifier = itemModifier(index, item)
                val finalItemModifier = messageItemModifier.then(itemModifier)
                Box(modifier = finalItemModifier) {
                    itemContent(item)
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

    // reverseLayout influences the scrolling behavior.
    // When reverseLayout is true, canScrollBackward is false when the list is scrolled to the bottom.
    val isScrolledToBottom by remember { derivedStateOf { !lazyListState.canScrollBackward } }
    LaunchedEffect(isScrolledToBottom) {
        if (isScrolledToBottom) {
            onScrolledToBottom()
        }
    }

    LoadMoreHandler(lazyListState = lazyListState) {
        if (!endOfMessages) {
            onMessagesStartReached()
        }
    }

    val isMessagesEndReached by remember {
        derivedStateOf {
            !startOfMessages &&
                lazyListState.firstVisibleItemIndex == 0 &&
                lazyListState.isScrollInProgress
        }
    }
    LaunchedEffect(isMessagesEndReached) {
        if (isMessagesEndReached) {
            val newestMessageItem = messages.firstOrNull { item -> item is MessageItemState } as? MessageItemState
            newestMessageItem?.message?.id?.let {
                onMessagesEndReached(it)
            }
        }
    }

    /** Marks the bottom most item as read every time it changes. **/
    OnLastVisibleItemChanged(lazyListState) { messageIndex ->
        val message = messagesState.messageItems.getOrNull(messageIndex)

        if (message is HasMessageListItemState) {
            onLastVisibleMessageChanged(message.message)
        }
    }
}

/**
 * Used to get an [Arrangement.Vertical] instance that represents the
 * vertical arrangement of the messages based on the current state.
 *
 * @param messagesState A [MessageListState] instance that represents the current state of the messages.
 * @param messagesVerticalArrangement Indicator from where the regular messages should start.
 * @param threadMessagesStart Indicator from where the thread messages should start.
 * @return An [Arrangement.Vertical] instance that represents the vertical arrangement on the current
 * [MessageListState].
 */
private fun calculateVerticalArrangement(
    messagesState: MessageListState,
    messagesVerticalArrangement: Arrangement.Vertical,
    threadMessagesStart: ThreadMessagesStart,
): Arrangement.Vertical {
    val isInThread = messagesState.parentMessageId != null
    return if (isInThread) {
        when (threadMessagesStart) {
            ThreadMessagesStart.BOTTOM -> Arrangement.Bottom
            ThreadMessagesStart.TOP -> Arrangement.Top
        }
    } else {
        messagesVerticalArrangement
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
 * @param messagesLazyListState The scrolling state of the list, used to manipulate and trigger scroll events.
 * @param scrollToBottom Handler when the user requests to scroll to the bottom of the messages list.
 */
@Composable
internal fun BoxScope.DefaultMessagesHelperContent(
    messagesState: MessageListState,
    messagesLazyListState: MessagesLazyListState,
    scrollToBottom: (() -> Unit) -> Unit,
) {
    val lazyListState = messagesLazyListState.lazyListState

    val messages = messagesState.messageItems
    val newMessageState = messagesState.newMessageState
    val areNewestMessagesLoaded = messagesState.endOfNewMessagesReached
    val isMessageInThread = messagesState.parentMessageId != null

    val coroutineScope = rememberCoroutineScope()

    val firstVisibleItemIndex by remember(messages) { derivedStateOf { lazyListState.firstVisibleItemIndex } }

    val focusedItemIndex = remember(messages) {
        messages.indexOfFirst { item -> item is MessageItemState && item.focusState is MessageFocused }
    }

    val offset = messagesLazyListState.focusedMessageOffset

    LaunchedEffect(focusedItemIndex, offset) {
        if (focusedItemIndex != -1 &&
            !lazyListState.isScrollInProgress
        ) {
            lazyListState.scrollToItem(focusedItemIndex, offset)
        }
    }

    LaunchedEffect(newMessageState) {
        val shouldScrollToBottom = shouldScrollToBottomOnNewMessage(
            focusedItemIndex,
            firstVisibleItemIndex,
            newMessageState,
            areNewestMessagesLoaded,
            lazyListState.isScrollInProgress,
        )

        if (shouldScrollToBottom) {
            lazyListState.animateScrollToItem(0)
        }
    }

    val scrollToBottomButtonVisible = isScrollToBottomButtonVisible(
        isMessageInThread,
        firstVisibleItemIndex,
        areNewestMessagesLoaded,
    )
    with(ChatTheme.componentFactory) {
        ScrollToBottomButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            visible = scrollToBottomButtonVisible,
            count = messagesState.unreadCount,
            onClick = {
                scrollToBottom {
                    coroutineScope.launch {
                        lazyListState.scrollToItem(0)
                    }
                }
            },
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
    return if (isInThread) {
        isScrollToBottomButtonVisibleInThread(firstVisibleItemIndex)
    } else {
        isScrollToBottomButtonVisibleInMessageList(firstVisibleItemIndex, areNewestMessagesLoaded)
    }
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
            .padding(8.dp),
    )
}
