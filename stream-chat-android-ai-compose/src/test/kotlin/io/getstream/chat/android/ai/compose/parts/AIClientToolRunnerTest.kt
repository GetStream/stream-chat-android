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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
internal class AIClientToolRunnerTest {

    private class CountingTool : AIClientTool {
        override val name = "athena_device_location"
        var runs = 0

        override suspend fun run(call: AIToolCallPart): AIClientToolResult {
            runs += 1
            return AIClientToolResult.completed("""{"city":"Skopje"}""", summary = "Shared approximate location")
        }
    }

    private fun awaiting(
        id: String = "toolu_01A",
        user: String = "u_1",
        client: String = "ios-1",
        name: String = "athena_device_location",
    ): List<AIMessagePart> = listOfNotNull(
        AIMessagePart.fromJson(
            "ai_tool_call",
            """
                {"id":"$id","name":"$name","status":"awaiting_client","executor":"client",
                "target_user_id":"$user","target_client_id":"$client"}
            """.trimIndent(),
        ),
    )

    @Test
    fun `a call runs once and only on the device it is addressed to`() = runTest {
        val tool = CountingTool()
        val runner = AIClientToolRunner(userId = "u_1", clientId = "ios-1", tools = listOf(tool), scope = this)
        val sent = mutableListOf<AIClientToolResult>()
        val send: suspend (AIToolCallPart, AIClientToolResult) -> Unit = { _, result -> sent += result }

        runner.run(awaiting(), send)
        runner.run(awaiting(), send)
        advanceUntilIdle()
        runner.run(awaiting(), send)
        runner.run(awaiting("toolu_02", client = "ios-2"), send)
        runner.run(awaiting("toolu_03", user = "u_2"), send)
        runner.run(awaiting("toolu_04", name = "unknown_tool"), send)
        advanceUntilIdle()

        assertEquals(1, tool.runs)
        assertEquals(1, sent.size)
        assertEquals("Shared approximate location", sent.first().summary)
        assertEquals("""{"city":"Skopje"}""", sent.first().output)
    }

    @Test
    fun `a result that could not be sent is sent again without running the tool again`() = runTest {
        val tool = CountingTool()
        val runner = AIClientToolRunner(userId = "u_1", clientId = "ios-1", tools = listOf(tool), scope = this)
        var attempts = 0
        val send: suspend (AIToolCallPart, AIClientToolResult) -> Unit = { _, _ ->
            attempts += 1
            if (attempts == 1) throw IOException("offline")
        }

        runner.run(awaiting(), send)
        advanceUntilIdle()
        runner.run(awaiting(), send)
        advanceUntilIdle()
        runner.run(awaiting(), send)
        advanceUntilIdle()

        assertEquals(1, tool.runs)
        assertEquals(2, attempts)
    }

    @Test
    fun `a result is offered at most maxAttempts times`() = runTest {
        val tool = CountingTool()
        val runner = AIClientToolRunner(userId = "u_1", clientId = "ios-1", tools = listOf(tool), scope = this)
        runner.maxAttempts = 2
        var attempts = 0
        val send: suspend (AIToolCallPart, AIClientToolResult) -> Unit = { _, _ ->
            attempts += 1
            throw IOException("offline")
        }

        repeat(4) {
            runner.run(awaiting(), send)
            advanceUntilIdle()
        }

        assertEquals(1, tool.runs)
        assertEquals(2, attempts)
    }

    @Test
    fun `the runner lists its tools and uses the first of a name`() = runTest {
        val first = CountingTool()
        val second = CountingTool()
        val runner = AIClientToolRunner(userId = "u_1", clientId = "ios-1", tools = listOf(first, second), scope = this)

        runner.run(awaiting()) { _, _ -> }
        advanceUntilIdle()

        assertEquals(listOf("athena_device_location"), runner.toolNames)
        assertEquals(1, first.runs)
        assertEquals(0, second.runs)
    }

    @Test
    fun `a tool that throws is reported as failed`() = runTest {
        val tool = object : AIClientTool {
            override val name = "athena_device_location"
            override suspend fun run(call: AIToolCallPart): AIClientToolResult = error("GPS unavailable")
        }
        val runner = AIClientToolRunner(userId = "u_1", clientId = "ios-1", tools = listOf(tool), scope = this)
        val sent = mutableListOf<AIClientToolResult>()

        runner.run(awaiting()) { _, result -> sent += result }
        advanceUntilIdle()

        assertEquals(1, sent.size)
        assertNull(sent.first().output)
        assertTrue(sent.first().failure != null)
    }

    @Test
    fun `a tool that times out is reported as failed`() = runTest {
        val tool = object : AIClientTool {
            override val name = "athena_device_location"
            override suspend fun run(call: AIToolCallPart): AIClientToolResult =
                withTimeout(10) {
                    delay(1_000)
                    AIClientToolResult.completed("{}")
                }
        }
        val runner = AIClientToolRunner(userId = "u_1", clientId = "ios-1", tools = listOf(tool), scope = this)
        val sent = mutableListOf<AIClientToolResult>()

        runner.run(awaiting()) { _, result -> sent += result }
        advanceUntilIdle()

        assertEquals(1, sent.size)
        assertTrue(sent.first().failure != null)
    }

    @Test
    fun `nothing is sent when the runner's scope is cancelled during a call`() = runTest {
        val tool = object : AIClientTool {
            override val name = "athena_device_location"
            override suspend fun run(call: AIToolCallPart): AIClientToolResult {
                delay(1_000)
                return AIClientToolResult.completed("{}")
            }
        }
        val job = Job(coroutineContext[Job])
        val runner = AIClientToolRunner(userId = "u_1", clientId = "ios-1", tools = listOf(tool), scope = this + job)
        val sent = mutableListOf<AIClientToolResult>()

        runner.run(awaiting()) { _, result -> sent += result }
        runCurrent()
        job.cancel()
        advanceUntilIdle()

        assertTrue(sent.isEmpty())
    }

    @Test
    fun `a call without its own id is not run, as its result could not be matched`() = runTest {
        val tool = CountingTool()
        val runner = AIClientToolRunner(userId = "u_1", clientId = "ios-1", tools = listOf(tool), scope = this)
        val withoutId = listOfNotNull(
            AIMessagePart.fromJson(
                "ai_tool_call",
                """
                    {"name":"athena_device_location","status":"awaiting_client","executor":"client",
                    "target_user_id":"u_1","target_client_id":"ios-1"}
                """.trimIndent(),
            ),
        )

        runner.run(withoutId) { _, _ -> }
        advanceUntilIdle()

        assertEquals(0, tool.runs)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `maxAttempts below 1 is rejected`() = runTest {
        AIClientToolRunner(userId = "u_1", clientId = "ios-1", tools = emptyList(), scope = this).maxAttempts = 0
    }
}
