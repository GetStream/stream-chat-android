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

package io.getstream.chat.android.compose.handlers

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Handler to notify more items should be loaded when the user scrolls to the end of the list.
 *
 * @param listState The state of the list used to control scrolling.
 * @param loadMoreThreshold The number if items before the end of the list. Default is 3.
 * @param loadMore Handler for load more action.
 */
@Deprecated(
    message = "This function is deprecated. Use the one with a lambda for loadMoreThreshold instead.",
    replaceWith = ReplaceWith(
        expression = "LoadMoreHandler(listState, { loadMoreThreshold }, loadMore)",
        imports = ["io.getstream.chat.android.compose.handlers.LoadMoreHandler"],
    ),
)
@Composable
public fun LoadMoreHandler(
    listState: LazyListState,
    loadMoreThreshold: Int = DefaultLoadMoreThreshold,
    loadMore: () -> Unit,
) {
    LoadMoreHandler(
        lazyListState = listState,
        threshold = { loadMoreThreshold },
        loadMore = loadMore,
    )
}

/**
 * Handler to notify that more items should be loaded when the user scrolls to the end of the list.
 *
 * @param lazyListState The [LazyListState] used to control scrolling.
 * @param threshold The number if items to check before reaching the end of the list. Default is 3.
 * @param loadMore The callback to load more items.
 */
@Composable
public fun LoadMoreHandler(
    lazyListState: LazyListState,
    threshold: () -> Int = { DefaultLoadMoreThreshold },
    loadMore: () -> Unit,
) {
    LaunchedEffect(lazyListState) {
        snapshotFlow {
            val layoutInfo = lazyListState.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            shouldLoadMore(
                totalItemsCount = totalItemsCount,
                lastVisibleItemIndex = lastVisibleItemIndex,
                loadMoreThreshold = threshold(),
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

/**
 * Handler to notify that more items should be loaded when the user scrolls to the end of the grid.
 *
 * @param lazyGridState The [LazyGridState] used to control scrolling.
 * @param threshold The number if items to check before reaching the end of the grid. Default is 3.
 * @param loadMore The callback to load more items.
 */
@Composable
public fun LoadMoreHandler(
    lazyGridState: LazyGridState,
    threshold: () -> Int = { DefaultLoadMoreThreshold },
    loadMore: () -> Unit,
) {
    LaunchedEffect(lazyGridState) {
        snapshotFlow {
            val layoutInfo = lazyGridState.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            shouldLoadMore(
                totalItemsCount = totalItemsCount,
                lastVisibleItemIndex = lastVisibleItemIndex,
                loadMoreThreshold = threshold(),
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

/**
 * Handler to notify that more items should be loaded when the user scrolls to the end of the pager.
 *
 * @param pagerState The [PagerState] used to control scrolling.
 * @param pageCount The total number of pages.
 * @param threshold The number if items to check before reaching the end of the pager. Default is 3.
 * @param loadMore The callback to load more items.
 */
@Composable
public fun LoadMoreHandler(
    pagerState: PagerState,
    pageCount: () -> Int,
    threshold: () -> Int = { DefaultLoadMoreThreshold },
    loadMore: () -> Unit,
) {
    LaunchedEffect(pagerState) {
        snapshotFlow {
            val layoutInfo = pagerState.layoutInfo
            val lastVisibleItemIndex = layoutInfo.visiblePagesInfo.lastOrNull()?.index ?: -1
            shouldLoadMore(
                totalItemsCount = pageCount(),
                lastVisibleItemIndex = lastVisibleItemIndex,
                loadMoreThreshold = threshold(),
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

private const val DefaultLoadMoreThreshold = 3
