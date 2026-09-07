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

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

/**
 * Draws the rail beside every rendered line of a block quote, in [color], reading the quotes from
 * the [AnnotationTagBlockQuote] ranges of [annotations] and their extent from [layout].
 *
 * Drawn rather than written as a marker character, because the lines a quote occupies are only
 * known once the text has been laid out. A character can be placed on a line break the renderer
 * made, never on one the layout chose, and it leaves a gap between lines besides.
 */
internal fun Modifier.blockQuoteRails(
    annotations: List<AnnotatedString.Range<String>>,
    layout: () -> TextLayoutResult?,
    color: Color,
    indentPerDepth: TextUnit,
): Modifier {
    val quotes = annotations.filter { it.tag == AnnotationTagBlockQuote }
    if (quotes.isEmpty()) return this
    return drawBehind {
        val laidOut = layout() ?: return@drawBehind
        val step = indentPerDepth.toPx(this, laidOut)
        val width = RailWidth.toPx()
        quotes.forEach { quote ->
            val depth = quote.item.toIntOrNull() ?: return@forEach
            // Centred in the space the last level of indent opened up.
            val offset = step * (depth - 1) + (step - width) / 2
            val lines = laidOut.lineRange(quote) ?: return@forEach
            // Mirrored for a quote running right to left, since the indent it sits in is
            // start-relative. Taken from the paragraph rather than the layout direction, because
            // one message can carry a quote of each direction.
            val left = when (laidOut.getParagraphDirection(quote.start)) {
                ResolvedTextDirection.Rtl -> size.width - offset - width
                else -> offset
            }
            for (line in lines) {
                val top = laidOut.getLineTop(line)
                drawRect(
                    color = color,
                    topLeft = Offset(left, top),
                    size = Size(width, laidOut.getLineBottom(line) - top),
                )
            }
        }
    }
}

/**
 * Every line the quote occupies, blank ones included, so the rail runs unbroken through the gap
 * between two paragraphs of the same quote.
 */
private fun TextLayoutResult.lineRange(quote: AnnotatedString.Range<String>): IntRange? {
    val last = (quote.end - 1).coerceAtLeast(quote.start)
    if (quote.start >= layoutInput.text.length) return null
    return getLineForOffset(quote.start)..getLineForOffset(last.coerceAtMost(layoutInput.text.length - 1))
}

/** Resolves against the laid-out font size, since the indent is expressed relative to the text. */
private fun TextUnit.toPx(density: Density, layout: TextLayoutResult): Float {
    val fontSize = layout.layoutInput.style.fontSize
    return when {
        type == TextUnitType.Sp -> with(density) { toPx() }
        type == TextUnitType.Em && fontSize.type == TextUnitType.Sp ->
            value * with(density) { fontSize.toPx() }
        else -> 0f
    }
}

private val RailWidth = 2.dp
