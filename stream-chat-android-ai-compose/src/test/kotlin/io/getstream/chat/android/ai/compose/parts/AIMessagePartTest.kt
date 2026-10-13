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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class AIMessagePartTest {

    @Test
    fun `steps decode in order and other attachments are skipped`() {
        val parts = AIMessagePart.partsFromJson(
            listOf(
                "ai_reasoning" to """
                    {"type":"ai_reasoning","v":1,"id":"r1","status":"completed",
                    "summary":"Needs the user's location first","duration_ms":3200}
                """.trimIndent(),
                "athena_image" to """{"artifact_id":"a1"}""",
                "ai_tool_call" to """
                    {"type":"ai_tool_call","v":1,"id":"toolu_01A","name":"get_location",
                    "display_title":"Checking your location","status":"awaiting_client","executor":"client",
                    "target_user_id":"u_123","target_client_id":"ios-7F3A","arguments":{"accuracy":"city","level":2}}
                """.trimIndent(),
                "ai_reasoning" to """{"id":"r2","status":"streaming","preview":"Now that I know the city…"}""",
            ),
        )

        assertEquals(listOf("r1", "toolu_01A", "r2"), parts.map { it.id })
        assertEquals(
            listOf(AIMessagePart.Kind.Reasoning, AIMessagePart.Kind.ToolCall, AIMessagePart.Kind.Reasoning),
            parts.map { it.kind },
        )
        val first = parts[0].reasoning!!
        val call = parts[1].toolCall!!
        val live = parts[2].reasoning!!
        assertNull(parts[0].toolCall)
        assertEquals(AIReasoningPart.Status.Completed, first.status)
        assertEquals("Needs the user's location first", first.summary)
        assertEquals(3.2, first.durationSeconds!!, 0.0001)
        assertEquals(AIToolCallPart.Status.AwaitingClient, call.status)
        assertEquals(AIToolCallPart.Executor.Client, call.executor)
        assertEquals("Checking your location", call.displayTitle)
        assertTrue(call.isAwaiting(userId = "u_123", clientId = "ios-7F3A"))
        assertFalse(call.isAwaiting(userId = "u_123", clientId = "ios-OTHER"))
        assertFalse(call.isAwaiting(userId = "u_456", clientId = "ios-7F3A"))
        assertEquals("""{"accuracy":"city","level":2}""", call.arguments)
        assertTrue(live.isStreaming)
        assertEquals("Now that I know the city…", live.preview)
    }

    @Test
    fun `new kinds and statuses never break decoding`() {
        val parts = AIMessagePart.partsFromJson(
            listOf(
                "ai_tool_call" to """{"id":"c1","name":"search","status":"paused_for_review","duration_ms":"fast"}""",
                "ai_reasoning" to "not json",
                "ai_tool_call" to """{"id":"c2","v":2,"name":"future"}""",
                "ai_citation" to """{"id":"s1","url":"https://getstream.io","title":"Stream"}""",
            ),
        )

        val call = parts[0].toolCall!!
        assertEquals("an unknown status keeps its value", "paused_for_review", call.status.rawValue)
        assertFalse(call.status.isFinished)
        assertEquals(AIToolCallPart.Executor.Server, call.executor)
        assertNull("a field of the wrong type reads as missing", call.durationMs)

        val broken = parts[1].reasoning!!
        assertEquals("a step without an ID is known by its position", "ai_reasoning-1", broken.id)
        assertEquals(AIReasoningPart.Status.Completed, broken.status)

        assertEquals(AIMessagePart.Kind.ToolCall, parts[2].kind)
        assertEquals(2, parts[2].version)
        assertNull("a newer format of a known kind has no typed view", parts[2].toolCall)
        assertFalse(parts[2].isSupported)

        assertEquals(AIMessagePart.Kind("ai_citation"), parts[3].kind)
        assertFalse(parts[3].isSupported)
        assertEquals("a kind of your own reads from its payload", "Stream", parts[3].payload["title"])
    }

    @Test
    fun `fields read leniently`() {
        val call = AIMessagePart.from(
            type = "ai_tool_call",
            payload = mapOf(
                "id" to "c1",
                "name" to "",
                "status" to "",
                "duration_ms" to true,
                "display_title" to 7,
                "arguments" to "a string",
                "summary" to 2.5,
            ),
        )!!.toolCall!!

        assertEquals("an empty string reads as missing", "", call.name)
        assertEquals(AIToolCallPart.Status.Running, call.status)
        assertNull("booleans are not numbers", call.durationMs)
        assertNull(call.displayTitle)
        assertNull("only objects and arrays are arguments", call.arguments)
        assertNull(call.summary)

        val long = AIMessagePart.from("ai_reasoning", mapOf("duration_ms" to 4_000L, "v" to 1.0))!!.reasoning!!
        assertEquals(4_000, long.durationMs)
    }

    @Test
    fun `parts compare by kind, identity, version and payload`() {
        val one = AIMessagePart.fromJson("ai_tool_call", """{"id":"c1","status":"running"}""")
        val same = AIMessagePart.fromJson("ai_tool_call", """{"status":"running","id":"c1"}""")
        val moved = AIMessagePart.fromJson("ai_tool_call", """{"id":"c1","status":"completed"}""")

        assertEquals(one, same)
        assertEquals(one.hashCode(), same.hashCode())
        assertNotEquals(one, moved)
        assertNull("only ai_ attachments are steps", AIMessagePart.fromJson("image", "{}"))
    }

    @Test
    fun `arguments keep nested values with sorted keys`() {
        val call = AIMessagePart.fromJson(
            "ai_tool_call",
            """{"id":"c1","arguments":{"z":[1,{"b":true,"a":null}],"a":"x/y \"q\"\n"}}""",
        )!!.toolCall!!

        assertEquals("""{"a":"x/y \"q\"\n","z":[1,{"a":null,"b":true}]}""", call.arguments)
    }
}
