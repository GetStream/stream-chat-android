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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Displays different content based on the loading and empty states.
 * Uses [Crossfade] to animate the transition.
 *
 * @param isLoading If the content is loading.
 * @param modifier The modifier to apply to this layout.
 * @param isEmpty If the content is empty. Defaults to false.
 * @param contentAlignment The alignment of the content inside the box. Defaults to [Alignment.Center].
 * @param loadingIndicator The indicator to display when [isLoading] is true. Defaults to [LoadingIndicator].
 * @param emptyContent The content to display when [isEmpty] is true. Defaults to empty.
 * @param content The content to display when neither [isLoading] nor [isEmpty] is true.
 */
@Composable
internal fun ContentBox(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    isEmpty: Boolean = false,
    contentAlignment: Alignment = Alignment.Center,
    loadingIndicator: @Composable BoxScope.() -> Unit = { LoadingIndicator() },
    emptyContent: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    Crossfade(
        targetState = isLoading to isEmpty,
    ) { (isLoading, isEmpty) ->
        Box(
            modifier = modifier,
            contentAlignment = contentAlignment,
        ) {
            when {
                isLoading -> loadingIndicator()
                isEmpty -> emptyContent()
                else -> content()
            }
        }
    }
}
