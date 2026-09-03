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
    fun `renders an image as its alt text, since images cannot be drawn`() {
        renderer.render("![alt text](https://example.com/a.png)").text shouldBeEqualTo "alt text"
    }

    @Test
    fun `renders a reference image as its alt text too`() {
        renderer.render("![alt][d]\n\n[d]: https://x.com/a.png").text shouldBeEqualTo "alt"
    }

    @ParameterizedTest
    @MethodSource("unopenableDestinations")
    fun `does not annotate a destination that cannot be opened`(destination: String) {
        val result = renderer.render("[label]($destination)")

        result.text shouldBeEqualTo "label"
        result.urlAt("label") shouldBeEqualTo null
    }

    @ParameterizedTest
    @MethodSource("hostileSchemes")
    fun `does not annotate a scheme a message has no business carrying`(destination: String) {
        // A tapped link is handed to the system, so a message must not be able to reach an
        // arbitrary scheme through one.
        val result = renderer.render("[tap]($destination)")

        result.text shouldBeEqualTo "tap"
        result.urlAt("tap") shouldBeEqualTo null
    }

    @ParameterizedTest
    @MethodSource("openableSchemes")
    fun `annotates a scheme a message may carry`(destination: String) {
        renderer.render("[tap]($destination)").urlAt("tap") shouldBeEqualTo destination
    }

    @Test
    fun `links a destination carrying a port`() {
        renderer.render("[label](example.com:8080)").urlAt("label") shouldBeEqualTo
            "https://example.com:8080"
    }

    @Test
    fun `resolves escapes and references inside a destination`() {
        renderer.render("[x](https://a.com?a=1&amp;b=2)").urlAt("x") shouldBeEqualTo
            "https://a.com?a=1&b=2"
        renderer.render("[x](https://a.com/a\\_b)").urlAt("x") shouldBeEqualTo "https://a.com/a_b"
    }

    @Test
    fun `falls back to plain text when a document cannot be rendered`() {
        // Nesting this deep exhausts the stack while parsing, and this runs during composition.
        val source = ">".repeat(2000) + " x"

        renderer.render(source).text shouldBeEqualTo source
    }

    @Test
    fun `resolves a full reference link`() {
        val result = renderer.render("see [the docs][d] now\n\n[d]: https://getstream.io")

        result.text shouldBeEqualTo "see the docs now"
        result.urlAt("the docs") shouldBeEqualTo "https://getstream.io"
    }

    @Test
    fun `resolves a short reference link`() {
        val result = renderer.render("see [d] now\n\n[d]: getstream.io")

        result.text shouldBeEqualTo "see d now"
        result.urlAt("d") shouldBeEqualTo "https://getstream.io"
    }

    @Test
    fun `leaves a reference link with no definition as written`() {
        renderer.render("see [the docs][nope] now").text shouldBeEqualTo "see [the docs][nope] now"
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
        fun unopenableDestinations(): List<String> = listOf("#section", "/docs/page", "foo bar")

        @JvmStatic
        @Suppress("unused")
        fun hostileSchemes(): List<String> = listOf(
            "javascript:alert(1)",
            "intent://scan/#Intent;scheme=zxing;end",
            "file:///data/data/x",
            "myapp://reset?token=1",
            "unknown-scheme:whatever",
        )

        @JvmStatic
        @Suppress("unused")
        fun openableSchemes(): List<String> = listOf(
            "http://x.com",
            "https://x.com",
            "mailto:a@b.com",
            "tel:+123",
        )

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
            // A tag ending a line absorbs that line's feed, exactly as trailing spaces do.
            Arguments.of("first<br/>\nsecond", "first\nsecond"),
            Arguments.of("# Title\nbody", "Title\nbody"),
            Arguments.of("> quoted", "|quoted"),
            // One quote spanning two lines: CommonMark treats a soft break inside a quote as one
            // block, and every line of it is marked so it reads as a single quote.
            Arguments.of("> quoted\n> continued", "|quoted\n|continued"),
            // A blank line ends a quote, so this is two of them, kept apart.
            Arguments.of("> first\n\n> second", "|first\n\n|second"),
            // A quote holding two paragraphs marks the blank line between them too.
            Arguments.of("> one\n>\n> two", "|one\n|\n|two"),
            // A hard break inside a quote opens exactly one new marked line.
            Arguments.of("> one  \n> two", "|one\n|two"),
            // Nesting stacks the markers.
            Arguments.of("> outer\n> > inner", "|outer\n|\n||inner"),
            Arguments.of("- one\n- two", "• one\n• two"),
            Arguments.of("---\nafter", "***\nafter"),
            // Escapes are resolved, and a backslash that escapes nothing is left alone.
            Arguments.of("5 \\* 3", "5 * 3"),
            Arguments.of("C:\\path\\to", "C:\\path\\to"),
            // Autolink brackets are syntax; the bare URL is left for the entity pass to linkify.
            Arguments.of("visit <https://getstream.io> now", "visit https://getstream.io now"),
            Arguments.of("mail <a@b.com> now", "mail a@b.com now"),
            // Character references are resolved, named and numeric alike.
            Arguments.of("a &amp; b &lt;c&gt;", "a & b <c>"),
            Arguments.of("a &#38; b &#x26; c", "a & b & c"),
            // Anything that only looks like one is left as typed.
            Arguments.of("a &notreal; b", "a &notreal; b"),
            // Code content is literal, so a reference inside it stays as written.
            Arguments.of("`a &amp; b`", "a &amp; b"),
            // An item whose only content is a block keeps it on the marker's line, so no marker
            // is ever left sitting alone.
            Arguments.of("- # H", "• H"),
            Arguments.of("- > quoted", "• |quoted"),
            Arguments.of("- ```\n  x\n  ```", "• x"),
            Arguments.of("- - a", "• >• a"),
            Arguments.of("1. # H\n1. next", "1. H\n2. next"),
            // A second block inside a list item starts its own line, indented under the item's
            // text, rather than running into the marker line.
            Arguments.of("- item\n\n  # H\n- next", "• item\n>H\n• next"),
            Arguments.of("- item\n\n  > q\n- next", "• item\n>|q\n• next"),
            // Content following a nested list stays indented, and the next item still gets a line.
            Arguments.of("- a\n    - b\n\n  more\n- c", "• a\n>• b\n>more\n• c"),
            // A code block inside a quote keeps the marker on every line.
            Arguments.of("> ```\n> one\n> two\n> ```", "|one\n|two"),
            // An indented code block loses the indentation that declared it.
            Arguments.of("    one\n    two", "one\ntwo"),
            // A table is still a block, so what follows it starts on a new line.
            Arguments.of("| a | b |\n| - | - |\n\nafter", "| a | b |\n| - | - |\nafter"),
            // Carriage returns never survive into the output.
            Arguments.of("a\r\n\r\nb", "a\n\nb"),
            Arguments.of("a\r\nb", "a\nb"),
            // A heading keeps neither the space before its text nor the one after it.
            Arguments.of("# H \nnext", "H\nnext"),
            Arguments.of("# H #\nnext", "H\nnext"),
            // Two breaks in the source stay two breaks.
            Arguments.of("a<br/><br/>b", "a\n\nb"),
            // An HTML block is a block, so what follows it starts on a new line.
            Arguments.of("<div>x</div>\n\nafter", "<div>x</div>\nafter"),
            // A document that renders to nothing falls back to what was typed, rather than
            // leaving an empty bubble.
            Arguments.of("[d]: https://getstream.io", "[d]: https://getstream.io"),
            // A checkbox belongs beside the marker rather than pushing the item onto a new line.
            Arguments.of("- [x] done", "• [x] done"),
            Arguments.of("- [ ] todo", "• [ ] todo"),
            // Tab indentation declares an indented code block just as four spaces do.
            Arguments.of("\tone\n\ttwo", "one\ntwo"),
            // Every line of a block inside a list item is indented, not only the first.
            Arguments.of("- item\n\n      one\n      two", "• item\n>one\n>two"),
            // The specification turns a line ending inside a code span into a space.
            Arguments.of("> `a\n> b`", "|a  b"),
            // A reference to an invalid code point becomes the replacement character.
            Arguments.of("a &#xD800; b", "a \uFFFD b"),
            Arguments.of("a &#0; b", "a \uFFFD b"),
            // A link or image with an empty label keeps its source, rather than disappearing.
            Arguments.of("see [](https://x.com) here", "see [](https://x.com) here"),
            Arguments.of("![](https://x.com/a.png)", "![](https://x.com/a.png)"),
            // Only the delimiter runs are syntax, so a backtick between them is content.
            Arguments.of("``a `b` c``", "a `b` c"),
            // Verbatim source drops the quote markers of the lines it continues on.
            Arguments.of("> <div>\n> x\n> </div>", "|<div>\n|x\n|</div>"),
            Arguments.of("> | a |\n> | - |", "|| a |\n|| - |"),
            // A message of only whitespace is still text the sender typed.
            Arguments.of("   ", "   "),
            // Unsupported constructs keep their source text so nothing is lost.
            Arguments.of("| a | b |\n| --- | --- |\n| 1 | 2 |", "| a | b |\n| --- | --- |\n| 1 | 2 |"),
        )
    }
}

private val TestStyles = MarkdownStyles(
    headings = listOf(30, 26, 22, 18, 16, 14).map { SpanStyle(fontSize = it.sp) },
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
