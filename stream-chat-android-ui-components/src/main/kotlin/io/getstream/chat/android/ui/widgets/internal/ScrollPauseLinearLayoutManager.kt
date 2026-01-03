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

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * Exposes an API for pausing scroll functionality. Primary use-case is for pausing
 * a RecyclerView's ability to scroll when we are swiping items.
 */
internal class ScrollPauseLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
    var verticalScrollEnabled: Boolean = orientation == VERTICAL
    var horizontalScrollEnabled: Boolean = orientation == HORIZONTAL

    override fun canScrollVertically(): Boolean {
        return verticalScrollEnabled && super.canScrollVertically()
    }

    override fun canScrollHorizontally(): Boolean {
        return horizontalScrollEnabled && super.canScrollHorizontally()
    }
}
