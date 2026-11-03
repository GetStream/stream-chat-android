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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Port of `lang-core/src/parser/__tests__/parser.test.ts`. */
internal class ParserTest {
    /** Stack(children), Title(text), Table(columns, rows). */
    private val schema = ParamMap(
        "Stack" to listOf(ParamDef("children", required = true)),
        "Title" to listOf(ParamDef("text", required = true)),
        "Table" to listOf(ParamDef("columns", required = true), ParamDef("rows", required = true)),
    )

    private fun parse(input: String) = Parser(schema).parse(input)
    private fun stream() = StreamParser(schema)
    private fun codes(input: String) = parse(input).meta.errors.map { it.code.wireName }
    private fun children(result: ParseResult) = result.root
        ?.props
        ?.get("children")
        .arrayValue
        .orEmpty()

    // unknown-component

    @Test
    fun `reports unknown component`() {
        val result = parse("""root = DataTable("col")""")
        assertEquals(1, result.meta.errors.size)
        assertEquals(ValidationErrorCode.UnknownComponent, result.meta.errors[0].code)
        assertEquals("DataTable", result.meta.errors[0].component)
        assertNull(result.root)
    }

    @Test
    fun `reports unknown components in tree`() {
        assertTrue("unknown-component" in codes("root = Stack([Ghost(\"a\")])\n"))
    }

    @Test
    fun `does not report known components`() {
        assertTrue(parse("""root = Stack(["hello"])""").meta.errors.isEmpty())
    }

    // excess-args

    @Test
    fun `reports excess args but still renders`() {
        val result = parse("""root = Title("hello", "extra")""")
        assertTrue("excess-args" in codes("""root = Title("hello", "extra")"""))
        assertEquals(v("hello"), result.root?.props?.get("text"))
    }

    @Test
    fun `excess args message lists count`() {
        val error = parse("""root = Title("hello", "extra", "more")""").meta.errors.first {
            it.code ==
                ValidationErrorCode.ExcessArgs
        }
        assertTrue("2 excess dropped" in error.message)
    }

    @Test
    fun `no excess args when counts match`() {
        assertFalse("excess-args" in codes("""root = Title("hello")"""))
        assertFalse("excess-args" in codes("root = Table([], [])"))
    }

    // unresolved references

    @Test
    fun `tracks unresolved references`() {
        val result = parse("root = Stack([tbl])")
        assertTrue("tbl" in result.meta.unresolved)
        assertTrue(result.meta.errors.isEmpty())
    }

    @Test
    fun `clears unresolved when defined`() {
        assertTrue(parse("root = Stack([tbl])\ntbl = Title(\"hello\")").meta.unresolved.isEmpty())
    }

    @Test
    fun `streaming tracks and resolves forward references`() {
        val parser = stream()
        val mid = parser.push("root = Stack([tbl])\n")
        assertTrue("tbl" in mid.meta.unresolved)
        assertTrue(mid.meta.errors.isEmpty())
        assertTrue(parser.push("tbl = Title(\"hello\")\n").meta.unresolved.isEmpty())
    }

    @Test
    fun `streaming keeps unresolved at end of stream`() {
        val parser = stream()
        parser.push("root = Stack([tbl])\n")
        val result = parser.getResult()
        assertTrue("tbl" in result.meta.unresolved)
        assertTrue(result.meta.errors.isEmpty())
    }

    // required props

    @Test
    fun `missing required shape`() {
        val result = parse("root = Stack()")
        assertEquals(1, result.meta.errors.size)
        assertEquals(ValidationErrorCode.MissingRequired, result.meta.errors[0].code)
        assertEquals("Stack", result.meta.errors[0].component)
    }

    @Test
    fun `null required shape`() {
        val result = parse("root = Stack(null)")
        assertEquals(1, result.meta.errors.size)
        assertEquals(ValidationErrorCode.NullRequired, result.meta.errors[0].code)
    }

    // array null-dropping

    @Test
    fun `drops unresolved refs from arrays`() {
        val items = children(parse("root = Stack([missing, t1])\nt1 = Title(\"ok\")"))
        assertEquals(1, items.size)
        assertEquals("Title", items[0].elementValue?.typeName)
    }

    @Test
    fun `drops invalid components from arrays`() {
        val items = children(parse("root = Stack([bad, good])\nbad = Title()\ngood = Title(\"ok\")"))
        assertEquals(1, items.size)
        assertEquals(v("ok"), items[0].elementValue?.props?.get("text"))
    }

    @Test
    fun `drops unknown components from arrays`() {
        val items = children(parse("root = Stack([u, t1])\nu = Ghost(\"x\")\nt1 = Title(\"ok\")"))
        assertEquals(1, items.size)
        assertEquals("Title", items[0].elementValue?.typeName)
    }

    @Test
    fun `preserves null literals in arrays`() {
        assertEquals(listOf(OpenUIValue.Null, OpenUIValue.Null), children(parse("root = Stack([null, null])")))
    }

    @Test
    fun `streaming fills in later definitions`() {
        val parser = stream()
        assertEquals(1, children(parser.push("root = Stack([t1, t2])\nt1 = Title(\"first\")\n")).size)
        assertEquals(2, children(parser.push("t2 = Title(\"second\")\n")).size)
    }

    // orphaned statements

    @Test
    fun `detects orphaned statements`() {
        assertEquals(
            listOf("orphan"),
            parse("root = Stack([t1])\nt1 = Title(\"used\")\norphan = Title(\"unused\")").meta.orphaned,
        )
    }

    @Test
    fun `no orphans when all reachable`() {
        assertTrue(parse("root = Stack([t1, t2])\nt1 = Title(\"a\")\nt2 = Title(\"b\")").meta.orphaned.isEmpty())
    }

    @Test
    fun `orphaned during streaming`() {
        val result = stream().push("root = Stack([t1])\nt1 = Title(\"a\")\norphan = Title(\"stray\")\n")
        assertEquals(listOf("orphan"), result.meta.orphaned)
    }

    @Test
    fun `detects multiple orphans`() {
        assertEquals(
            listOf("a", "b"),
            parse("root = Stack([t1])\nt1 = Title(\"used\")\na = Title(\"x\")\nb = Title(\"y\")").meta.orphaned,
        )
    }

    @Test
    fun `deeply nested refs are not orphaned`() {
        assertTrue(
            parse(
                "root = Stack([t1])\nt1 = Table([col], rows)\ncol = Title(\"Name\")\nrows = [[\"Alice\"]]",
            ).meta.orphaned.isEmpty(),
        )
    }

    // fences and comments inside strings

    @Test
    fun `preserves fences inside strings`() {
        val result = parse("""root = Title("```js\nconsole.log(\"Hello World\");\n```")""")
        assertTrue(result.meta.errors.isEmpty())
        assertEquals(v("```js\nconsole.log(\"Hello World\");\n```"), result.root?.props?.get("text"))
    }

    @Test
    fun `preserves python fence inside strings`() {
        val result = parse("""root = Title("```python\nprint('hi')\n```")""")
        assertEquals(v("```python\nprint('hi')\n```"), result.root?.props?.get("text"))
    }

    @Test
    fun `preserves slashes inside multiline strings`() {
        val result = parse("root = Title(\"\nHello // World\nhttps://example.com/\n\")")
        assertTrue(result.meta.errors.isEmpty())
        assertEquals(v("\nHello // World\nhttps://example.com/\n"), result.root?.props?.get("text"))
    }

    @Test
    fun `preserves hash inside multiline strings`() {
        val result = parse("root = Title(\"\nHello # World\nhttps://example.com/\n\")")
        assertEquals(v("\nHello # World\nhttps://example.com/\n"), result.root?.props?.get("text"))
    }

    @Test
    fun `apostrophes outside fences do not open strings`() {
        val result = parse("Here's the code:\n\n```js\nroot = Title(\"hello\")\n```")
        assertTrue(result.meta.errors.isEmpty())
        assertEquals(v("hello"), result.root?.props?.get("text"))
    }

    // redefined statement ids

    private val withNewline = "root = Stack([a])\na = Title(\"x\")\na = Title(\"y\")\n"
    private val withoutNewline = "root = Stack([a])\na = Title(\"x\")\na = Title(\"y\")"

    private fun titleText(result: ParseResult) = children(result)
        .firstOrNull()
        ?.elementValue
        ?.props
        ?.get("text")
        .stringValue

    @Test
    fun `parse is last wins`() {
        assertEquals("y", titleText(parse(withNewline)))
        assertEquals("y", titleText(parse(withoutNewline)))
    }

    @Test
    fun `stream last wins for single push`() {
        for (text in listOf(withNewline, withoutNewline)) {
            val parser = stream()
            parser.push(text)
            assertEquals("y", titleText(parser.getResult()))
        }
    }

    @Test
    fun `stream last wins line by line and char by char`() {
        val lineParser = stream()
        withNewline.split(Regex("(?<=\n)")).filter { it.isNotEmpty() }.forEach { lineParser.push(it) }
        assertEquals("y", titleText(lineParser.getResult()))

        for (text in listOf(withNewline, withoutNewline)) {
            val charParser = stream()
            text.forEach { charParser.push(it.toString()) }
            assertEquals("y", titleText(charParser.getResult()))
        }
    }

    @Test
    fun `stream last wins across two chunks`() {
        val parser = stream()
        parser.push("root = Stack([a])\na = Title(\"x\")\n")
        parser.push("a = Title(\"y\")\n")
        assertEquals("y", titleText(parser.getResult()))

        val parser2 = stream()
        parser2.push("root = Stack([a])\na = Title(\"x\")\n")
        parser2.push("a = Title(\"y\")")
        assertEquals("y", titleText(parser2.getResult()))
    }

    @Test
    fun `stream last wins via set`() {
        assertEquals("y", titleText(stream().set(withoutNewline)))
    }

    @Test
    fun `incomplete redefinition does not clobber completed statement`() {
        val parser = stream()
        parser.push("root = Stack([a])\na = Title(\"x\")\n")
        assertEquals("x", titleText(parser.push("a = Title(\"y")))
        assertEquals("y", titleText(parser.push("\")\n")))
    }

    @Test
    fun `set restarts when the text is replaced`() {
        val parser = stream()
        parser.set("root = Stack([a])\na = Title(\"x\")\n")
        assertEquals("z", titleText(parser.set("root = Stack([b])\nb = Title(\"z\")\n")))
    }

    // OpenUI 1.0: the entry statement

    @Test
    fun `without a root statement the first root component call renders with a warning`() {
        val result = Parser(schema, rootName = "Stack").parse("a = Title(\"x\")\nmain = Stack([a])")
        assertEquals("main", result.root?.statementId)
        val error = result.meta.errors.single()
        assertEquals(ValidationErrorCode.NoRoot, error.code)
        assertTrue(error.isWarning)
        assertEquals(v("warning"), error.jsonValue["severity"])
    }

    @Test
    fun `without a root statement or root component call nothing renders`() {
        val result = Parser(schema, rootName = "Stack").parse("a = Title(\"x\")")
        assertNull(result.root)
        assertEquals(listOf(ValidationErrorCode.NoRoot), result.meta.errors.map { it.code })
        assertFalse(result.meta.errors[0].isWarning)
        assertTrue(result.meta.orphaned.isEmpty())
    }

    @Test
    fun `a root that is not a component is an error`() {
        val result = parse("root = \"hello\"")
        assertNull(result.root)
        assertEquals(listOf("no-root"), result.meta.errors.map { it.code.wireName })
    }

    @Test
    fun `a ternary root picks the component at runtime`() {
        val result = parse("root = \$dark ? a : b\na = Title(\"A\")\nb = Title(\"B\")")
        assertNull(result.root)
        assertTrue(result.rootExpression is ASTNode.Ternary)
        assertTrue(result.meta.errors.isEmpty())
    }

    @Test
    fun `an unknown function evaluates to null with an error`() {
        val result = parse("root = Stack([@Shout(\"x\"), Title(\"a\")])")
        assertEquals(listOf("unknown-function"), result.meta.errors.map { it.code.wireName })
        assertEquals("Shout", result.meta.errors[0].component)
        assertEquals(1, children(result).size)
    }

    @Test
    fun `library functions and actions join the built-in calls`() {
        val paramMap = Parser.compileSchema(
            OpenUIJson.parse(
                """{"${'$'}defs": {"Title": {"properties": {"text": {}}}},
                   "functions": {"Upper": {"params": {"properties": {"text": {"type": "string"}}, "required": ["text"]}}},
                   "actions": {"Notify": {"params": {"properties": {"message": {"type": "string"}}}}}}""",
            ),
        )
        assertEquals(CallDef.Kind.Function, paramMap.callDefs["Upper"]?.kind)
        assertEquals(CallDef.Kind.Action, paramMap.callDefs["Notify"]?.kind)
        assertEquals(Builtins.calls.keys, paramMap.callDefs.keys - setOf("Upper", "Notify"))

        val result = Parser(paramMap).parse("root = Title(@Upper(\"x\"))")
        val call = (result.root?.props?.get("text") as OpenUIValue.Expression).node as ASTNode.Comp
        assertTrue(call.isFunction)
        assertEquals(mapOf("text" to ASTNode.Str("x")), call.mappedProps)

        val invalid = Parser(paramMap).parse("root = Title(@Upper(1))")
        assertEquals(listOf("type-mismatch"), invalid.meta.errors.map { it.code.wireName })
    }

    @Test
    fun `a state name still streaming in is not declared until the stream completes`() {
        val parser = stream()
        assertFalse("\$dis" in parser.push("root = Stack([Title(\$dis").stateDeclarations)
        assertTrue("\$dis" in parser.getResult(complete = true).stateDeclarations)
        assertEquals("\$d", StreamParser.partialStateName("x = \$d", "x = \$d"))
        assertNull(StreamParser.partialStateName("x = \$d + \$d", "x = \$d + \$d"))
        assertNull(StreamParser.partialStateName("x = \$d ", "x = \$d"))
    }
}
