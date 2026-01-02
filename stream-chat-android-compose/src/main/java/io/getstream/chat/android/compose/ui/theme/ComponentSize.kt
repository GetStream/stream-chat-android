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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Represents the size of a component.
 *
 * @param width The width of the component.
 * @param height The height of the component.
 */
public data class ComponentSize(
    val width: Dp,
    val height: Dp,
) {
    public companion object {
        /**
         * Represents a zero size.
         */
        public val Zero: ComponentSize = ComponentSize(0.dp, 0.dp)

        /**
         * Creates a square size with the same value for width and height.
         *
         * @param size The size value.
         */
        public fun square(size: Dp): ComponentSize = ComponentSize(size, size)

        /**
         * Represents a fill max size.
         */
        public val FillMaxSize: ComponentSize = ComponentSize(Dp.Infinity, Dp.Infinity)

        /**
         * Represents a fill max width.
         *
         * @param height The height value.
         */
        public fun fillMaxWidth(height: Dp): ComponentSize = ComponentSize(Dp.Infinity, height)

        /**
         * Represents a fill max height.
         *
         * @param width The width value.
         */
        public fun fillMaxHeight(width: Dp): ComponentSize = ComponentSize(width, Dp.Infinity)

        /**
         * Represents a height size.
         *
         * @param height The height value.
         */
        public fun height(height: Dp): ComponentSize = ComponentSize(Dp.Unspecified, height)

        /**
         * Represents a width size.
         *
         * @param width The width value.
         */
        public fun width(width: Dp): ComponentSize = ComponentSize(width, Dp.Unspecified)
    }
}

/**
 * Represents the padding of a component.
 *
 * @param start The start padding.
 * @param top The top padding.
 * @param end The end padding.
 * @param bottom The bottom padding.
 */
public data class ComponentPadding(
    val start: Dp = 0.dp,
    val top: Dp = 0.dp,
    val end: Dp = 0.dp,
    val bottom: Dp = 0.dp,
) {

    public constructor(horizontal: Dp = 0.dp, vertical: Dp = 0.dp) : this(
        start = horizontal,
        top = vertical,
        end = horizontal,
        bottom = vertical,
    )

    public companion object {
        /**
         * Represents a zero padding.
         */
        public val Zero: ComponentPadding = ComponentPadding()

        /**
         * Creates a padding with the same value for all sides.
         *
         * @param padding The padding value.
         */
        public fun all(padding: Dp): ComponentPadding = ComponentPadding(padding, padding, padding, padding)
    }

    /**
     * Returns the Compose padding values.
     */
    val values: PaddingValues get() = PaddingValues(start, top, end, bottom)
}

/**
 * Represents the offset of a component.
 *
 * @param x The x offset.
 * @param y The y offset.
 */
public data class ComponentOffset(
    val x: Dp,
    val y: Dp,
) {
    public companion object {
        public val Zero: ComponentOffset = ComponentOffset(0.dp, 0.dp)
    }
}
