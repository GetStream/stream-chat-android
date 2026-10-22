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

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.parts.AIMessagePart
import io.getstream.chat.android.ai.compose.ui.component.internal.AIThinkingClock
import io.getstream.chat.android.ai.compose.ui.component.internal.AIThinkingTurn
import io.getstream.chat.android.ai.compose.ui.component.internal.LocalAIThinkingTurn
import io.getstream.chat.android.ai.compose.ui.component.internal.LocalAnswerWriting
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AIAssistantMessageTest {

    @get:Rule
    val rule = createComposeRule()

    private val reports = mutableListOf<Boolean>()

    @Test
    fun `a finished reply reports it is not being written`() {
        rule.setContent {
            CompositionLocalProvider(LocalAnswerWriting provides { reports += it }) {
                AIAssistantMessage(text = "Sunny, 24°C.")
            }
        }

        rule.runOnIdle { assertEquals(listOf(false), reports) }
    }

    @Test
    fun `a reply stays written a moment after the agent finishes it`() {
        var generating by mutableStateOf(true)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalAnswerWriting provides { reports += it }) {
                AIAssistantMessage(text = "Sunny, 24°C.", isGenerating = generating)
            }
        }
        rule.mainClock.advanceTimeByFrame()
        assertEquals(listOf(true), reports)

        generating = false
        rule.mainClock.advanceTimeBy(50)
        assertEquals("still writing within the tail", listOf(true), reports)

        rule.mainClock.advanceTimeBy(100)
        assertEquals(listOf(true, false), reports)
    }

    @Test
    fun `generating again cancels the tail`() {
        var generating by mutableStateOf(true)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalAnswerWriting provides { reports += it }) {
                AIAssistantMessage(text = "Sunny", isGenerating = generating)
            }
        }
        rule.mainClock.advanceTimeByFrame()

        generating = false
        rule.mainClock.advanceTimeBy(50)
        generating = true
        rule.mainClock.advanceTimeBy(200)

        assertEquals(listOf(true), reports)
    }

    @Test
    fun `a long reply that finishes at once stays written until all of it shows`() {
        val words = List(33) { "word$it" }.joinToString(" ")
        var text by mutableStateOf("")
        var generating by mutableStateOf(true)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalAnswerWriting provides { reports += it }) {
                AIAssistantMessage(text = text, isGenerating = generating)
            }
        }
        rule.mainClock.advanceTimeByFrame()

        text = words
        generating = false
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(600)
        rule.waitForIdle()
        assertEquals("still typing after the tail", listOf(true), reports)
        rule.onNodeWithText(words).assertDoesNotExist()

        rule.mainClock.advanceTimeBy(5_000)
        rule.waitForIdle()
        rule.onNodeWithText(words).assertExists()
        assertEquals(listOf(true, false), reports)
    }

    @Test
    fun `attachments show through the factory after the answer`() {
        val factory = RecordingAttachmentFactory()
        rule.setContent {
            CompositionLocalProvider(LocalChatAiComponentFactory provides factory) {
                AIAssistantMessage(
                    text = "Here is your itinerary.",
                    attachments = listOf(AIMessageAttachment.file("https://example.com/itinerary.pdf".toUri())),
                )
            }
        }

        rule.onNodeWithText("Here is your itinerary.").assertExists()
        rule.onNodeWithText("assistant: itinerary.pdf").assertExists()
        rule.runOnIdle { assertEquals(setOf(AIMessageRole.Assistant), factory.roles()) }
    }

    @Test
    fun `an answer that arrives with a finished first step waits for it to fold`() {
        val clock = thoughtUntil(stoppedAt = 8000)
        now = 8100
        rule.mainClock.autoAdvance = false
        showInTurn(clock)
        rule.mainClock.advanceTimeByFrame()

        rule.onNodeWithText(Answer).assertDoesNotExist()

        val untilAnswer = AIThinkingClock.FINISHED_REASONING_SHOWN_MILLIS + StreamingReasoningDefaults.FoldDurationMillis
        now += untilAnswer
        rule.mainClock.advanceTimeBy(untilAnswer + 2000)

        rule.onNodeWithText(Answer).assertExists()
    }

    @Test
    fun `an answer made again after its step folded shows at once`() {
        val clock = thoughtUntil(stoppedAt = 8000)
        now = 8100
        clock.isFirstStep("r1", Turn)
        clock.showsFirstStep(Turn)
        now = 60_000
        rule.mainClock.autoAdvance = false
        showInTurn(clock)
        rule.mainClock.advanceTimeByFrame()

        rule.onNodeWithText(Answer).assertExists()
    }

    private var now = 0L

    // The turn thought from 0 until the reply arrived, at [stoppedAt].
    private fun thoughtUntil(stoppedAt: Long): AIThinkingClock {
        val clock = AIThinkingClock { now }
        clock.update(Turn, isThinking = true)
        now = stoppedAt
        clock.update(Turn, isThinking = false, hasReplies = true)
        return clock
    }

    private fun showInTurn(clock: AIThinkingClock) {
        val step = AIMessagePart.from(
            type = "ai_reasoning",
            payload = mapOf("id" to "r1", "status" to "completed", "preview" to "Weighing the options."),
        )!!
        rule.setContent {
            CompositionLocalProvider(LocalAIThinkingTurn provides AIThinkingTurn(clock, Turn)) {
                AIAssistantMessage(text = Answer, parts = listOf(step))
            }
        }
    }

    private companion object {
        const val Turn = "q1"
        const val Answer = "Sunny, 24°C."
    }
}
