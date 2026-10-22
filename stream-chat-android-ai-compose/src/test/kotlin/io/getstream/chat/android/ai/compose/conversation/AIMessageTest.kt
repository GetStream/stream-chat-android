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

import io.getstream.chat.android.ai.compose.parts.AIReasoningPart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class AIMessageTest {

    @Test
    fun `a message that gives only its id, role and text is done and carries nothing else`() {
        val message = object : AIMessage {
            override val id = "m1"
            override val role = AIMessageRole.Assistant
            override val text = "Hello"
        }

        assertFalse(message.isGenerating)
        assertTrue(message.parts.isEmpty())
        assertTrue(message.attachments.isEmpty())
        assertNull(message.reasoningText(AIReasoningPart(id = "r1", status = AIReasoningPart.Status.Completed)))
    }

    @Test
    fun `a role is an open set named by its raw value`() {
        assertEquals("user", AIMessageRole.User.toString())
        assertEquals("assistant", AIMessageRole.Assistant.rawValue)
        assertEquals(AIMessageRole("system"), AIMessageRole("system"))
    }
}
