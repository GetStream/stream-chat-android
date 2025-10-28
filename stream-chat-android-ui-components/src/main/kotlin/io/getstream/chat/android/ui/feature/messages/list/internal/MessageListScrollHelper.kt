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

package io.getstream.chat.android.ui.feature.messages.list.internal

import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.extensions.internal.safeCast
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.messages.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.internal.MessageListItemAdapter
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.getFragmentManager
import io.getstream.log.taggedLogger
import kotlin.properties.Delegates

internal class MessageListScrollHelper(
    private val recyclerView: RecyclerView,
    private val scrollButtonView: ScrollButtonView,
    private val disableScrollWhenShowingDialog: Boolean,
    private val callback: MessageReadListener,
) {

    private val logger by taggedLogger("MessageListScrollHelper")

    internal var alwaysScrollToBottom: Boolean by Delegates.notNull()
    internal var scrollToBottomButtonEnabled: Boolean by Delegates.notNull()

    private val layoutManager: LinearLayoutManager
        get() = recyclerView.layoutManager as LinearLayoutManager
    private val adapter: MessageListItemAdapter
        get() = recyclerView.adapter as MessageListItemAdapter

    private var onScrollToBottomHandler: MessageListView.OnScrollToBottomHandler =
        MessageListView.OnScrollToBottomHandler {
            recyclerView.scrollToPosition(currentList.lastIndex)
        }

    internal var unreadCountEnabled: Boolean = true

    private var endOfNewMessagesReached: Boolean = true

    private var bottomOffset: Int = 0

    /**
     * True when the latest message is visible.
     *
     * Note: This does not mean the whole message is visible,
     * it will be true even if only a part of it is.
     */
    private var isAtBottom = false
        set(value) {
            logger.v { "[setIsAtBottom] value: $value" }
            if (value && !field) {
                callback.onLastMessageRead()
            }
            field = value
        }

    private val currentList: List<MessageListItem>
        get() {
            return adapter.currentList
        }

    init {
        scrollButtonView.setOnClickListener {
            onScrollToBottomHandler.onScrollToBottom()
        }
        recyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {

                /**
                 * Checks if we currently have a popup shown over the list.
                 *
                 * @param recyclerView The list that we're observing.
                 * @param newState The scroll state of the list.
                 */
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    logger.d { "[onScrollStateChanged] newState: $newState" }
                    super.onScrollStateChanged(recyclerView, newState)

                    if (disableScrollWhenShowingDialog) {
                        stopScrollIfPopupShown(recyclerView)
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    logger.d {
                        "[onScrolled] scrollToBottomButtonEnabled: $scrollToBottomButtonEnabled" +
                            ", currentList.size: ${currentList.size}"
                    }
                    if (!scrollToBottomButtonEnabled || currentList.isEmpty()) {
                        return
                    }
                    val shouldScrollToBottomBeVisible = shouldScrollToBottomBeVisible()
                    logger.v { "[onScrolled] shouldScrollToBottomBeVisible: $shouldScrollToBottomBeVisible" }
                    scrollButtonView.isVisible = shouldScrollToBottomBeVisible
                }
            },
        )
    }

    /**
     * Calculates the bottom offset by comparing the position of the last visible item
     * with the position of the last potentially visible item.
     *
     * @return The bottom offset.
     */
    private fun calculateBottomOffset(): Int {
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        val lastPotentiallyVisibleItemPosition = currentList.indexOfLast { it.isValid() }
        logger.v {
            "[calculateBottomOffset] lastVisibleItemPosition: $lastVisibleItemPosition, " +
                "lastPotentiallyVisibleItemPosition: $lastPotentiallyVisibleItemPosition"
        }
        return lastPotentiallyVisibleItemPosition - lastVisibleItemPosition
    }

    /**
     * Determines whether the scroll to bottom button should be visible or not.
     *
     * @return Whether the scroll to bottom button should be visible or not.
     */
    private fun shouldScrollToBottomBeVisible(): Boolean {
        bottomOffset = calculateBottomOffset()
        logger.d {
            "[shouldScrollToBottomBeVisible] bottomOffset: $bottomOffset" +
                ", endOfNewMessagesReached: $endOfNewMessagesReached"
        }
        isAtBottom = bottomOffset <= 0 && endOfNewMessagesReached

        return when {
            adapter.itemCount == 0 -> false
            !endOfNewMessagesReached -> true
            else -> {
                val hasInvisibleUnreadMessage = !isAtBottom
                val hasScrolledUpEnough = bottomOffset > SCROLL_BUTTON_VISIBILITY_THRESHOLD

                hasInvisibleUnreadMessage || hasScrolledUpEnough
            }
        }
    }

    /**
     * Checks if we have any popups shown over the list and stops scrolling in case we do.
     *
     * @param recyclerView The list that's being observed for long taps and scrolling.
     */
    private fun stopScrollIfPopupShown(recyclerView: RecyclerView) {
        val fragmentManager = recyclerView.context.getFragmentManager() ?: return
        val hasDialogsShown = fragmentManager.fragments.any { it is DialogFragment }

        if (hasDialogsShown) {
            recyclerView.stopScroll()
        }
    }

    internal fun scrollToMessage(message: Message) {
        recyclerView.postDelayed(
            {
                currentList.indexOfFirst { it is MessageListItem.MessageItem && it.message.id == message.id }
                    .takeIf { it >= 0 }
                    ?.let {
                        if (message.pinned) {
                            this@MessageListScrollHelper.layoutManager
                                .scrollToPositionWithOffset(it, 8.dpToPx())
                        } else {
                            with(recyclerView) {
                                this@MessageListScrollHelper.layoutManager
                                    .scrollToPositionWithOffset(it, height / 3)
                                post {
                                    findViewHolderForAdapterPosition(it)
                                        ?.safeCast<BaseMessageItemViewHolder<*>>()
                                        ?.startHighlightAnimation()
                                }
                            }
                        }

                        if (it > SCROLL_BUTTON_VISIBILITY_THRESHOLD) scrollButtonView.isVisible = true
                    }
            },
            HIGHLIGHT_MESSAGE_DELAY,
        )
    }

    internal fun scrollToBottom() {
        recyclerView.scrollToPosition(currentList.lastIndex)
    }

    internal fun onMessageListChanged(
        isThreadStart: Boolean,
        hasNewMessages: Boolean,
        isInitialList: Boolean,
        endOfNewMessagesReached: Boolean,
    ) {
        val isAtBottom = isAtBottom
        logger.d {
            "[onMessageListChanged] isInitialList: $isInitialList, hasNewMessages: $hasNewMessages" +
                ", endOfNewMessagesReached: $endOfNewMessagesReached, isAtBottom: $isAtBottom"
        }
        this.endOfNewMessagesReached = endOfNewMessagesReached
        scrollButtonView.isVisible = shouldScrollToBottomBeVisible()

        if (!isThreadStart && shouldKeepScrollPosition(endOfNewMessagesReached, hasNewMessages)) {
            return
        }

        if (isThreadStart) {
            layoutManager.scrollToPosition(currentList.lastIndex)
            return
        }
        val shouldScrollToBottom = shouldScrollToBottom(
            isInitialList,
            endOfNewMessagesReached,
            hasNewMessages,
            isAtBottom,
        )
        logger.v { "[onMessageListChanged] shouldScrollToBottom: $shouldScrollToBottom" }
        if (shouldScrollToBottom) {
            layoutManager.scrollToPosition(currentList.lastIndex)
            callback.onLastMessageRead()
        }
    }

    private fun shouldKeepScrollPosition(
        areNewestMessagesLoaded: Boolean,
        hasNewMessages: Boolean,
    ): Boolean = !areNewestMessagesLoaded || !scrollToBottomButtonEnabled ||
        (!hasNewMessages || adapter.currentList.isEmpty())

    private fun shouldScrollToBottom(
        isInitialList: Boolean,
        endOfNewMessagesReached: Boolean,
        hasNewMessages: Boolean,
        isAtBottom: Boolean,
    ): Boolean {
        logger.v {
            "[shouldScrollToBottom] hasNewMessages: $hasNewMessages" +
                ", endOfNewMessagesReached: $endOfNewMessagesReached, isInitialList: $isInitialList" +
                ", isLastMessageMine: ${isLastMessageMine()}" +
                ", isAtBottom: $isAtBottom, alwaysScrollToBottom: $alwaysScrollToBottom"
        }
        return hasNewMessages &&
            endOfNewMessagesReached &&
            (isInitialList || isLastMessageMine() || isAtBottom || alwaysScrollToBottom)
    }

    private fun isLastMessageMine(): Boolean = currentList
        .lastOrNull()
        ?.safeCast<MessageListItem.MessageItem>()
        ?.isMine
        ?: false

    private fun MessageListItem.isValid(): Boolean = (this is MessageListItem.MessageItem && !(this.isTheirs && this.message.isDeleted())) ||
        (this is MessageListItem.ThreadSeparatorItem)

    internal fun setScrollToBottomHandler(onScrollToBottomHandler: MessageListView.OnScrollToBottomHandler) {
        this.onScrollToBottomHandler = onScrollToBottomHandler
    }

    internal fun interface MessageReadListener {
        fun onLastMessageRead()
    }

    private companion object {
        private const val HIGHLIGHT_MESSAGE_DELAY = 100L
        private const val SCROLL_BUTTON_VISIBILITY_THRESHOLD = 8
    }
}
