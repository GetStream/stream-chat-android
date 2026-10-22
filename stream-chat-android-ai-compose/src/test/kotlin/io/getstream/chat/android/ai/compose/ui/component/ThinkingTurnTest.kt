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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.parts.AIReasoningPart
import io.getstream.chat.android.ai.compose.ui.component.internal.AIThinkingClock
import io.getstream.chat.android.ai.compose.ui.component.internal.AIThinkingTurn
import io.getstream.chat.android.ai.compose.ui.component.internal.LocalAIThinkingTurn
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ThinkingTurnTest {

    @get:Rule
    val rule = createComposeRule()

    private var now = 0L
    private val clock = AIThinkingClock { now }

    @Before
    fun setUp() {
        rule.mainClock.autoAdvance = false
    }

    private fun show(inTurn: Boolean = true, content: @Composable () -> Unit) {
        rule.setContent {
            val turn = AIThinkingTurn(clock, Turn).takeIf { inTurn }
            CompositionLocalProvider(LocalAIThinkingTurn provides turn, content = content)
        }
        frame()
    }

    private fun frame() {
        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle()
    }

    private val header get() = rule.onNode(hasClickAction())
    private val description: String
        get() = header.fetchSemanticsNode().config[SemanticsProperties.ContentDescription].single()
    private val openState: String get() = header.fetchSemanticsNode().config[SemanticsProperties.StateDescription]

    private fun step(id: String = "r1", streaming: Boolean = false, durationMs: Int? = null) = AIReasoningPart(
        id = id,
        status = if (streaming) AIReasoningPart.Status.Streaming else AIReasoningPart.Status.Completed,
        preview = "Weighing the options.",
        durationMs = durationMs,
    )

    // The turn thought from 0 until the reply arrived, at [stoppedAt].
    private fun thoughtUntil(stoppedAt: Long) {
        clock.update(Turn, isThinking = true)
        now = stoppedAt
        clock.update(Turn, isThinking = false, hasReplies = true)
    }

    @Test
    fun `the thinking shows nothing of itself so it reads as folded`() {
        clock.update(Turn, isThinking = true)

        show { AIThinking() }

        assertEquals("Thinking…", description)
        assertEquals("Collapsed", openState)
    }

    @Test
    fun `the first step takes over the turn's thinking`() {
        clock.update(Turn, isThinking = true)
        now = 3000
        var part by mutableStateOf(step(streaming = true))
        show { StreamingReasoning(part = part) }

        now = 8000
        clock.update(Turn, isThinking = false, hasReplies = true)
        part = step(durationMs = 2000)
        frame()

        assertEquals("Thought for 8s", description)
    }

    @Test
    fun `a later step keeps its own time`() {
        clock.update(Turn, isThinking = true)
        clock.isFirstStep("r1", Turn)
        now = 8000

        show { StreamingReasoning(part = step(id = "r2", durationMs = 3000)) }

        assertEquals("Thought for 3s", description)
    }

    @Test
    fun `outside a conversation a step keeps its own time`() {
        thoughtUntil(stoppedAt = 8000)

        show(inTurn = false) { StreamingReasoning(part = step(durationMs = 3000)) }

        assertEquals("Thought for 3s", description)
        assertEquals("Collapsed", openState)
    }

    @Test
    fun `a first step that arrives done opens then folds before the answer`() {
        thoughtUntil(stoppedAt = 8000)
        now = 8100

        show { StreamingReasoning(part = step()) }
        frame()

        assertEquals("Thought for 8s", description)
        assertEquals("Expanded", openState)

        rule.mainClock.advanceTimeBy(AIThinkingClock.FINISHED_REASONING_SHOWN_MILLIS)
        frame()

        assertEquals("Collapsed", openState)
    }

    @Test
    fun `a first step that arrives later stays folded`() {
        thoughtUntil(stoppedAt = 8000)
        now = 12_000

        show { StreamingReasoning(part = step()) }
        frame()

        assertEquals("Collapsed", openState)
    }

    @Test
    fun `a first step the reader opened stays open`() {
        thoughtUntil(stoppedAt = 8000)
        now = 8100
        show { StreamingReasoning(part = step()) }
        frame()

        header.performClick()
        frame()
        header.performClick()
        frame()
        rule.mainClock.advanceTimeBy(AIThinkingClock.FINISHED_REASONING_SHOWN_MILLIS)
        frame()

        assertEquals("Expanded", openState)
    }

    @Test
    fun `a different step shown in its place starts afresh`() {
        rule.mainClock.autoAdvance = true
        var part by mutableStateOf(step(id = "r1", durationMs = 2000))
        show(inTurn = false) { StreamingReasoning(part = part) }
        header.performClick()
        rule.waitForIdle()
        assertEquals("Expanded", openState)

        part = step(id = "r2", durationMs = 3000)
        rule.waitForIdle()

        assertEquals("Collapsed", openState)
    }

    private companion object {
        const val Turn = "q1"
    }
}
