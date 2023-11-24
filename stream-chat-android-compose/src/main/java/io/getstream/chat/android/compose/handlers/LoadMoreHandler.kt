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

package io.getstream.chat.android.compose.handlers

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Handler to be used with [LazyColumn] to implement infinite scroll.
 *
 * @param listState The state of the list used to control scrolling.
 * @param loadMoreThreshold The number if items before the end of the list.
 * @param channelCount Total channel count (optional).
 * @param loadMore Handler for load more action.
 */
@Composable
public fun LoadMoreHandler(
    listState: LazyListState,
    loadMoreThreshold: Int = 3,
    channelCount: Int? = null,
    loadMore: () -> Unit,
) {
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false

            val result = lastVisibleItem.index > (totalItemsCount - loadMoreThreshold - 1)

            if (channelCount == null) {
                // Return the result, there is no channel count provided
                return@derivedStateOf result
            }

            val visibleItemsCount = listState.layoutInfo.visibleItemsInfo.size
            if (channelCount <= visibleItemsCount) {
                false
            } else {
                result
            }
        }
    }

    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .filter { shouldLoad -> shouldLoad }
            .collect {
                loadMore()
            }
    }
}
