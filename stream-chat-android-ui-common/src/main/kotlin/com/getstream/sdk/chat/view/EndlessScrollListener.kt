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

package com.getstream.sdk.chat.view

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

public class EndlessScrollListener(
    private val loadMoreThreshold: Int,
    private val loadMoreListener: () -> Unit,
) : RecyclerView.OnScrollListener() {

    init {
        require(loadMoreThreshold >= 0) { "Load more threshold must not be negative" }
    }

    private var paginationEnabled: Boolean = false
    private var scrollStateReset = true

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (!paginationEnabled) {
            return
        }

        val layoutManager = recyclerView.layoutManager
        if (layoutManager !is LinearLayoutManager) {
            throw IllegalStateException("EndlessScrollListener supports only LinearLayoutManager")
        }

        if (layoutManager.reverseLayout) {
            checkScrollDown(dy, layoutManager, recyclerView)
        } else {
            checkScrollUp(dy, layoutManager, recyclerView)
        }
    }

    private fun checkScrollUp(dy: Int, layoutManager: LinearLayoutManager, recyclerView: RecyclerView) {
        if (dy >= 0) {
            // Scrolling downwards
            return
        }

        handleScroll(layoutManager, recyclerView)
    }

    private fun checkScrollDown(dy: Int, layoutManager: LinearLayoutManager, recyclerView: RecyclerView) {
        if (dy <= 0) {
            // Scrolling upwards
            return
        }

        handleScroll(layoutManager, recyclerView)
    }

    private fun handleScroll(layoutManager: LinearLayoutManager, recyclerView: RecyclerView) {
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        if (scrollStateReset && firstVisiblePosition <= loadMoreThreshold) {
            scrollStateReset = false
            recyclerView.post {
                if (paginationEnabled) {
                    loadMoreListener()
                }
            }
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE ||
            newState == RecyclerView.SCROLL_STATE_DRAGGING
        ) {
            scrollStateReset = true
        }
    }

    public fun enablePagination() {
        paginationEnabled = true
    }

    public fun disablePagination() {
        paginationEnabled = false
    }
}
