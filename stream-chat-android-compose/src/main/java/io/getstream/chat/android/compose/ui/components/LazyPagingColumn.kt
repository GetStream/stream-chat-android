/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.handlers.LoadMoreHandler

/**
 * A [LazyColumn] that supports paging, by showing a loading item and notifying when more items should be loaded.
 *
 * @param items The list of items to display.
 * @param modifier The modifier to apply to this layout.
 * @param itemKey A factory of stable and unique keys representing the item.
 * @param showLoadingItem If a loading item should be shown at the end of the list.
 * @param onLoadMore Callback to be called when more items should be loaded.
 * @param itemContent The content displayed by a single item.
 * @param loadingItem The content displayed by the loading item.
 *
 * @see [LazyListScope.items]
 */
@Composable
internal fun <T> LazyPagingColumn(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemKey: ((item: T) -> Any)? = null,
    showLoadingItem: Boolean = false,
    onLoadMore: () -> Unit = {},
    itemContent: @Composable LazyItemScope.(item: T) -> Unit,
    loadingItem: @Composable LazyItemScope.() -> Unit = {
        LoadingFooter(modifier = modifier.fillMaxWidth())
    },
) {
    val lazyListState = rememberLazyListState()
    LazyColumn(
        modifier = modifier,
        state = lazyListState,
    ) {
        items(
            items = items,
            key = itemKey,
            itemContent = itemContent,
        )
        if (showLoadingItem) {
            item { loadingItem() }
        }
    }
    LoadMoreHandler(
        listState = lazyListState,
        loadMore = onLoadMore,
    )
}
