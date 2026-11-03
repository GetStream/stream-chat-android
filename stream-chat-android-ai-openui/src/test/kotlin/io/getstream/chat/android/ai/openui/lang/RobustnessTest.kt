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

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Model output that must not crash the parser or lose statements. */
internal class RobustnessTest {
    private val parser: Parser
        get() = Fixtures.libraries.getValue("playground").let { (paramMap, root) -> Parser(paramMap, root) }

    @Test
    fun `deeply nested arrays do not overflow the stack`() {
        val depth = 20_000
        val program = "root = Stack([" + "[".repeat(depth) + "TextContent(\"x\")" + "]".repeat(depth) + "])"

        val result = parser.parse(program)

        assertEquals("Stack", result.root?.typeName)
    }

    @Test
    fun `deeply nested expressions do not overflow the stack`() {
        val depth = 20_000
        val program = "root = Stack([TextContent(" + "(".repeat(depth) + "\"x\"" + ")".repeat(depth) + ")])"

        val result = parser.parse(program)

        assertEquals("Stack", result.root?.typeName)
    }

    @Test
    fun `long reference chains do not overflow the stack`() {
        val count = 5_000
        val lines = listOf("root = Stack([a0])") +
            (0 until count).map { "a$it = Stack([a${it + 1}])" } +
            "a$count = TextContent(\"x\")"

        val result = parser.parse(lines.joinToString("\n"))

        assertEquals("Stack", result.root?.typeName)
    }

    @Test
    fun `long operator and member chains do not overflow the stack`() {
        val count = 20_000
        val programs = listOf(
            "root = Stack([TextContent(" + List(count) { "1" }.joinToString("+") + ")])",
            "root = Stack([TextContent(x" + ".a".repeat(count) + ")])",
            "root = Stack([TextContent(" + List(count) { "\$v" }.joinToString("+") + ")])",
        )

        programs.forEach { program ->
            assertEquals("Stack", parser.parse(program).root?.typeName)
        }
    }

    @Test
    fun `statements that reference one another twice stay within bounds`() {
        val levels = 40
        val lines = listOf("root = Stack([a1])") +
            (1..levels).map { "a$it = Stack([a${it + 1}, a${it + 1}])" } +
            "a${levels + 1} = TextContent(\"x\")"

        val result = parser.parse(lines.joinToString("\n"))

        assertEquals("Stack", result.root?.typeName)
    }

    @Test
    fun `a chain within the limits is kept`() {
        val result = parser.parse("root = Stack([TextContent(" + List(100) { "\"a\"" }.joinToString("+") + ")])")

        assertTrue(result.meta.errors.isEmpty())
        assertEquals("Stack", result.root?.typeName)
    }

    @Test
    fun `nesting within the limits is kept`() {
        val program = "root = Stack([" + "Stack([".repeat(50) + "TextContent(\"deep\")" + "])".repeat(50) + "])"

        val result = parser.parse(program)

        assertTrue(OpenUIJson.stringify(OpenUIValue.Element(result.root!!)).contains("deep"))
        assertTrue(result.meta.errors.isEmpty())
    }

    @Test
    fun `windows line endings with comments keep every statement`() {
        val program = "root = Stack([a, b]) // page\r\na = TextContent(\"A\")\r\n# note\r\nb = TextContent(\"B\")"

        val result = parser.parse(program)

        assertEquals(2, result.root?.props?.get("children").arrayValue?.size)
        assertTrue(result.meta.unresolved.isEmpty())
    }

    @Test
    fun `stripComments splits on line feeds like JavaScript`() {
        assertEquals("a = 1\nb = 2", Preprocessor.stripComments("a = 1 // x\r\nb = 2"))
    }

    @Test
    fun `non-finite numbers serialize as null`() {
        val result = parser.parse("root = Stack([TextContent(\"x\")], 1e999)")

        assertEquals("Stack", result.root?.typeName)
        assertTrue(!OpenUIJson.stringify(result.jsonValue).contains("Infinity"))
    }
}
