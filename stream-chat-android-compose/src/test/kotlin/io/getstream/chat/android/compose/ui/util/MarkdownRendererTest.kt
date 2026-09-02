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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.ui.util.internal.MarkdownRenderer
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class MarkdownRendererTest {

    private val renderer = MarkdownRenderer(TestStyles)

    @ParameterizedTest
    @MethodSource("renderedTextArguments")
    fun `renders the text markdown produces`(source: String, expected: String) {
        renderer.render(source).text shouldBeEqualTo expected
    }

    @Test
    fun `styles bold text`() {
        val result = renderer.render("say **that** now")

        result.text shouldBeEqualTo "say that now"
        result.spanAt("that")?.fontWeight shouldBeEqualTo FontWeight.Bold
    }

    @Test
    fun `styles italic text`() {
        val result = renderer.render("say *that* now")

        result.text shouldBeEqualTo "say that now"
        result.spanAt("that")?.fontStyle shouldBeEqualTo FontStyle.Italic
    }

    @Test
    fun `styles bold italic text`() {
        val result = renderer.render("say ***that*** now")

        result.text shouldBeEqualTo "say that now"
        result.spanAt("that")?.fontWeight shouldBeEqualTo FontWeight.Bold
        result.spanAt("that")?.fontStyle shouldBeEqualTo FontStyle.Italic
    }

    @Test
    fun `styles strikethrough text`() {
        val result = renderer.render("say ~~that~~ now")

        result.text shouldBeEqualTo "say that now"
        result.spanAt("that")?.textDecoration shouldBeEqualTo TextDecoration.LineThrough
    }

    @Test
    fun `styles an inline code span`() {
        val result = renderer.render("call `render()` first")

        result.text shouldBeEqualTo "call render() first"
        result.spanAt("render()")?.fontFamily shouldBeEqualTo FontFamily.Monospace
    }

    @Test
    fun `styles a fenced code block and keeps its line breaks`() {
        val result = renderer.render("before\n\n```kotlin\nval a = 1\n\nval b = 2\n```")

        result.text shouldBeEqualTo "before\n\nval a = 1\n\nval b = 2"
        result.spanAt("val a = 1")?.fontFamily shouldBeEqualTo FontFamily.Monospace
    }

    @Test
    fun `styles a heading per level`() {
        val levels = (1..6).map { level ->
            val marker = "#".repeat(level)
            renderer.render("$marker Title").spanAt("Title")?.fontSize
        }

        levels shouldBeEqualTo listOf(30.sp, 26.sp, 22.sp, 18.sp, 16.sp, 14.sp)
    }

    @Test
    fun `indents nested list items`() {
        val result = renderer.render("- one\n    - two\n        - three")

        // Indenting with text rather than a paragraph style keeps every item on one line: a
        // paragraph range that ends in a line break renders an empty line after it.
        result.text shouldBeEqualTo "• one\n>• two\n>>• three"
        result.paragraphStyles.shouldBeEmpty()
    }

    @Test
    fun `keeps the ordinals of an ordered list`() {
        renderer.render("1. one\n1. two\n1. three").text shouldBeEqualTo "1. one\n2. two\n3. three"
    }

    @Test
    fun `annotates an inline link with its destination`() {
        val result = renderer.render("see [the docs](https://getstream.io/chat) now")

        result.text shouldBeEqualTo "see the docs now"
        result.urlAt("the docs") shouldBeEqualTo "https://getstream.io/chat"
    }

    @Test
    fun `makes a schemeless link destination absolute`() {
        val result = renderer.render("[link](getstream.io)")

        result.urlAt("link") shouldBeEqualTo "https://getstream.io"
    }

    @Test
    fun `keeps the destination of a link whose label is itself a url`() {
        val result = renderer.render("[https://text-link.com](https://real-link.com)")

        result.text shouldBeEqualTo "https://text-link.com"
        result.urlAt("https://text-link.com") shouldBeEqualTo "https://real-link.com"
    }

    @Test
    fun `renders an image as its source, since images are not supported`() {
        renderer.render("![alt](https://example.com/a.png)").text shouldBeEqualTo
            "![alt](https://example.com/a.png)"
    }

    @Test
    fun `does not hang on pathological emphasis markers`() {
        // Regression input taken from the iOS SDK, which once hung on it.
        val source = "**~*~~~*~*~**~*~* h e a r d ***~*~*~**~*~~~*"

        renderer.render(source).text.isNotEmpty() shouldBeEqualTo true
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun renderedTextArguments(): List<Arguments> = listOf(
            Arguments.of("plain text", "plain text"),
            // A soft break has to stay a line break, or enabling markdown would join lines that
            // render correctly today.
            Arguments.of("first\nsecond", "first\nsecond"),
            Arguments.of("first\n\nsecond", "first\n\nsecond"),
            // Hard breaks, in their three spellings.
            Arguments.of("first  \nsecond", "first\nsecond"),
            Arguments.of("first\\\nsecond", "first\nsecond"),
            Arguments.of("first<br/>second", "first\nsecond"),
            Arguments.of("# Title\nbody", "Title\nbody"),
            Arguments.of("> quoted", "|quoted"),
            // One quote spanning two lines: CommonMark treats a soft break inside a quote as one
            // block, and every line of it is marked so it reads as a single quote.
            Arguments.of("> quoted\n> continued", "|quoted\n|continued"),
            // A blank line ends a quote, so this is two of them, kept apart.
            Arguments.of("> first\n\n> second", "|first\n\n|second"),
            Arguments.of("- one\n- two", "• one\n• two"),
            Arguments.of("---\nafter", "***\nafter"),
            // Escapes are resolved, and a backslash that escapes nothing is left alone.
            Arguments.of("5 \\* 3", "5 * 3"),
            Arguments.of("C:\\path\\to", "C:\\path\\to"),
            // Unsupported constructs keep their source text so nothing is lost.
            Arguments.of("| a | b |\n| --- | --- |\n| 1 | 2 |", "| a | b |\n| --- | --- |\n| 1 | 2 |"),
        )
    }
}

private val TestStyles = MarkdownStyles(
    heading1 = SpanStyle(fontSize = 30.sp),
    heading2 = SpanStyle(fontSize = 26.sp),
    heading3 = SpanStyle(fontSize = 22.sp),
    heading4 = SpanStyle(fontSize = 18.sp),
    heading5 = SpanStyle(fontSize = 16.sp),
    heading6 = SpanStyle(fontSize = 14.sp),
    codeSpan = SpanStyle(fontFamily = FontFamily.Monospace),
    codeBlock = SpanStyle(fontFamily = FontFamily.Monospace),
    blockQuote = SpanStyle(color = Color.Gray),
    listIndent = ">",
    blockQuotePrefix = "|",
    thematicBreak = "***",
)

/** The merged span covering [substring], or null when it carries no style of its own. */
private fun AnnotatedString.spanAt(substring: String): SpanStyle? {
    val start = text.indexOf(substring)
    if (start < 0) return null
    val covering = spanStyles.filter { it.start <= start && it.end >= start + substring.length }
    if (covering.isEmpty()) return null
    return covering.map { it.item }.reduce { merged, style -> merged.merge(style) }
}

private fun AnnotatedString.urlAt(substring: String): String? {
    val start = text.indexOf(substring)
    if (start < 0) return null
    return getStringAnnotations(AnnotationTagUrl, start, start + substring.length).firstOrNull()?.item
}
