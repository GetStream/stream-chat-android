package io.getstream.chat.android.compose.handlers

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Handler to be used with [LazyColumn] to implement infinite scroll.
 *
 * @param listState The state of the list used to control scrolling.
 * @param loadMoreThreshold The number if items before the end of the list.
 * @param loadMore Handler for load more action.
 */
@Composable
public fun LoadMoreHandler(
    listState: LazyListState,
    loadMoreThreshold: Int = 3,
    loadMore: () -> Unit,
) {
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false

            lastVisibleItem.index > (totalItemsCount - loadMoreThreshold - 1)
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
