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

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.infiniteRepeatable
import com.valentinilk.shimmer.ShimmerTheme
import com.valentinilk.shimmer.defaultShimmerTheme
import com.valentinilk.shimmer.shimmerSpec

/**
 * The default [ShimmerTheme] used in the Stream Chat Compose library.
 */
internal val StreamShimmerTheme: ShimmerTheme = defaultShimmerTheme.copy(
    animationSpec = infiniteRepeatable(
        animation = shimmerSpec(
            durationMillis = 1000,
            easing = FastOutSlowInEasing,
        ),
    ),
    rotation = 45.0f,
)
