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

package io.getstream.chat.android.ai.openui.lang

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/** Tool providers: function maps and MCP results. */
internal class ToolProviderTest {
    private fun text(vararg texts: String?) = texts.map { McpToolResult.Content("text", it) }

    @Test
    fun `a function provider calls the handler for a tool, and names the tools it has otherwise`() = runTest {
        val tools = FunctionToolProvider("echo" to { args -> args["q"] ?: OpenUIValue.Null })
        assertEquals(v("hi"), tools.callTool("echo", OpenUIObject.of("q" to v("hi"))))

        val error = assertThrows<ToolNotFoundException> { tools.callTool("missing", OpenUIObject.Empty) }
        assertEquals("missing", error.toolName)
        assertEquals(listOf("echo"), error.availableTools)
        assertEquals("[openui] No handler for tool \"missing\". Available: echo", error.message)
        assertEquals(
            "[openui] No handler for tool \"x\". Available: (none)",
            ToolNotFoundException("x").message,
        )
    }

    @Test
    fun `MCP results prefer structured content, then JSON text, then raw text`() {
        assertEquals(v(1), extractToolResult(McpToolResult(text("ignored"), structuredContent = v(1))))
        assertEquals(obj("a" to 1), extractToolResult(McpToolResult(text("{\"a\":", "1}"))))
        assertEquals(v("plain"), extractToolResult(McpToolResult(text("plain"), structuredContent = OpenUIValue.Null)))
        assertEquals(v(""), extractToolResult(McpToolResult(text(null))))
        assertEquals(OpenUIValue.Null, extractToolResult(McpToolResult(listOf(McpToolResult.Content("image")))))
    }

    @Test
    fun `MCP error results throw with their text`() {
        val error = assertThrows<McpToolException> {
            extractToolResult(McpToolResult(text("bad", "input"), isError = true))
        }
        assertEquals("bad\ninput", error.toolErrorText)
        assertEquals("MCP tool error: bad\ninput", error.message)
        assertEquals("MCP tool error: Unknown error", McpToolException("").message)
    }

    @Test
    fun `an MCP provider extracts the client's results`() = runTest {
        val provider = McpToolProvider { name, args -> McpToolResult(text("{\"tool\":\"$name\",\"n\":${args["n"]}}")) }
        assertEquals(obj("tool" to "sum", "n" to 2), provider.callTool("sum", OpenUIObject.of("n" to v(2))))
        val failing = McpToolProvider { _, _ -> McpToolResult(text("nope"), isError = true) }
        assertThrows<McpToolException> { failing.callTool("sum", OpenUIObject.Empty) }
    }

    @Test
    fun `MCP errors become query errors`() = runTest {
        val manager = QueryManager(McpToolProvider { _, _ -> McpToolResult(text("nope"), isError = true) }, this)
        manager.evaluateQueries(listOf(QueryManager.QueryNode("data", "list", obj(), OpenUIValue.Null)))
        testScheduler.advanceUntilIdle()
        val error = manager.snapshot.value.errors.single()
        assertEquals("mcp-error", error.code)
        assertEquals("Query \"list\" returned an error: nope", error.message)
    }
}
