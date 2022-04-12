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

package io.getstream.chat.ui.sample.util.extensions

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Automatically scrolls [RecyclerView] to the top when the first item is completely visible and
 * a new range of items is inserted above.
 */
internal fun RecyclerView.autoScrollToTop() {
    val layoutManager = layoutManager as? LinearLayoutManager
        ?: throw IllegalStateException("Auto scroll only works with LinearLayoutManager")
    val adapter = adapter
        ?: throw IllegalStateException("Adapter must be set in order for auto scroll to work")

    adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            if (positionStart == 0 && positionStart == layoutManager.findFirstCompletelyVisibleItemPosition()) {
                layoutManager.scrollToPosition(0)
            }
        }
    })
}
