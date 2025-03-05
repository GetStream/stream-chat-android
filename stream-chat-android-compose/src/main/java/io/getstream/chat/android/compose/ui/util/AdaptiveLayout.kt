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

package io.getstream.chat.android.compose.ui.util

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowWidthSizeClass

internal object AdaptiveLayoutConstraints {
    const val LIST_PANE_WEIGHT = 0.3f
    const val DETAIL_PANE_WEIGHT = 0.7f
}

/**
 * Provides information about the current adaptive layout.
 */
public object AdaptiveLayoutInfo {
    /**
     * Returns if the current window width size class is expanded.
     * Expanded width windows are windows that are wider than `840dp`, typically used on tablets and desktops.
     *
     * @see <a href=https://developer.android.com/develop/ui/compose/layouts/adaptive/use-window-size-classes>
     *     Use window size classes</a>
     */
    @Composable
    public fun isWidthExpanded(): Boolean =
        currentWindowAdaptiveInfo()
            .windowSizeClass
            .windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
}
