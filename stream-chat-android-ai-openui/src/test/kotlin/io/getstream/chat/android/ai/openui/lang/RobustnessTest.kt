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
import org.junit.jupiter.api.Timeout
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
    fun `nested components in a runtime root stay within bounds`() {
        val levels = 40
        val program = "root = \$flag ? " + "Stack([".repeat(levels) + "TextContent(\"x\")" + "])".repeat(levels) +
            " : TextContent(\"y\")"

        val result = parser.parse(program)

        assertNotNull(result.rootExpression)
    }

    @Test
    fun `nested calls in query args stay within bounds`() {
        val levels = 40
        val program = "root = Stack([TextContent(\"x\")])\n" +
            "q = Query(\"tool\", {n: " + "@Count(".repeat(levels) + "1" + ")".repeat(levels) + "})"

        val result = parser.parse(program)

        assertEquals(1, result.queryStatements.size)
    }

    @Test
    @Timeout(10)
    fun `nested calls in query args serialize in linear size`() {
        val levels = 40
        val program = "root = Stack([TextContent(\"x\")])\n" +
            "q = Query(\"tool\", {n: " + "@Count(".repeat(levels) + "1" + ")".repeat(levels) + "})"
        assertLinear(levels) { parser.parse(program).queryStatements.single().argsAST!! }
    }

    @Test
    @Timeout(10)
    fun `nested components in a runtime root serialize in linear size`() {
        val levels = 40
        val program = "root = \$flag ? " + "Stack([".repeat(levels) + "TextContent(\"x\")" + "])".repeat(levels) +
            " : TextContent(\"y\")"
        assertLinear(levels) { parser.parse(program).rootExpression!! }
    }

    @Test
    @Timeout(10)
    fun `nested calls in element props serialize in linear size`() {
        val levels = 40
        val program = "root = Stack([TextContent(" + "@Count(".repeat(levels) + "\$v" + ")".repeat(levels) + ")])"
        val root = OpenUIValue.Element(parser.parse(program).root!!)

        assertTrue(OpenUIJson.stringify(root).length < MAX_CHARS_PER_LEVEL * levels)
        assertTrue(root.plainJson.toString().length < MAX_CHARS_PER_LEVEL * levels)
        assertEquals(root, OpenUIValue.Element(parser.parse(program).root!!))
    }

    /** Serializing, printing, hashing and comparing what [parse] returns stays proportional to its size. */
    private fun assertLinear(levels: Int, parse: () -> ASTNode) {
        val node = parse()
        // A separate parse, so comparing can't stop at the same instance.
        val other = parse()
        val expression = OpenUIValue.Expression(node)
        val json = OpenUIJson.stringify(expression)

        assertTrue(json.length < MAX_CHARS_PER_LEVEL * levels, "${json.length} chars")
        assertEquals(json, expression.plainJson.toString())
        assertEquals(json, expression.toString())
        assertTrue(node.toString().length < MAX_CHARS_PER_LEVEL * levels)
        assertEquals(node.hashCode(), other.hashCode())
        assertEquals(node, other)
        // Every mapped prop is the arg itself, not a copy of it.
        var mapped = 0
        node.walk { call ->
            if (call !is ASTNode.Comp) return@walk
            call.mappedProps?.values?.forEach {
                assertTrue(call.argIndexOf(it) != -1)
                mapped++
            }
        }
        assertTrue(mapped >= levels)
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

    private companion object {
        // A level writes a call, its args and a reference per mapped prop: about a hundred chars.
        const val MAX_CHARS_PER_LEVEL = 200
    }
}
