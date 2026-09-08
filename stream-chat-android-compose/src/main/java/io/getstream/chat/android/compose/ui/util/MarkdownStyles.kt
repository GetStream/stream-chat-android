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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamTypography

/**
 * Styling for the markdown constructs rendered inside a message bubble.
 *
 * @param listIndent Indents a list once per level of nesting, wrapped lines included.
 * @param blockQuoteIndent Sets a quote in from the margin once per level, leaving room for
 * the rail drawn beside it.
 * @param thematicBreak Stands in for a thematic break (`---`).
 */
@Immutable
internal data class MarkdownStyles(
    val headings: List<SpanStyle>,
    val codeSpan: SpanStyle,
    val codeBlock: SpanStyle,
    val blockQuote: SpanStyle,
    val listIndent: TextUnit = 1.em,
    val blockQuoteIndent: TextUnit = BlockQuoteIndent,
    val thematicBreak: String = "⸻",
) {

    /** The style for a heading of the given [level], counting from 1. */
    fun heading(level: Int): SpanStyle = headings.getOrElse(level - 1) { headings.last() }

    companion object {

        /** Shared with whatever draws the rail, so the two agree on where it goes. */
        val BlockQuoteIndent: TextUnit = 1.em

        /** Builds the default styling from the design system. */
        @Composable
        fun defaults(
            typography: StreamTypography = ChatTheme.typography,
            colors: StreamColors = ChatTheme.colors,
        ): MarkdownStyles = MarkdownStyles(
            // Four sizes for six levels, so two pairs collide. The fourth stays at body size,
            // as it does on the web, and the sixth is set apart by colour. Every level is
            // weighted above body text, which is what carries a heading where sizes run out.
            headings = listOf(
                typography.title1.toSpanStyle(),
                typography.title3Bold.toSpanStyle(),
                typography.bodyBold.toSpanStyle(),
                typography.bodyBold.toSpanStyle(),
                typography.footnoteBold.toSpanStyle(),
                typography.footnoteBold.toSpanStyle().copy(color = colors.textLowEmphasis),
            ),
            codeSpan = SpanStyle(
                fontFamily = FontFamily.Monospace,
                background = colors.inputBackground,
            ),
            codeBlock = SpanStyle(
                fontFamily = FontFamily.Monospace,
                background = colors.inputBackground,
            ),
            blockQuote = SpanStyle(color = colors.textLowEmphasis),
        )
    }
}
