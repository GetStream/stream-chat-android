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

package io.getstream.chat.android.ui.widgets

import android.content.res.Resources
import androidx.core.widget.NestedScrollView

/**
 * Helper class for handling pagination with [NestedScrollView].
 * Triggers load more when the user scrolls near the bottom of the scroll view.
 *
 * @param loadMoreThresholdDp The distance in dp from the bottom where pagination should be triggered. Default is 200dp.
 * @param loadMoreListener The handler which is called when pagination should be triggered.
 * @param resources The resources used to convert dp to pixels.
 */
internal class NestedScrollViewPaginationHelper(
    private val loadMoreThresholdDp: Int = DEFAULT_LOAD_MORE_THRESHOLD_DP,
    private val loadMoreListener: () -> Unit,
    private val resources: Resources,
) {
    init {
        require(loadMoreThresholdDp >= 0) { "Load more threshold must not be negative" }
    }

    private var paginationEnabled: Boolean = false

    private val scrollChangeListener = NestedScrollView.OnScrollChangeListener { nestedScrollView, _, scrollY, _, _ ->
        if (!paginationEnabled) {
            return@OnScrollChangeListener
        }

        val totalHeight = nestedScrollView.getChildAt(0)?.height ?: 0
        val scrollViewHeight = nestedScrollView.height
        val scrollPosition = scrollY + scrollViewHeight
        val threshold = loadMoreThresholdDp * resources.displayMetrics.density

        if (totalHeight > 0 && (totalHeight - scrollPosition) < threshold) {
            nestedScrollView.post {
                if (paginationEnabled) {
                    loadMoreListener()
                }
            }
        }
    }

    /**
     * Attaches the scroll listener to the given [NestedScrollView].
     */
    fun attachTo(nestedScrollView: NestedScrollView) {
        nestedScrollView.setOnScrollChangeListener(scrollChangeListener)
    }

    /**
     * Detaches the scroll listener from the given [NestedScrollView].
     */
    fun detachFrom(nestedScrollView: NestedScrollView) {
        nestedScrollView.setOnScrollChangeListener(null as NestedScrollView.OnScrollChangeListener?)
    }

    /**
     * Manually enables pagination.
     */
    fun enablePagination() {
        paginationEnabled = true
    }

    /**
     * Manually disables pagination.
     */
    fun disablePagination() {
        paginationEnabled = false
    }

    private companion object {
        private const val DEFAULT_LOAD_MORE_THRESHOLD_DP = 200
    }
}
