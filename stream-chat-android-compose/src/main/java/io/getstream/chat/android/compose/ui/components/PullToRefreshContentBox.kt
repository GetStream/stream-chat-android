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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * A wrapper around a [PullToRefreshBox] that displays an empty content when it is empty.
 *
 * @param isLoading If the content is loading.
 * @param onRefresh The callback to be called when the refresh is requested.
 * @param modifier The modifier to apply to this layout.
 * @param isEmpty If the content is empty.
 * @param loadingIndicator The indicator to display when the content is loading.
 * @param emptyContent The content to display when the content is empty.
 * @param content The content to display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PullToRefreshContentBox(
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    isEmpty: Boolean = false,
    loadingIndicator: @Composable BoxScope.(pullToRefreshState: PullToRefreshState, isRefreshing: Boolean) -> Unit =
        { pullToRefreshState, isRefreshing ->
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = ChatTheme.colors.barsBackground,
                color = ChatTheme.colors.primaryAccent,
            )
        },
    emptyContent: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        state = pullToRefreshState,
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = modifier,
        indicator = { loadingIndicator(pullToRefreshState, isLoading) },
    ) {
        if (isEmpty) {
            Box(
                modifier = Modifier
                    // Ensure the content fills the available space and
                    // add scrollability even when empty, so that the pull-to-refresh works.
                    .matchParentSize()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center,
            ) {
                emptyContent()
            }
        } else {
            content()
        }
    }
}
