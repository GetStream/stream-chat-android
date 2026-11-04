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

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/** Incremental preprocessing and materialization reuse in [StreamParser]. */
internal class StreamParserTest {

    private val chat = Fixtures.libraries.getValue("chat")

    private fun chatParser() = StreamParser(chat.first, chat.second)

    /** Parses [text] in one go, with nothing kept from earlier text. */
    private fun oneShot(paramMap: ParamMap?, rootName: String?, text: String, complete: Boolean = false) =
        StreamParser(paramMap, rootName).set(text, complete)

    private fun assertSame(expected: ParseResult, actual: ParseResult, message: String) {
        if (expected == actual) return
        fail("$message: ${firstDifference(actual.jsonValue, expected.jsonValue) ?: "results differ"}")
    }

    // Preprocessing ----------------------------------------------------------------------------

    @Test
    fun `preprocessing appended text matches preprocessing it whole`() {
        val random = Random(SEED)
        repeat(PREPROCESSOR_RUNS) {
            val text = StringBuilder()
            val preprocessor = IncrementalPreprocessor()
            var stable = ""
            repeat(random.nextInt(1, MAX_PIECES)) {
                val piece = pieces[random.nextInt(pieces.size)]
                text.append(piece)
                val startedOver = preprocessor.append(piece)
                val preprocessed = preprocessor.text.substring(0, preprocessor.length)
                assertEquals(Preprocessor.preprocess(text.toString()), preprocessed, "$text")
                assertTrue(startedOver || preprocessor.text.startsWith(stable), "$text changed before its last line")
                val lastBreak = preprocessed.lastIndexOf('\n')
                stable = when {
                    lastBreak != -1 -> preprocessed.substring(0, lastBreak + 1)
                    startedOver -> ""
                    else -> stable
                }
            }
        }
    }

    // Equivalence ------------------------------------------------------------------------------

    @TestFactory
    fun `streaming each fixture matches parsing it at once`(): List<DynamicTest> =
        ParserConformanceTest.cases.map { case ->
            DynamicTest.dynamicTest(case.name) {
                val (paramMap, rootName) = Fixtures.libraries.getValue(case.library)
                val input = case.input
                // Every prefix of short inputs; for long ones, spread checkpoints across the input.
                val stride = input.length / MAX_CHECKPOINTS + 1

                val byChar = StreamParser(paramMap, rootName)
                for (end in 1..input.length) {
                    val streamed = byChar.set(input.substring(0, end))
                    if (end % stride == 0 || end == input.length) {
                        val expected = oneShot(paramMap, rootName, input.substring(0, end))
                        assertSame(expected, streamed, "${case.name} @ $end")
                    }
                }

                val random = Random(SEED + case.name.hashCode())
                val byChunk = StreamParser(paramMap, rootName)
                var end = 0
                while (end < input.length) {
                    val next = minOf(input.length, end + random.nextInt(1, MAX_CHUNK))
                    val streamed = byChunk.push(input.substring(end, next))
                    end = next
                    assertSame(oneShot(paramMap, rootName, input.substring(0, end)), streamed, "${case.name} @ $end")
                }

                val whole = Parser(paramMap, rootName).parse(input)
                for (parser in listOf(byChar, byChunk)) {
                    val completed = parser.getResult(complete = true)
                    assertSame(oneShot(paramMap, rootName, input, complete = true), completed, "${case.name} complete")
                    // Parsed at once nothing is reused; only the count of statements read twice differs.
                    val counted = completed.copy(meta = completed.meta.copy(statementCount = whole.meta.statementCount))
                    assertSame(whole, counted, "${case.name} vs parse")
                }
            }
        }

    @TestFactory
    fun `reused statements read as they do fresh`(): List<DynamicTest> =
        reusedStatements.mapIndexed { index, (program, chunkSize) ->
            DynamicTest.dynamicTest("program $index") {
                val streaming = chatParser()
                for (end in chunkSize until program.length + chunkSize step chunkSize) {
                    val prefix = program.substring(0, minOf(end, program.length))
                    assertSame(oneShot(chat.first, chat.second, prefix), streaming.set(prefix), "$index @ $end")
                }
                val streamed = streaming.set(program, complete = true)
                val whole = Parser(chat.first, chat.second).parse(program)
                assertSame(
                    whole,
                    streamed.copy(meta = streamed.meta.copy(statementCount = whole.meta.statementCount)),
                    "$index",
                )
            }
        }

    @Test
    fun `the node budget still applies to reused statements`() {
        val (paramMap, rootName) = Fixtures.libraries.getValue("mini")
        val program = "a0 = Title(\"x\")\n" + lines(1..BUDGET_LEVELS) { "a$it = Stack([a${it - 1}, a${it - 1}])" } +
            "\ne = Stack([a$BUDGET_LEVELS])\nroot = Stack([e, e, e, e, e, e, e])"
        val parser = StreamParser(paramMap, rootName)
        var streamed = ParseResult.empty()
        for (end in BUDGET_CHUNK until program.length + BUDGET_CHUNK step BUDGET_CHUNK) {
            val prefix = program.substring(0, minOf(end, program.length))
            streamed = parser.set(prefix, complete = end >= program.length)
            assertSame(oneShot(paramMap, rootName, prefix, complete = end >= program.length), streamed, "@ $end")
        }
        assertEquals(Parser(paramMap, rootName).parse(program), streamed)
        // Each `e` fits the budget alone, but not all of them: the later ones read as null.
        val children = streamed.root?.props?.get("children").arrayValue.orEmpty()
        assertTrue(children.size in 1 until 7, "children: ${children.size}")
    }

    @Test
    fun `a deep chain is cut where it is too deep, also when reused`() {
        val program = reusedStatements[DEEP_CHAIN_PROGRAM].first
        val streamed = streamInChunks(program, chunkSize = 60)
        val whole = Parser(chat.first, chat.second).parse(program)
        assertEquals(whole.root, streamed.root)
        assertTrue("\"end\"" in whole.jsonValue.toString())
    }

    // Invalidation -----------------------------------------------------------------------------

    @Test
    fun `an edited statement is materialized again`() {
        val program = "root = Card([a, b])\na = TextContent(\"one\")\nb = TextContent(\"two\")\n"
        val parser = chatParser()
        streamInChunks(program, chunkSize = 3, parser)
        val merged = mergeStatements(program, "a = TextContent(\"edited\")")
        val result = parser.set(merged, complete = true)
        assertSame(oneShot(chat.first, chat.second, merged, complete = true), result, "merged")
        assertTrue("edited" in result.root!!.jsonValue.toString())
        assertTrue("two" in result.root!!.jsonValue.toString())
    }

    @Test
    fun `a statement declared again replaces what referenced it`() {
        val parser = chatParser()
        val first = parser.set("root = Card([a])\na = TextContent(\"one\")\n")
        assertTrue("one" in first.root!!.jsonValue.toString())
        val second = parser.set("root = Card([a])\na = TextContent(\"one\")\na = TextContent(\"two\")\n")
        assertTrue("two" in second.root!!.jsonValue.toString())
        assertTrue("one" !in second.root!!.jsonValue.toString())
    }

    @Test
    fun `a referenced statement that completes later shows up`() {
        val parser = chatParser()
        val program = "root = Card([CardHeader(\"Title\"), later])\nlater = TextContent(\"arrived\")\n"
        var sawUnresolved = false
        for (end in 1..program.length) {
            val prefix = program.substring(0, end)
            val result = parser.set(prefix)
            if ("later" in result.meta.unresolved) sawUnresolved = true
            assertSame(oneShot(chat.first, chat.second, prefix), result, "@ $end")
        }
        assertTrue(sawUnresolved)
        val done = parser.getResult(complete = true)
        assertTrue(done.meta.unresolved.isEmpty())
        assertTrue("arrived" in done.root!!.jsonValue.toString())
    }

    @Test
    fun `state used by a reused statement follows its declaration`() {
        val parser = chatParser()
        val base = "root = Card([header])\nheader = CardHeader(\$title)\n"
        val undeclared = parser.set(base)
        assertEquals(OpenUIValue.Null, undeclared.stateDeclarations["\$title"])
        val declared = parser.set("$base\$title = \"Hi\"\n")
        assertEquals(v("Hi"), declared.stateDeclarations["\$title"])
        val redeclared = parser.set("$base\$title = \"Hi\"\n\$title = \"Bye\"\n")
        assertEquals(v("Bye"), redeclared.stateDeclarations["\$title"])
        assertSame(oneShot(chat.first, chat.second, "$base\$title = \"Hi\"\n\$title = \"Bye\"\n"), redeclared, "state")
    }

    @Test
    fun `an Each loop variable named like a statement stays scoped`() {
        val program = """
            root = Card([list])
            item = TextContent("statement")
            list = Card(@Each(${'$'}rows, "item", TextContent(item)))
            ${'$'}rows = ["a", "b"]
        """.trimIndent()
        val streamed = streamInChunks(program, chunkSize = 2)
        val whole = Parser(chat.first, chat.second).parse(program)
        assertEquals(whole.root, streamed.root)
        assertEquals(whole.meta.orphaned, streamed.meta.orphaned)
        assertTrue("item" in streamed.meta.orphaned)
    }

    @Test
    fun `replacing the text starts over`() {
        val parser = chatParser()
        parser.set("root = Card([a])\na = TextContent(\"one\")\n")
        val replaced = parser.set("root = Card([b])\nb = TextContent(\"two\")\n")
        assertSame(oneShot(chat.first, chat.second, "root = Card([b])\nb = TextContent(\"two\")\n"), replaced, "replaced")
        assertNotEquals(0, parser.preprocessedChars)
    }

    // Cost -------------------------------------------------------------------------------------

    @Test
    fun `a long reply costs work linear in its chunks`() {
        val program = longProgram()
        val chunkSize = program.length / LONG_CHUNKS + 1
        val parser = chatParser()
        var chunks = 0
        var maxNodesPerChunk = 0
        var end = 0
        while (end < program.length) {
            val before = parser.materializedNodes
            end = minOf(program.length, end + chunkSize)
            parser.set(program.substring(0, end), complete = end == program.length)
            maxNodesPerChunk = maxOf(maxNodesPerChunk, parser.materializedNodes - before)
            chunks++
        }
        val whole = Parser(chat.first, chat.second).parse(program)
        assertEquals(LONG_STATEMENTS + 3, whole.meta.statementCount)
        assertEquals(whole, parser.getResult(complete = true))

        // Each chunk reads its own characters and the line it ends, not the whole reply again.
        assertTrue(parser.preprocessedChars < PREPROCESSED_FACTOR * program.length, "${parser.preprocessedChars}")
        // Each chunk materializes the root's list of references and the statement still arriving,
        // and reuses the statements before it.
        assertTrue(maxNodesPerChunk < NODES_PER_CHUNK, "max per chunk $maxNodesPerChunk")
        assertTrue(parser.materializedNodes < NODES_PER_CHUNK * chunks, "${parser.materializedNodes}")
    }

    private fun streamInChunks(program: String, chunkSize: Int, parser: StreamParser = chatParser()): ParseResult {
        var result = ParseResult.empty()
        var end = 0
        while (end < program.length) {
            end = minOf(program.length, end + chunkSize)
            result = parser.set(program.substring(0, end), complete = end == program.length)
        }
        return result
    }

    private companion object {
        const val SEED = 0x9e3779b9L.toInt()
        const val PREPROCESSOR_RUNS = 2000
        const val MAX_PIECES = 31
        const val MAX_CHECKPOINTS = 400
        const val MAX_CHUNK = 12
        const val BUDGET_LEVELS = 11
        const val BUDGET_CHUNK = 40
        const val DEEP_CHAIN_PROGRAM = 2
        const val LONG_STATEMENTS = 300
        const val LONG_CHUNKS = 3000
        const val PREPROCESSED_FACTOR = 20
        const val NODES_PER_CHUNK = 500

        val pieces = listOf(
            "`", "``", "```", "```js\n", "```\n", "\"", "'", "\\", "/", "//", "#", "\n", "\r\n", " ", "\t",
            " ", "　", "é", "a", "x = 1", "(", ")", "[", "]", "?", ":", "root = Stack([a])",
            "a = TextContent(\"hi\")", "\$x", "You're", "\"```\"",
        )

        fun lines(range: IntRange, line: (Int) -> String): String = range.joinToString("\n") { line(it) }

        val deepChain = listOf(
            "root = Card([x, p, d1])",
            "p = Card([x])",
            lines(1..60) { "d$it = Card([d${it + 1}])" },
            "d61 = Card([p])",
            lines(1..30) { "x${if (it == 1) "" else "$it"} = Card([x${it + 1}])" },
            "x31 = TextContent(\"end\")",
        ).joinToString("\n")

        val reusedStatements: List<Pair<String, Int>> = listOf(
            // Declared after use, and declared again.
            "root = Card([a, b])\na = TextContent(\"one\")\nb = TextContent(\$t)\n" +
                "a = TextContent(\"two\")\n\$t = \"x\"" to 3,
            // A cycle, cut where `b` is reached through `a` but not where `c` reaches it.
            "root = Card([a, c])\na = Card([b])\nb = Card([a, TextContent(\"x\")])\nc = Card([b])" to 3,
            // `p`, and the `x` it uses, fit where the root uses them, but not at the end of the deep chain.
            deepChain to 60,
            // Each `e` fits the node budget, but not all of them.
            "a0 = TextContent(\"x\")\n" + lines(1..11) { "a$it = [a${it - 1}, a${it - 1}]" } +
                "\ne = [a11]\nroot = Card([e, e, e, e, e, e, e])" to 40,
            "root = Card([CardHeader(\$title), list])\n\$title = \"Hi\"\n" +
                "list = Card([TextContent(\$title), Button(\"Go\", @Set(\$title, \"Bye\"))])" to 2,
        )

        /** A fenced reply with comments, state and one card per line. */
        fun longProgram(): String {
            val lines = arrayListOf(
                "```openui",
                "// A long list",
                "root = Card([title, " + (0 until LONG_STATEMENTS).joinToString(", ") { "s$it" } + "])",
                "title = CardHeader(\"Items\", \$filter)",
                "\$filter = \"all\"",
            )
            for (index in 0 until LONG_STATEMENTS) {
                lines += "s$index = Card([CardHeader(\"Item $index\", \"Subtitle $index\"), " +
                    "TextContent(\"Body text for item $index, long enough to look like a real one.\"), " +
                    "Buttons([Button(\"Open\", @ToAssistant(\"open $index\"))])]) // card $index"
            }
            lines += "```"
            return lines.joinToString("\n")
        }
    }
}
