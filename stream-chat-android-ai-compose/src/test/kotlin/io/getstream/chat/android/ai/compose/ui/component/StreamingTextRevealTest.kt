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

package io.getstream.chat.android.ai.compose.ui.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class StreamingTextRevealTest {

    @get:Rule
    val rule = createComposeRule()

    private var text by mutableStateOf("")
    private var animate by mutableStateOf(true)
    private val shown = mutableListOf<String>()

    @Before
    fun setUp() {
        rule.mainClock.autoAdvance = false
    }

    private fun show(initialText: String, initialAnimate: Boolean) {
        text = initialText
        animate = initialAnimate
        rule.setContent {
            StreamingText(text = text, animate = animate, chunkDelayMs = ChunkDelayMs) { displayed ->
                if (shown.lastOrNull() != displayed) shown += displayed
            }
        }
    }

    // Applies the change and runs the frame that recomposes with it.
    private fun update(change: () -> Unit) {
        rule.runOnIdle(change)
        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle()
    }

    private fun advanceChunks(count: Int) {
        repeat(count) { rule.mainClock.advanceTimeBy(ChunkDelayMs) }
        rule.waitForIdle()
    }

    private val current: String get() = shown.last()

    private fun chunksUntilShown(expected: String): Int {
        var steps = 0
        while (current != expected) {
            check(steps < MaxSteps) { "never showed \"$expected\", last \"$current\"" }
            advanceChunks(1)
            steps++
        }
        return steps
    }

    @Test
    fun `without animation the whole text shows at once`() {
        show("Hello there friend", initialAnimate = false)
        rule.waitForIdle()

        assertEquals(listOf("Hello there friend"), shown)
    }

    @Test
    fun `with animation the text is revealed one chunk at a time`() {
        show("Hello there friend", initialAnimate = true)
        advanceChunks(1)

        assertTrue(current, current.length < "Hello there friend".length)

        advanceChunks(10)

        assertEquals("Hello there friend", current)
        assertEquals(listOf("", "Hello", "Hello ", "Hello there", "Hello there ", "Hello there friend"), shown)
    }

    @Test
    fun `a continuation keeps what is shown and reveals only the new part`() {
        show("Hello", initialAnimate = true)
        advanceChunks(5)
        assertEquals("Hello", current)
        val before = shown.size

        update { text = "Hello world" }
        val continuationSteps = chunksUntilShown("Hello world")
        assertEquals(listOf("Hello ", "Hello world"), shown.drop(before))

        // A new text of the same shape is revealed from its first word, which takes longer.
        update { text = "Bye world" }
        val freshSteps = chunksUntilShown("Bye world")

        assertTrue("continuation $continuationSteps, fresh $freshSteps", continuationSteps < freshSteps)
    }

    @Test
    fun `a text that does not continue the previous one starts over`() {
        show("Hello world", initialAnimate = true)
        advanceChunks(5)
        val before = shown.size

        update { text = "Good bye now" }
        advanceChunks(10)

        assertEquals(listOf("Good", "Good ", "Good bye", "Good bye ", "Good bye now"), shown.drop(before))
    }

    @Test
    fun `the same text again changes nothing`() {
        show("Hello world", initialAnimate = true)
        advanceChunks(5)
        val before = shown.toList()

        update { text = "Hello world" }
        advanceChunks(5)

        assertEquals(before, shown)
    }

    @Test
    fun `an empty text clears what is shown`() {
        show("Hello", initialAnimate = true)
        advanceChunks(5)

        update { text = "" }
        advanceChunks(1)

        assertEquals("", current)
    }

    @Test
    fun `turning animation off mid-reveal finishes the reveal instead of jumping`() {
        show("one two three four", initialAnimate = true)
        advanceChunks(2)
        val partial = current
        assertTrue(partial.length < "one two three four".length)

        update { animate = false }
        advanceChunks(10)

        assertEquals("one two three four", current)
        val afterSwitch = shown.drop(shown.indexOf(partial) + 1)
        assertTrue("revealed ${afterSwitch.size} more steps", afterSwitch.size > 1)
    }

    @Test
    fun `a new text arriving as animation turns off shows at once`() {
        show("Hello", initialAnimate = true)
        advanceChunks(5)
        val before = shown.size

        update {
            text = "Goodbye everyone"
            animate = false
        }
        advanceChunks(5)

        assertEquals(listOf("Goodbye everyone"), shown.drop(before))
    }

    @Test
    fun `with animation off a new text shows at once`() {
        show("Hello", initialAnimate = false)
        rule.waitForIdle()

        update { text = "Hello world" }
        advanceChunks(3)

        // No partial step such as "Hello " in between.
        assertEquals(listOf("Hello", "Hello world"), shown)
    }

    @Test
    fun `line breaks are kept as their own chunks`() {
        show("one\ntwo", initialAnimate = true)
        advanceChunks(5)

        assertEquals(listOf("", "one", "one\n", "one\ntwo"), shown)
    }

    private companion object {
        const val ChunkDelayMs = 30L
        const val MaxSteps = 20
    }
}
