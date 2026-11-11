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

package io.getstream.chat.android.ai.compose.conversation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class AIConversationTypesTest {

    @Test
    fun `status is answering while submitted or streaming`() {
        assertTrue(AIConversationStatus.Submitted.isAnswering)
        assertTrue(AIConversationStatus.Streaming.isAnswering)
        assertFalse(AIConversationStatus.Ready.isAnswering)
        assertFalse(AIConversationStatus.Error.isAnswering)
        assertFalse(AIConversationStatus("paused").isAnswering)
        assertEquals("streaming", AIConversationStatus.Streaming.toString())
    }

    @Test
    fun `request kinds`() {
        assertEquals("message", AIConversationRequestKind.Message.rawValue)
        assertEquals("regenerate", AIConversationRequestKind.Regenerate.rawValue)
        assertEquals(AIConversationRequestKind("message"), AIConversationRequestKind.Message)
    }

    @Test
    fun `activity states are those of Stream Chat's AI indicator`() {
        assertEquals("AI_STATE_THINKING", AIAgentActivity.State.Thinking.rawValue)
        assertEquals("AI_STATE_EXTERNAL_SOURCES", AIAgentActivity.State.CheckingExternalSources.rawValue)
        assertEquals("AI_STATE_GENERATING", AIAgentActivity.State.Generating.rawValue)
        assertEquals("AI_STATE_ERROR", AIAgentActivity.State.Error.rawValue)
    }

    @Test
    fun `activity from an indicator's raw state`() {
        val activity = AIAgentActivity(AIAgentActivity.State("AI_STATE_THINKING"), text = "Thinking")

        assertEquals(AIAgentActivity.State.Thinking, activity.state)
        assertEquals("Thinking", activity.text)
    }

    @Test
    fun `agent error is what the agent said`() {
        assertEquals("Rate limited", AIAgentError("Rate limited").message)
        assertEquals(AIAgentError("Rate limited"), AIAgentError("Rate limited"))
        assertNotEquals(AIAgentError("Rate limited"), AIAgentError())
    }

    @Test
    fun `agent error without what the agent said`() {
        assertNull(AIAgentError().message)
        assertEquals(AIAgentError(), AIAgentError())
    }
}
