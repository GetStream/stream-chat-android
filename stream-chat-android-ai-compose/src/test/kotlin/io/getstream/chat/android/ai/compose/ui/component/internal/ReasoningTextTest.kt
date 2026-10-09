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

package io.getstream.chat.android.ai.compose.ui.component.internal

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

@Suppress("StringShouldBeRawString") // The line breaks are what the tests are about.
internal class ReasoningTextTest {

    private val duration = DurationStrings(
        seconds = "%ds",
        minutes = "%dm",
        minutesSeconds = "%dm %ds",
        fraction = "%ss",
        locale = Locale.US,
    )

    private val strings = ReasoningStrings(
        thinking = "Thinking…",
        thinkingFor = "Thinking… %s",
        thought = "Thought process",
        thoughtFor = "Thought for %s",
        duration = duration,
    )

    @Test
    fun `paragraphs split at blank lines`() {
        val paragraphs = ReasoningParagraph.split("First thought.\nStill first.\n\n\n\nSecond thought.\n\n")

        assertEquals(listOf("First thought.\nStill first.", "Second thought."), paragraphs.map { it.text })
        assertEquals(listOf(0, 1), paragraphs.map { it.id })
    }

    @Test
    fun `a growing paragraph keeps its identity`() {
        val before = ReasoningParagraph.split("Settled.\n\nGrow")
        val after = ReasoningParagraph.split("Settled.\n\nGrowing now")

        assertEquals(before.first(), after.first())
        assertEquals(before.last().id, after.last().id)
    }

    @Test
    fun `the reasoning is open while the model thinks and folds when it is done`() {
        assertTrue(reasoningOpens(isThinking = true, showsLiveReasoning = true, initiallyExpanded = false))
        assertFalse(reasoningOpens(isThinking = false, showsLiveReasoning = true, initiallyExpanded = false))
        assertTrue(reasoningOpens(isThinking = false, showsLiveReasoning = true, initiallyExpanded = true))
        assertFalse(reasoningOpens(isThinking = true, showsLiveReasoning = false, initiallyExpanded = false))
    }

    @Test
    fun `the header counts the seconds while thinking`() {
        assertEquals("Thinking…", thinkingTitle(0.4, strings))
        assertEquals("Thinking… 7s", thinkingTitle(7.6, strings))
        assertEquals("Thinking… 1m 5s", thinkingTitle(65.0, strings))
    }

    @Test
    fun `the title follows the thinking`() {
        assertEquals("Thinking…", reasoningTitle(isThinking = true, durationSeconds = 4.0, strings = strings))
        assertEquals("Thought for 12s", reasoningTitle(isThinking = false, durationSeconds = 12.6, strings = strings))
        assertEquals("Thought for 1m 5s", reasoningTitle(isThinking = false, durationSeconds = 65.0, strings = strings))
        assertEquals("Thought for 1s", reasoningTitle(isThinking = false, durationSeconds = 0.2, strings = strings))
        assertEquals("Thought for 2m", reasoningTitle(isThinking = false, durationSeconds = 120.0, strings = strings))
        assertEquals("Thought process", reasoningTitle(isThinking = false, durationSeconds = null, strings = strings))
    }

    @Test
    fun `tool durations read like the agent's`() {
        assertEquals("0.4s", formatToolDuration(0.42, duration))
        assertEquals("13s", formatToolDuration(12.6, duration))
        assertEquals("1m 5s", formatToolDuration(65.2, duration))
    }

    @Test
    fun `durations follow the translated units and the locale's decimal separator`() {
        val german = DurationStrings(
            seconds = "%d Sek.",
            minutes = "%d Min.",
            minutesSeconds = "%d Min. %d Sek.",
            fraction = "%s Sek.",
            locale = Locale.GERMANY,
        )

        assertEquals("0,4 Sek.", formatToolDuration(0.42, german))
        assertEquals("1 Min. 5 Sek.", formatToolDuration(65.2, german))
        assertEquals("2 Min.", formatSeconds(120, german))
    }

    @Test
    fun `new thoughts are revealed steadily`() {
        val reveal = TextReveal()
        reveal.update("Weighing", animated = false)
        assertEquals("what is there when the view opens shows at once", "Weighing", reveal.shown)

        reveal.update("Weighing the two options.", animated = true)
        assertEquals("new thoughts are not dumped in one go", "Weighing", reveal.shown)
        assertTrue(reveal.revealing)
        reveal.tick()
        assertTrue(reveal.shown.length > "Weighing".length && reveal.shown.length < "Weighing the two options.".length)
        repeat(40) { reveal.tick() }
        assertEquals("Weighing the two options.", reveal.shown)
        assertFalse(reveal.revealing)

        reveal.update("Something else entirely", animated = true)
        assertEquals("text that does not carry on replaces what is shown", "Something else entirely", reveal.shown)
        reveal.update("Something else entirely, done", animated = false)
        assertEquals("once the model is done the rest shows at once", "Something else entirely, done", reveal.shown)
    }

    @Test
    fun `revealing never splits a character`() {
        val reveal = TextReveal("a")
        reveal.update("a😀😀😀😀😀😀😀", animated = true)
        while (reveal.revealing) {
            reveal.tick()
            val last = reveal.shown.last()
            assertFalse(Character.isHighSurrogate(last))
        }
        assertEquals("a😀😀😀😀😀😀😀", reveal.shown)
    }

    @Test
    fun `inline markdown and headings read as text`() {
        val text = ReasoningParagraph.annotated("### Plan\nCheck the **order** first, *then* `pay` via [Stripe](https://stripe.com)")

        assertEquals("Plan\nCheck the order first, then pay via Stripe", text.text)
        val bold = text.spanStyles.filter { it.item.fontWeight == FontWeight.SemiBold }.map { text.text.substring(it.start, it.end) }
        assertEquals(listOf("Plan", "order"), bold)
        val italic = text.spanStyles.filter { it.item.fontStyle == FontStyle.Italic }.map { text.text.substring(it.start, it.end) }
        assertEquals(listOf("then"), italic)
        val code = text.spanStyles.filter { it.item.fontFamily == FontFamily.Monospace }.map { text.text.substring(it.start, it.end) }
        assertEquals(listOf("pay"), code)
        val links = text.getLinkAnnotations(0, text.length)
        assertEquals("https://stripe.com", (links.single().item as LinkAnnotation.Url).url)
        assertEquals("Stripe", text.text.substring(links.single().start, links.single().end))
    }

    @Test
    fun `unclosed markdown is shown as written`() {
        assertEquals("a **half", ReasoningParagraph.annotated("a **half").text)
        assertEquals("snake_case_name stays", ReasoningParagraph.annotated("snake_case_name stays").text)
        assertEquals("2 * 3 * 4", ReasoningParagraph.annotated("2 * 3 * 4").text)
    }
}
