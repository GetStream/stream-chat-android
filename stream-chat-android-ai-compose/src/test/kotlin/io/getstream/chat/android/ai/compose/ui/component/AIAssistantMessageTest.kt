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
}
