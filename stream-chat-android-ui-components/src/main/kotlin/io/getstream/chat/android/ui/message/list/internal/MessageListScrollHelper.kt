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

package io.getstream.chat.android.ui.message.list.internal

import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.getFragmentManager
import io.getstream.chat.android.ui.common.extensions.internal.safeCast
import io.getstream.chat.android.ui.common.extensions.isDeleted
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.internal.MessageListItemAdapter
import kotlin.math.max
import kotlin.properties.Delegates

internal class MessageListScrollHelper(
    private val recyclerView: RecyclerView,
    private val scrollButtonView: ScrollButtonView,
    private val callback: MessageReadListener,
) {
    internal var alwaysScrollToBottom: Boolean by Delegates.notNull()
    internal var scrollToBottomButtonEnabled: Boolean by Delegates.notNull()

    private val layoutManager: LinearLayoutManager
        get() = recyclerView.layoutManager as LinearLayoutManager
    private val adapter: MessageListItemAdapter
        get() = recyclerView.adapter as MessageListItemAdapter

    private var lastSeenMessageInChannel: MessageListItem? = null
    private var lastSeenMessageInThread: MessageListItem? = null
    private var isAtBottom = false
    private var unreadCount = 0

    private val currentList: List<MessageListItem>
        get() {
            return adapter.currentList
        }

    init {
        scrollButtonView.setOnClickListener {
            recyclerView.scrollToPosition(currentList.lastIndex)
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
                    super.onScrollStateChanged(recyclerView, newState)

                    stopScrollIfPopupShown(recyclerView)
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (!scrollToBottomButtonEnabled || currentList.isEmpty()) {
                        return
                    }

                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val lastPotentiallyVisibleItemPosition = currentList.indexOfLast { it.isValid() }
                    val bottomOffset = lastPotentiallyVisibleItemPosition - lastVisibleItemPosition
                    isAtBottom = bottomOffset == 0

                    max(lastVisibleItemPosition, getLastSeenItemPosition())
                        .coerceIn(0, currentList.lastIndex)
                        .let { currentList[it] }.let {
                            when {
                                adapter.isThread -> lastSeenMessageInThread = it
                                else -> lastSeenMessageInChannel = it
                            }
                        }

                    if (unreadCount > 0) {
                        refreshUnreadCount()
                    }

                    val hasInvisibleUnreadMessage = unreadCount > 0 && !isAtBottom
                    val hasScrolledUpEnough = bottomOffset > SCROLL_BUTTON_VISIBILITY_THRESHOLD
                    if (hasInvisibleUnreadMessage || hasScrolledUpEnough) {
                        scrollButtonView.setUnreadCount(unreadCount)
                        scrollButtonView.isVisible = true
                    } else {
                        scrollButtonView.isVisible = false
                    }
                }
            }
        )
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
                                ?.scrollToPositionWithOffset(it, 8.dpToPx())
                        } else {
                            with(recyclerView) {
                                this@MessageListScrollHelper.layoutManager
                                    ?.scrollToPositionWithOffset(it, height / 3)
                                post {
                                    findViewHolderForAdapterPosition(it)
                                        ?.safeCast<BaseMessageItemViewHolder<*>>()
                                        ?.startHighlightAnimation()
                                }
                            }
                        }
                    }
            },
            HIGHLIGHT_MESSAGE_DELAY
        )
    }

    internal fun onMessageListChanged(isThreadStart: Boolean, hasNewMessages: Boolean, isInitialList: Boolean) {
        if (!scrollToBottomButtonEnabled || (!hasNewMessages || adapter.currentList.isEmpty())) {
            return
        }

        if (isThreadStart) {
            layoutManager.scrollToPosition(0)
        } else if (isInitialList || isLastMessageMine() || isAtBottom || alwaysScrollToBottom) {
            layoutManager.scrollToPosition(currentList.lastIndex)
            callback.onLastMessageRead()
        } else {
            refreshUnreadCount()
            scrollButtonView.setUnreadCount(unreadCount)
            scrollButtonView.isVisible = true
        }
    }

    private fun refreshUnreadCount() {
        var unreadCount = 0
        for (i in currentList.lastIndex downTo getLastSeenItemPosition() + 1) {
            if (currentList[i].isValid()) {
                unreadCount++
            }
        }
        this.unreadCount = unreadCount
    }

    private fun getLastSeenItemPosition(): Int {
        val lastMessageId = if (adapter.isThread) {
            lastSeenMessageInThread
        } else {
            lastSeenMessageInChannel
        }?.getStableId()
        return currentList.indexOfLast { it.getStableId() == lastMessageId }
    }

    private fun isLastMessageMine(): Boolean {
        return currentList
            .lastOrNull()
            ?.safeCast<MessageListItem.MessageItem>()
            ?.isMine
            ?: false
    }

    private fun MessageListItem.isValid(): Boolean {
        return this is MessageListItem.MessageItem && !(this.isTheirs && this.message.isDeleted())
    }

    internal fun interface MessageReadListener {
        fun onLastMessageRead()
    }

    private companion object {
        private const val HIGHLIGHT_MESSAGE_DELAY = 100L
        private const val SCROLL_BUTTON_VISIBILITY_THRESHOLD = 8
    }
}
