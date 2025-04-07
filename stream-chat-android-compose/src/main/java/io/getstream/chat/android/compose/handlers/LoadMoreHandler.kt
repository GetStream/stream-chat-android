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

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Handler to notify more items should be loaded when the user scrolls to the end of the list.
 *
 * @param listState The state of the list used to control scrolling.
 * @param loadMoreThreshold The number if items before the end of the list. Default is half of the visible items.
 * @param loadMore Handler for load more action.
 */
@Composable
public fun LoadMoreHandler(
    listState: LazyListState,
    loadMoreThreshold: () -> Int = { listState.layoutInfo.visibleItemsInfo.size / 2 },
    loadMore: () -> Unit,
) {
    LaunchedEffect(listState) {
        snapshotFlow {
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            shouldLoadMore(
                totalItemsCount = totalItemsCount,
                lastVisibleItemIndex = lastVisibleItemIndex,
                loadMoreThreshold = loadMoreThreshold(),
            )
        }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore) {
                    loadMore()
                }
            }
    }
}

@VisibleForTesting
internal fun shouldLoadMore(
    totalItemsCount: Int,
    lastVisibleItemIndex: Int,
    loadMoreThreshold: Int,
): Boolean =
    totalItemsCount > 0 && // List isn’t empty
        totalItemsCount > loadMoreThreshold && // Ensure list is large enough
        lastVisibleItemIndex > totalItemsCount - loadMoreThreshold - 1
