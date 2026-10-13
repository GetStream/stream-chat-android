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

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.parts.AIMessagePart
import io.getstream.chat.android.ai.compose.parts.AIToolCallPart
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ReplyStepsSemanticsTest {

    @get:Rule
    val rule = createComposeRule()

    private fun toolCall(fields: String): AIToolCallPart =
        requireNotNull(AIMessagePart.fromJson("ai_tool_call", """{"id":"t1","name":"web_search",$fields}""")?.toolCall)

    // Everything TalkBack reads for the merged tool call, in order.
    private fun SemanticsNodeInteraction.spoken(): List<String> {
        val config = fetchSemanticsNode().config
        return config.getOrNull(SemanticsProperties.ContentDescription).orEmpty() +
            config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }
    }

    @Test
    fun `a tool call with an em line height renders`() {
        rule.setContent {
            AIToolCall(
                part = toolCall(""""status":"running""""),
                textStyle = MaterialTheme.typography.bodyMedium.copy(lineHeight = 1.5.em),
            )
        }

        rule.onRoot().assertExists()
    }

    @Test
    fun `TalkBack hears a running call's status`() {
        rule.setContent { AIToolCall(part = toolCall(""""status":"running"""")) }

        assertEquals(listOf("In progress", "web search"), rule.onRoot().onChildAt(0).spoken())
    }

    @Test
    fun `TalkBack hears a completed call's status with its summary`() {
        rule.setContent { AIToolCall(part = toolCall(""""status":"completed","summary":"Found 3 results"""")) }

        assertEquals(listOf("Completed", "web search", "Found 3 results"), rule.onRoot().onChildAt(0).spoken())
    }

    @Test
    fun `TalkBack hears a failed call's status once`() {
        rule.setContent { AIToolCall(part = toolCall(""""status":"failed"""")) }

        assertEquals(listOf("web search", "Didn't complete"), rule.onRoot().onChildAt(0).spoken())
    }

    @Test
    fun `the reasoning header says what a tap does and whether it is open`() {
        rule.setContent { StreamingReasoning(text = "Weighing options.", isThinking = false, durationSeconds = 4.0) }
        val header = rule.onNode(hasClickAction())

        header.assertHeightIsAtLeast(48.dp)
        assertEquals("show reasoning", header.fetchSemanticsNode().config[SemanticsActions.OnClick].label)
        assertEquals("Collapsed", header.fetchSemanticsNode().config[SemanticsProperties.StateDescription])

        header.performClick()

        assertEquals("hide reasoning", header.fetchSemanticsNode().config[SemanticsActions.OnClick].label)
        assertEquals("Expanded", header.fetchSemanticsNode().config[SemanticsProperties.StateDescription])
    }
}
