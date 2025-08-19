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

package io.getstream.chat.android.compose.ui.util.adaptivelayout

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

/**
 * Provides information about the current adaptive layout.
 *
 * @see <a href=https://developer.android.com/develop/ui/compose/layouts/adaptive/use-window-size-classes>
 *     Use window size classes</a>
 */
public object AdaptiveLayoutInfo {

    /**
     * Returns if the current window is single pane.
     * Single pane windows are windows that are narrower than `840dp` or shorter than `900dp`.
     */
    @Composable
    public fun singlePaneWindow(): Boolean {
        val windowSize = currentWindowAdaptiveInfo().windowSizeClass
        return windowSize.windowWidthSizeClass < WindowWidthSizeClass.EXPANDED ||
            windowSize.windowHeightSizeClass < WindowHeightSizeClass.MEDIUM
    }
}

internal object AdaptiveLayoutConstraints {
    const val LIST_PANE_WEIGHT = 0.3f
    const val DETAIL_PANE_WEIGHT = 0.7f
}

/**
 * [WindowWidthSizeClass]'s hash codes are set order-wise
 * from [WindowWidthSizeClass.COMPACT] to [WindowWidthSizeClass.EXPANDED], so we use them for comparison.
 */
public operator fun WindowWidthSizeClass.compareTo(other: WindowWidthSizeClass): Int =
    hashCode().compareTo(other.hashCode())

/**
 * [WindowHeightSizeClass]'s hash codes are set order-wise
 * from [WindowHeightSizeClass.COMPACT] to [WindowHeightSizeClass.EXPANDED], so we use them for comparison.
 */
public operator fun WindowHeightSizeClass.compareTo(other: WindowHeightSizeClass): Int =
    hashCode().compareTo(other.hashCode())
