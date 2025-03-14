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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.valentinilk.shimmer.shimmer
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamShimmerTheme

/**
 * Displays a shimmer progress indicator using [StreamShimmerTheme].
 *
 * @param modifier The modifier to be applied to the component.
 */
@Composable
internal fun ShimmerProgressIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color = ChatTheme.colors.mediaShimmerBase)
            .shimmer()
            .background(color = ChatTheme.colors.mediaShimmerHighlights),
    )
}
