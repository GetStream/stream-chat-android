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

package io.getstream.chat.android.ui.widgets.internal

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Automatically scrolls [RecyclerView] to the top when the first item is completely visible and
 * a new range of items is inserted or moved to the position above.
 */
internal class SnapToTopDataObserver(
    private val recyclerView: RecyclerView,
) : RecyclerView.AdapterDataObserver() {

    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
        ?: throw IllegalStateException("Auto scroll only works with LinearLayoutManager")

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        autoScrollToTopIfNecessary(minOf(fromPosition, toPosition))
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        autoScrollToTopIfNecessary(positionStart)
    }

    /**
     * Scrolls the list to the top if the user is is not dragging it
     * and the list is scrolled to the top prior to the update.
     */
    private fun autoScrollToTopIfNecessary(itemPosition: Int) {
        if (
            itemPosition != 0 ||
            recyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE ||
            recyclerView.canScrollVertically(-1)
        ) {
            return
        }

        if (layoutManager.findFirstVisibleItemPosition() == 0) {
            layoutManager.scrollToPosition(0)
        }
    }
}
