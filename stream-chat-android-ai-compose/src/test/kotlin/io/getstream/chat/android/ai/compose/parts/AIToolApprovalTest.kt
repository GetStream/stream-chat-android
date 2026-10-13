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

package io.getstream.chat.android.ai.compose.parts

import io.getstream.chat.android.ai.compose.ui.component.approvalLines
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class AIToolApprovalTest {

    private fun call(json: String): AIToolCallPart = AIMessagePart.fromJson("ai_tool_call", json)!!.toolCall!!

    @Test
    fun `a call that asks first carries its question`() {
        val waiting = call(
            """
                {"id":"toolu_01A","name":"athena_device_location","status":"awaiting_approval","executor":"client",
                "target_user_id":"u_1","target_client_id":"ios-1","approval":{"title":"Share your location?",
                "message":"Only your city is shared.","reason":"to check the local weather",
                "allow_title":"Share location","decline_title":"Don't share"}}
            """.trimIndent(),
        )

        assertEquals(AIToolCallPart.Status.AwaitingApproval, waiting.status)
        assertEquals(
            AIToolApproval(
                title = "Share your location?",
                message = "Only your city is shared.",
                reason = "to check the local weather",
                allowTitle = "Share location",
                declineTitle = "Don't share",
            ),
            waiting.approval,
        )
        assertTrue(waiting.isAwaitingApproval(userId = "u_1", clientId = "ios-1"))
        assertFalse("a client tool is answered on its install", waiting.isAwaitingApproval(userId = "u_1", clientId = "ios-2"))
        assertFalse(waiting.isAwaitingApproval(userId = "u_2", clientId = "ios-1"))
        assertFalse("the device doesn't run it yet", waiting.isAwaiting(userId = "u_1", clientId = "ios-1"))
        assertFalse(waiting.status.isFinished)
        assertEquals(listOf("To check the local weather.", "Only your city is shared."), approvalLines(waiting.approval!!))
    }

    @Test
    fun `a server tool's question is answered from any of the person's devices`() {
        val waiting = call(
            """
                {"id":"call-1","name":"send_email","status":"awaiting_approval","target_user_id":"u_1",
                "approval":{"title":"Send this email?"}}
            """.trimIndent(),
        )

        assertTrue(waiting.isAwaitingApproval(userId = "u_1", clientId = "web-1"))
        assertNull("the card shows its own, translated title", waiting.approval?.allowTitle)
        assertNull(waiting.approval?.declineTitle)
        assertEquals(emptyList<String>(), approvalLines(waiting.approval!!))
    }

    @Test
    fun `answered questions say how and a declined call is over`() {
        val allowed = call(
            """
                {"id":"toolu_01A","status":"awaiting_client","executor":"client","target_user_id":"u_1",
                "target_client_id":"ios-1","approval":{"title":"Share your location?","decision":"allowed"}}
            """.trimIndent(),
        )
        assertEquals(AIToolApproval.Decision.Allowed, allowed.approval?.decision)
        assertFalse(allowed.isDeclined)
        assertTrue("allowed, the device runs it", allowed.isAwaiting(userId = "u_1", clientId = "ios-1"))
        assertFalse(allowed.isAwaitingApproval(userId = "u_1", clientId = "ios-1"))

        val declined = call(
            """
                {"id":"toolu_01B","status":"cancelled","summary":"Location not shared",
                "approval":{"title":"Share your location?","decision":"declined"}}
            """.trimIndent(),
        )
        assertTrue(declined.isDeclined)
        assertTrue(declined.status.isFinished)
    }

    @Test
    fun `a question without a title asks nothing`() {
        val odd = call("""{"id":"toolu_01A","status":"awaiting_approval","target_user_id":"u_1","approval":{"message":"?"}}""")
        assertNull(odd.approval)
        assertFalse(odd.isAwaitingApproval(userId = "u_1", clientId = "ios-1"))

        val notAnObject = call("""{"id":"toolu_01A","status":"awaiting_approval","approval":"yes"}""")
        assertNull(notAnObject.approval)
    }

    @Test
    fun `a reason reads as a sentence`() {
        assertEquals(listOf("Why not?"), approvalLines(AIToolApproval(title = "t", reason = "  why not? ")))
        assertEquals(listOf("Checking…"), approvalLines(AIToolApproval(title = "t", reason = "checking…")))
        assertEquals(emptyList<String>(), approvalLines(AIToolApproval(title = "t", reason = "   ")))
    }
}
