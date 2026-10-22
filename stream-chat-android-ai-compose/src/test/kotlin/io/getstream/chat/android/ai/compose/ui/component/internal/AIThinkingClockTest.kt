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

import androidx.compose.runtime.saveable.SaverScope
import io.getstream.chat.android.ai.compose.ui.component.StreamingReasoningDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class AIThinkingClockTest {

    private var now = Asked
    private val clock = AIThinkingClock { now }

    private fun at(millis: Long): AIThinkingClock {
        now = Asked + millis
        return clock
    }

    private fun thinkingTurn(): String {
        at(0).update(Turn, isThinking = true)
        return Turn
    }

    @Test
    fun `a turn is known by its key`() {
        clock.update(StringBuilder("q").append(1).toString(), isThinking = true)

        assertEquals(Asked, clock.startedAt(Turn))
        assertNull(clock.startedAt("q2"))
    }

    @Test
    fun `update starts the thinking the first time the turn thinks`() {
        clock.update(Turn, isThinking = false)
        assertNull("a turn that never thought has no thinking to time", clock.startedAt(Turn))

        clock.update(Turn, isThinking = true)
        at(5000).update(Turn, isThinking = true)
        assertEquals(Asked, clock.startedAt(Turn))
    }

    @Test
    fun `update with no latest turn does nothing`() {
        clock.update(null, isThinking = true)

        assertNull(clock.startedAt(Turn))
    }

    @Test
    fun `when thinking resumes before the first step it carries on`() {
        val turn = thinkingTurn()
        at(2000).update(turn, isThinking = false)
        at(3000).update(turn, isThinking = true)
        at(8000).update(turn, isThinking = false)

        assertEquals(Asked, clock.startedAt(turn))
        assertEquals(8.0, at(9000).durationOfFirstStep(turn, ownSeconds = null))
    }

    @Test
    fun `when the turn thinks again after its reply was taken away it starts afresh`() {
        val turn = thinkingTurn()
        assertTrue(clock.isFirstStep("r1", turn))
        at(8000).update(turn, isThinking = false, hasReplies = true)

        at(60_000).update(turn, isThinking = true, hasReplies = false)
        at(65_000).update(turn, isThinking = false, hasReplies = true)

        // Such as when the app asks for the reply again.
        assertEquals(Asked + 60_000, clock.startedAt(turn))
        // The new reply's first step takes over its thinking.
        assertTrue(clock.isFirstStep("r1", turn))
        assertEquals(5.0, at(69_000).durationOfFirstStep(turn, ownSeconds = null))
    }

    @Test
    fun `when thinking resumes while the reply shows it carries on`() {
        val turn = thinkingTurn()
        at(8000).update(turn, isThinking = false, hasReplies = true)
        at(9000).update(turn, isThinking = true, hasReplies = true)

        assertEquals(Asked, clock.startedAt(turn))
    }

    @Test
    fun `isFirstStep is the first step to show in the turn`() {
        val turn = thinkingTurn()

        assertTrue(clock.isFirstStep("r1", turn))
        assertFalse(clock.isFirstStep("r2", turn))
        // So it stays first when its view is made again.
        assertTrue(clock.isFirstStep("r1", turn))
    }

    @Test
    fun `isFirstStep when the turn was not seen thinking is false`() {
        assertFalse(clock.isFirstStep("r1", Turn))
    }

    @Test
    fun `durationOfFirstStep when the step arrived done is until the turn stopped thinking`() {
        val turn = thinkingTurn()
        at(8000).update(turn, isThinking = false)
        at(20_000).update(turn, isThinking = false)

        assertEquals(8.0, at(30_000).durationOfFirstStep(turn, ownSeconds = null))
    }

    @Test
    fun `durationOfFirstStep when the step was seen streaming is until it is done`() {
        val turn = thinkingTurn()
        at(2000).update(turn, isThinking = false)
        clock.firstStepIsStreaming(turn)

        assertEquals(9.0, at(9000).durationOfFirstStep(turn, ownSeconds = 4.0))
    }

    @Test
    fun `durationOfFirstStep when its own is longer is its own`() {
        val turn = thinkingTurn()
        at(3000).update(turn, isThinking = false)

        assertEquals(12.0, clock.durationOfFirstStep(turn, ownSeconds = 12.0))
    }

    @Test
    fun `durationOfFirstStep is measured once`() {
        val turn = thinkingTurn()
        clock.firstStepIsStreaming(turn)
        at(6000).durationOfFirstStep(turn, ownSeconds = null)

        // So the step reads the same when its view is made again.
        assertEquals(6.0, at(60_000).durationOfFirstStep(turn, ownSeconds = null))
    }

    @Test
    fun `durationOfFirstStep when the turn was not seen thinking is the step's own`() {
        assertEquals(4.0, clock.durationOfFirstStep(Turn, ownSeconds = 4.0))
        assertNull(clock.durationOfFirstStep(Turn, ownSeconds = null))
    }

    @Test
    fun `showsFirstStep when the step arrives done as the thinking ends opens it and holds the answer`() {
        val turn = thinkingTurn()
        at(8000).update(turn, isThinking = false)

        assertTrue(at(8100).showsFirstStep(turn))
        assertEquals(
            Asked + 8100 + AIThinkingClock.FINISHED_REASONING_SHOWN_MILLIS + StreamingReasoningDefaults.FoldDurationMillis,
            clock.answerStartsAt(turn),
        )
    }

    @Test
    fun `showsFirstStep when the step arrives later stays folded`() {
        val turn = thinkingTurn()
        at(8000).update(turn, isThinking = false)

        assertFalse(at(12_000).showsFirstStep(turn))
        assertNull(clock.answerStartsAt(turn))
    }

    @Test
    fun `showsFirstStep when the step thought in view stays as it is`() {
        val turn = thinkingTurn()
        clock.firstStepIsStreaming(turn)
        at(8000).update(turn, isThinking = false)

        // It showed its reasoning as it came.
        assertFalse(clock.showsFirstStep(turn))
    }

    @Test
    fun `showsFirstStep when the step is made again later stays folded`() {
        val turn = thinkingTurn()
        at(8000).update(turn, isThinking = false)
        assertTrue(clock.showsFirstStep(turn))
        assertTrue("it keeps showing while it arrives", at(8500).showsFirstStep(turn))

        assertFalse(at(30_000).showsFirstStep(turn))
    }

    @Test
    fun `a saved clock is restored with its turns' times`() {
        val turn = thinkingTurn()
        clock.isFirstStep("r1", turn)
        at(8000).update(turn, isThinking = false, hasReplies = true)
        assertEquals(8.0, clock.durationOfFirstStep(turn, ownSeconds = null))

        val saved = with(AIThinkingClock.Saver) { SaverScope { true }.save(clock) }
        val restored = AIThinkingClock.Saver.restore(saved!!)!!

        assertEquals(Asked, restored.startedAt(turn))
        assertTrue(restored.isFirstStep("r1", turn))
        assertFalse(restored.isFirstStep("r2", turn))
        assertEquals(8.0, restored.durationOfFirstStep(turn, ownSeconds = null))
    }

    private companion object {
        const val Asked = 1_000_000L
        const val Turn = "q1"
    }
}
