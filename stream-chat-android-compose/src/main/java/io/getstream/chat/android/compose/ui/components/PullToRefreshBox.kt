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

package io.getstream.chat.android.compose.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * A wrapper around [Box] that provides pull-to-refresh functionality.
 * This component uses the [PullToRefreshState] to manage the pull-to-refresh state.
 *
 * @param isRefreshing Indicates whether the pull-to-refresh is currently active.
 * @param onRefresh The callback to be invoked when the user performs a pull-to-refresh action.
 * @param modifier The modifier to be applied to the box.
 * @param state The [PullToRefreshState] to manage the pull-to-refresh state.
 * @param enabled Indicates whether the pull-to-refresh is enabled.
 * @param contentAlignment The alignment of the content within the box.
 * @param indicator The custom indicator to be displayed during the pull-to-refresh action.
 * @param content The content to be displayed within the box.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    enabled: Boolean = true,
    contentAlignment: Alignment = Alignment.TopStart,
    indicator: @Composable BoxScope.() -> Unit = {
        Indicator(
            state = state,
            isRefreshing = isRefreshing,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = ChatTheme.colors.barsBackground,
            color = ChatTheme.colors.primaryAccent,
        )
    },
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier.pullToRefresh(
            isRefreshing = isRefreshing,
            state = state,
            enabled = enabled,
            onRefresh = onRefresh,
        ),
        contentAlignment = contentAlignment,
    ) {
        content()
        indicator()
    }
}
