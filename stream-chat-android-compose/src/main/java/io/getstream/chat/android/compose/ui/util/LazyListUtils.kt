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

package io.getstream.chat.android.compose.ui.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Creates and remembers a [LazyListState] that automatically scrolls to the last item
 * whenever [itemCount] increases.
 *
 * Useful for lists where new items are appended and should be immediately visible
 * (e.g. attachment preview strips in the message composer).
 *
 * @param itemCount The current number of items in the list.
 */
@Composable
internal fun rememberAutoScrollLazyListState(itemCount: Int): LazyListState {
    val state = rememberLazyListState()
    var previousCount by remember { mutableIntStateOf(itemCount) }
    LaunchedEffect(itemCount) {
        if (itemCount > previousCount) {
            state.animateScrollToItem(itemCount - 1)
        }
        previousCount = itemCount
    }
    return state
}
