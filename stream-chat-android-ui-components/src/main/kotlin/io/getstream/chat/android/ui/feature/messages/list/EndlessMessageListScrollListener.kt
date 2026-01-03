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

package io.getstream.chat.android.ui.feature.messages.list

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.log.StreamLog

/**
 * Scroll listener which checks the layout manager of the MessageListView, listens for scrolling gestures
 * and triggers pagination when reaching the end top of the list.
 *
 * @param loadMoreThreshold The number of items or positions ahead of the end of the list where we can trigger the
 * pagination.
 * @param loadMoreAtTopListener The handler which is called when pagination should be triggered in the top direction.
 * @param loadMoreAtBottomListener The handler which is called when pagination should be triggered in the
 * bottom direction.
 */
public class EndlessMessageListScrollListener(
    private val loadMoreThreshold: Int,
    private val loadMoreAtTopListener: () -> Unit,
    private val loadMoreAtBottomListener: () -> Unit,
) : RecyclerView.OnScrollListener() {

    init {
        require(loadMoreThreshold >= 0) { "Load more threshold must not be negative" }
    }

    /**
     * Helper flag which marks the state if we should disable pagination.
     */
    private var paginationEnabled: Boolean = false

    private var shouldFetchBottomMessages: Boolean = false

    /**
     * Helper flag which marks  if we should wait for the scroll state reset.
     */
    private var scrollStateReset: Boolean = true

    public fun fetchAtBottom(shouldFetch: Boolean) {
        this.shouldFetchBottomMessages = shouldFetch
    }

    /**
     * Whenever we scroll, if the pagination is enabled, we check the scroll direction and validity.
     */
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (!paginationEnabled) {
            return
        }

        val layoutManager = recyclerView.layoutManager
        if (layoutManager !is LinearLayoutManager) {
            throw IllegalStateException("EndlessScrollListener supports only LinearLayoutManager")
        }

        handleScroll(dy, layoutManager, recyclerView)
    }

    /**
     * Checks if the scroll is going up or down and if the threshold number of items has been shown. If
     * [EndlessMessageListScrollListener] is configured to fetch bottom messages, it handles it when scrolling down.
     */
    private fun handleScroll(dy: Int, layoutManager: LinearLayoutManager, recyclerView: RecyclerView) {
        if (!paginationEnabled) return

        when {
            dy >= 0 && shouldFetchBottomMessages -> {
                handleScrollDown(layoutManager, recyclerView)
            }

            dy < 0 -> {
                handleScrollUp(layoutManager, recyclerView)
            }
        }
    }

    /**
     * Handles a valid scroll down. If the threshold has been met and the scroll state has been reset previously, we
     * trigger pagination.
     */
    private fun handleScrollDown(layoutManager: LinearLayoutManager, recyclerView: RecyclerView) {
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
        val total = recyclerView.adapter?.itemCount ?: 0
        if (DEBUG) {
            StreamLog.v(TAG) {
                "[handleScrollDown] lastVisiblePosition: $lastVisiblePosition, " +
                    "total: $total, loadMoreThreshold: $loadMoreThreshold, scrollStateReset: $scrollStateReset"
            }
        }
        if (scrollStateReset && lastVisiblePosition > total - loadMoreThreshold) {
            scrollStateReset = false
            recyclerView.post(loadMoreAtBottomListener)
        }
    }

    /**
     * Handles a valid scroll up. If the threshold has been met and the scroll state has been reset previously, we
     * trigger pagination.
     */
    private fun handleScrollUp(layoutManager: LinearLayoutManager, recyclerView: RecyclerView) {
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()

        if (scrollStateReset && firstVisiblePosition <= loadMoreThreshold) {
            scrollStateReset = false
            recyclerView.post(loadMoreAtTopListener)
        }
    }

    /**
     * Handles scroll state changes where it waits for a state reset and new scroll gestures.
     */
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE ||
            newState == RecyclerView.SCROLL_STATE_DRAGGING
        ) {
            scrollStateReset = true
        }
    }

    /**
     * Manually enables pagination in certain UI components and events.
     */
    public fun enablePagination() {
        paginationEnabled = true
    }

    /**
     * Manually disables pagination in certain UI components and events.
     */
    public fun disablePagination() {
        paginationEnabled = false
    }

    private companion object {
        private const val TAG = "Chat:EndlessMessageScroll"
        private const val DEBUG = false
    }
}
