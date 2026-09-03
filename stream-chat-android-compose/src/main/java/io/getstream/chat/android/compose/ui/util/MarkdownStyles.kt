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

package io.getstream.chat.android.compose.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamDesign

/**
 * Styling applied to the markdown constructs rendered inside a message bubble. Derived from the
 * design system, and not yet exposed: nothing outside the kit needs to override it.
 *
 * @param listIndent Prepended once per level of list nesting.
 * @param blockQuotePrefix Prepended to every quoted block.
 * @param thematicBreak Rendered in place of a thematic break (`---`).
 */
@Immutable
internal data class MarkdownStyles(
    val heading1: SpanStyle,
    val heading2: SpanStyle,
    val heading3: SpanStyle,
    val heading4: SpanStyle,
    val heading5: SpanStyle,
    val heading6: SpanStyle,
    val codeSpan: SpanStyle,
    val codeBlock: SpanStyle,
    val blockQuote: SpanStyle,
    val listIndent: String = "    ",
    val blockQuotePrefix: String = "┃ ",
    val thematicBreak: String = "⸻",
) {

    /**
     * The style for an ATX heading of the given [level], counting from 1. Levels past the ones the
     * design system has sizes for share the smallest style.
     */
    internal fun heading(level: Int): SpanStyle {
        val levels = listOf(heading1, heading2, heading3, heading4, heading5, heading6)
        return levels.getOrElse(level - 1) { levels.last() }
    }

    companion object {

        /**
         * Builds the default styling from the design system's type scale and colors.
         */
        @Composable
        fun defaults(
            typography: StreamDesign.Typography = ChatTheme.typography,
            colors: StreamDesign.Colors = ChatTheme.colors,
        ): MarkdownStyles = MarkdownStyles(
            heading1 = typography.headingLarge.toSpanStyle(),
            heading2 = typography.headingMedium.toSpanStyle(),
            heading3 = typography.headingSmall.toSpanStyle(),
            // The type scale runs out of heading sizes at four levels, and the remaining two share
            // the smallest rather than growing back up. The last is set apart by colour instead.
            heading4 = typography.headingExtraSmall.toSpanStyle(),
            heading5 = typography.headingExtraSmall.toSpanStyle(),
            heading6 = typography.headingExtraSmall.toSpanStyle().copy(color = colors.textSecondary),
            codeSpan = SpanStyle(
                fontFamily = FontFamily.Monospace,
                background = colors.backgroundCoreSurfaceSubtle,
            ),
            codeBlock = SpanStyle(
                fontFamily = FontFamily.Monospace,
                background = colors.backgroundCoreSurfaceSubtle,
            ),
            blockQuote = SpanStyle(color = colors.textSecondary),
        )
    }
}
