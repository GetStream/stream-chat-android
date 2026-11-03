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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Port of `lang-core/src/parser/__tests__/materialize.validation.test.ts`. */
internal class SchemaValidationTest {
    private val schemaJson = """
    {
      "${'$'}defs": {
        "ObjBox": { "properties": { "info": { "type": "object", "properties": { "author": { "type": "string" }, "views": { "type": "number" } }, "required": ["author"] } }, "required": [] },
        "ArrBox": { "properties": { "rows": { "type": "array", "items": { "type": "object", "properties": { "label": { "type": "string" }, "value": { "type": "number" } }, "required": ["label"] } } }, "required": [] },
        "TagBox": { "properties": { "tags": { "type": "array", "items": { "type": "string" } } }, "required": [] },
        "EnumBox": { "properties": { "status": { "enum": ["active", "inactive"] } }, "required": [] },
        "AnyBox": { "properties": { "combo": { "anyOf": [{ "type": "string" }, { "type": "number" }] } }, "required": [] },
        "ScalarBox": { "properties": { "title": { "type": "string" }, "count": { "type": "number" }, "flag": { "type": "boolean" } }, "required": [] },
        "ListBox": { "properties": { "children": { "type": "array" } }, "required": [] },
        "CardBox": { "properties": { "text": { "type": "string" } }, "required": [] },
        "ReqScalar": { "properties": { "title": { "type": "string" } }, "required": ["title"] },
        "DefBox": { "properties": { "theme": { "type": "string", "default": "dark" } }, "required": ["theme"] },
        "SlotBox": { "properties": { "children": { "type": "array", "items": { "anyOf": [{ "${'$'}ref": "#/${'$'}defs/CardBox" }] } } }, "required": [] },
        "ChartBox": { "properties": { "labels": { "type": "array", "items": { "type": "string" } }, "variant": { "enum": ["grouped", "stacked"] } }, "required": ["labels"] },
        "NestDefBox": { "properties": { "cfg": { "type": "object", "properties": { "mode": { "enum": ["fast", "slow"], "default": "fast" }, "retries": { "type": "number", "default": 3 } }, "required": ["mode"] } }, "required": [] }
      }
    }
    """

    private val parser = Parser(Parser.compileSchema(OpenUIJson.parse(schemaJson)))

    private fun errors(source: String) = parser.parse(source).meta.errors
    private fun root(source: String) = parser.parse(source).root

    private fun directErrors(value: OpenUIValue, schema: OpenUIValue, partial: Boolean = false): List<ValidationError> {
        val context = MaterializeContext(emptyMap(), null, partial, null, mutableSetOf())
        SchemaValidation.validate(value, schema, "X", "/p", context)
        return context.errors
    }

    // recursive descent & error reporting

    @Test
    fun `reports violations with json pointer paths`() {
        assertTrue(errors("""root = ObjBox({ author: "ann", views: 3 })""").isEmpty())

        val leaf = errors("""root = ObjBox({ author: "ann", views: "lots" })""").first()
        assertEquals(ValidationErrorCode.TypeMismatch, leaf.code)
        assertEquals("ObjBox", leaf.component)
        assertEquals("/info/views", leaf.path)

        assertEquals("/rows/0/value", errors("""root = ArrBox([{ label: "a", value: "x" }])""").first().path)

        val missing = errors("root = ObjBox({ views: 3 })").first()
        assertEquals(ValidationErrorCode.MissingRequired, missing.code)
        assertEquals("/info/author", missing.path)

        val nulled = errors("root = ObjBox({ author: null, views: 3 })").first()
        assertEquals(ValidationErrorCode.NullRequired, nulled.code)
        assertEquals("/info/author", nulled.path)
    }

    @Test
    fun `reports container shape mismatches and accumulates`() {
        assertTrue("expects object but got string" in errors("""root = ObjBox("hello")""").first().message)
        assertTrue("expects array but got number" in errors("root = TagBox(42)").first().message)
        assertTrue("expects string but got object" in errors("root = ScalarBox({ a: \$x })").first().message)

        val byPath = errors("""root = ArrBox([{ label: 1 }, { value: "x" }])""")
            .map {
                "${it.code.wireName}:${it.path}"
            }.sorted()
        assertEquals(
            listOf("missing-required:/rows/1/label", "type-mismatch:/rows/0/label", "type-mismatch:/rows/1/value"),
            byPath,
        )

        assertEquals(
            listOf("excess-args", "type-mismatch"),
            errors("""root = ScalarBox(1, 1, true, "extra")""")
                .map {
                    it.code.wireName
                }.sorted(),
        )
    }

    @Test
    fun `messages are self sufficient and attributed`() {
        assertEquals(
            """missing required field "/title" — signature: ReqScalar(title*: string)""",
            errors("root = ReqScalar()").first().message,
        )
        assertEquals(
            """required field "/labels" cannot be null — signature: ChartBox(labels*: array, variant: "grouped"|"stacked")""",
            errors("""root = ChartBox(null, "grouped")""").first().message,
        )
        assertTrue("Available components: ObjBox, ArrBox" in errors("root = Nope()").first().message)

        val attributed = errors("root = ListBox([inner])\ninner = ObjBox({ author: \"ann\", views: \"lots\" })").first()
        assertEquals("ObjBox", attributed.component)
        assertEquals("inner", attributed.statementId)
    }

    // the edge rule

    @Test
    fun `prunes invalid values on optional edges and array items`() {
        assertEquals(
            obj("author" to "ann"),
            root("""root = ObjBox({ author: "ann", views: "lots" })""")?.props?.get("info"),
        )

        val top = root("root = ScalarBox(5)")
        assertNotNull(top)
        assertFalse("title" in top.props)

        assertFalse("info" in root("root = ObjBox({ views: 3 })")!!.props)
        assertEquals(v(listOf("a", "c")), root("""root = TagBox(["a", 2, "c"])""")?.props?.get("tags"))
        assertEquals(
            v(listOf(mapOf("label" to "a", "value" to 1))),
            root("""root = ArrBox([{ label: "a", value: 1 }, { value: 2 }])""")?.props?.get("rows"),
        )
    }

    @Test
    fun `propagates through required edges`() {
        val nested = root("""root = ObjBox({ author: { first: "a" }, views: 3 })""")
        assertNotNull(nested)
        assertFalse("info" in nested.props)
        assertNull(root("root = ReqScalar(5)"))
    }

    @Test
    fun `substitutes schema defaults`() {
        val def = parser.parse("root = DefBox(5)")
        assertEquals(v("dark"), def.root?.props?.get("theme"))
        assertEquals(1, def.meta.errors.size)
        assertTrue(parser.parse("root = DefBox()").meta.errors.isEmpty())

        val optional = parser.parse("""root = NestDefBox({ mode: "warp", retries: 1 })""")
        assertEquals(obj("mode" to "fast", "retries" to 1), optional.root?.props?.get("cfg"))
        assertEquals(ValidationErrorCode.TypeMismatch, optional.meta.errors.first().code)
        assertEquals("/cfg/mode", optional.meta.errors.first().path)

        assertEquals(
            obj("mode" to "slow", "retries" to 3),
            root("""root = NestDefBox({ mode: "slow", retries: "many" })""")?.props?.get("cfg"),
        )

        val absent = parser.parse("root = NestDefBox({ retries: 2 })")
        assertEquals(obj("mode" to "fast", "retries" to 2), absent.root?.props?.get("cfg"))
        assertTrue(absent.meta.errors.isEmpty())

        val nulled = parser.parse("root = NestDefBox({ mode: null, retries: 2 })")
        assertEquals(obj("mode" to "fast", "retries" to 2), nulled.root?.props?.get("cfg"))
        assertTrue(nulled.meta.errors.isEmpty())
    }

    // components in data slots

    @Test
    fun `elements in data slots follow the edge rule`() {
        val dropped = parser.parse("""root = ChartBox(CardBox("x"), "grouped")""")
        assertNull(dropped.root)
        assertTrue(
            """expects array but got component "CardBox"""" in dropped.meta.errors
                .first()
                .message,
        )

        val pruned = parser.parse("""root = ChartBox(["a"], CardBox("x"))""")
        assertFalse("variant" in pruned.root!!.props)
        assertTrue("expects a literal value but got component" in pruned.meta.errors.first().message)

        assertEquals(v(listOf("a", "b")), root("""root = TagBox(["a", CardBox("x"), "b"])""")?.props?.get("tags"))
    }

    @Test
    fun `component slots accept components`() {
        val slot = parser.parse("""root = SlotBox([CardBox("hi"), EnumBox("active")])""")
        assertTrue(slot.meta.errors.isEmpty())
        assertEquals(2, slot.root?.props?.get("children").arrayValue?.size)

        val errs = errors("root = ObjBox({ author: CardBox(9) })")
        assertEquals(2, errs.size)
        assertEquals("/text", errs.first { it.component == "CardBox" }.path)
        assertEquals("/info/author", errs.first { it.component == "ObjBox" }.path)
    }

    @Test
    fun `data in component slot is pruned`() {
        val inline = parser.parse("""root = SlotBox([CardBox("hi"), { text: "a" }, "b", 3, [CardBox("x")]])""")
        assertEquals(1, inline.root?.props?.get("children").arrayValue?.size)
        assertEquals(
            listOf(
                """field "/children/1" expects CardBox but got plain object""",
                """field "/children/2" expects CardBox but got string""",
                """field "/children/3" expects CardBox but got number""",
                """field "/children/4" expects CardBox but got array""",
            ),
            inline.meta.errors.map { it.message },
        )

        val viaRef = parser.parse("root = SlotBox(items)\nitems = [{ text: \"a\" }]")
        assertEquals(v(emptyList<Any>()), viaRef.root?.props?.get("children"))
        assertEquals(ValidationErrorCode.TypeMismatch, viaRef.meta.errors.first().code)
        assertEquals("/children/0", viaRef.meta.errors.first().path)
    }

    @Test
    fun `ref to non component def is not a component slot`() {
        val treeParser = Parser(
            Parser.compileSchema(
                OpenUIJson.parse(
                    """
                    {
                      "properties": { "Tree": {} },
                      "${'$'}defs": {
                        "Tree": { "properties": { "node": { "${'$'}ref": "#/${'$'}defs/__schema0" } }, "required": ["node"] },
                        "__schema0": { "type": "object", "properties": { "label": { "type": "string" } } }
                      }
                    }
                    """,
                ),
            ),
        )
        val result = treeParser.parse("""root = Tree({ label: "a" })""")
        assertTrue(result.meta.errors.isEmpty())
        assertEquals(obj("label" to "a"), result.root?.props?.get("node"))
    }

    // conservative skips

    @Test
    fun `skips dynamic values composites and extras`() {
        assertTrue(errors("""root = TagBox(["a", ${'$'}x])""").isEmpty())
        assertTrue(errors("""root = ObjBox({ author: "a", views: Sum([1, 2]) })""").isEmpty())
        assertTrue(errors("root = AnyBox({ anything: true })").isEmpty())
        assertTrue(errors("root = ObjBox(null)").isEmpty())
        assertTrue(errors("root = ObjBox()").isEmpty())
        assertTrue(errors("""root = ObjBox({ author: "a", bonus: 99 })""").isEmpty())
        assertTrue(errors("""root = ListBox([1, "a", true])""").isEmpty())
    }

    @Test
    fun `skips uncheckable schemas and applies leaf edges`() {
        assertTrue(directErrors(v(5), OpenUIValue.Null).isEmpty())
        assertTrue(directErrors(v(5), v("string")).isEmpty())
        assertTrue(directErrors(v(5), obj("\$ref" to "#/\$defs/Thing")).isEmpty())
        assertTrue(directErrors(v(5), obj("oneOf" to listOf(mapOf("type" to "string")))).isEmpty())
        assertTrue(
            directErrors(v(listOf(5)), obj("type" to "array", "items" to listOf(mapOf("type" to "string")))).isEmpty(),
        )
        assertTrue(directErrors(v("x"), obj("type" to "date")).isEmpty())
        assertTrue(directErrors(obj(), obj("type" to "object", "required" to "author")).isEmpty())
        assertEquals(
            ValidationErrorCode.MissingRequired,
            directErrors(obj(), obj("type" to "object", "required" to listOf("a"))).first().code,
        )
        assertTrue(directErrors(v(1.5), obj("type" to "integer")).isEmpty())
        assertEquals(1, directErrors(v("3"), obj("type" to "integer")).size)
        assertTrue("""one of ["fixed"]""" in directErrors(v("loose"), obj("const" to "fixed")).first().message)
        assertTrue(directErrors(obj("a" to 1), obj("enum" to listOf("active"))).isEmpty())
    }

    // streaming gates

    @Test
    fun `defers required and enum checks while streaming`() {
        val required = StreamParser(parser.paramMap)
        assertTrue(required.push("root = ObjBox({ views: 3 ").meta.errors.isEmpty())
        val completed = required
            .push("})\n")
            .meta.errors
            .first()
        assertEquals(ValidationErrorCode.MissingRequired, completed.code)
        assertEquals("/info/author", completed.path)
        assertEquals("root", completed.statementId)

        val enumParser = StreamParser(parser.paramMap)
        assertTrue(
            enumParser
                .push("""root = EnumBox("bogus" """)
                .meta.errors
                .isEmpty(),
        )
        assertEquals("/status", enumParser.push(")\n").meta.errors.first().path)
    }

    @Test
    fun `keeps scalar checks on mid stream`() {
        val result = StreamParser(parser.paramMap).push("""root = ObjBox({ author: "a", views: "lots" """)
        assertTrue(result.meta.incomplete)
        assertEquals(
            "/info/views",
            result.meta.errors
                .first { it.code == ValidationErrorCode.TypeMismatch }
                .path,
        )
        assertTrue(directErrors(obj(), obj("type" to "object", "required" to listOf("a")), partial = true).isEmpty())
        assertTrue(directErrors(v("act"), obj("enum" to listOf("active")), partial = true).isEmpty())
        assertEquals(1, directErrors(v(5), obj("type" to "string"), partial = true).size)
    }
}
