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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

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
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false

            lastVisibleItem.index > (totalItemsCount - loadMoreThreshold - 1)
        }
    }

    LaunchedEffect(shouldLoadMore) {
        loadMore()
    }
}
