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

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class UtilityTest {

    @Nested
    inner class JavaScriptSemantics {
        @Test
        fun `number to string`() {
            val cases = listOf(
                0.0 to "0", -0.0 to "0", 1.0 to "1", -1.5 to "-1.5", 0.1 + 0.2 to "0.30000000000000004",
                1e21 to "1e+21", 1e20 to "100000000000000000000", 123456789012345680000.0 to "123456789012345680000",
                1e-7 to "1e-7", 0.000001 to "0.000001", 1.5e-10 to "1.5e-10", 2e5 to "200000",
                1 / 3.0 to "0.3333333333333333",
                Double.NaN to "NaN", Double.POSITIVE_INFINITY to "Infinity", Double.NEGATIVE_INFINITY to "-Infinity",
                5e-324 to "5e-324", 1.7976931348623157e308 to "1.7976931348623157e+308", 2.0 / 3 to "0.6666666666666666",
            )
            for ((value, expected) in cases) assertEquals(expected, JS.numberToString(value), "$value")
        }

        @Test
        fun `string to number`() {
            assertEquals(0.0, JS.stringToNumber(""))
            assertEquals(12.0, JS.stringToNumber("  12  "))
            assertEquals(16.0, JS.stringToNumber("0x10"))
            assertEquals(5.0, JS.stringToNumber("0b101"))
            assertEquals(1000.0, JS.stringToNumber("1e3"))
            assertEquals(0.5, JS.stringToNumber(".5"))
            assertEquals(5.0, JS.stringToNumber("5."))
            assertEquals(Double.NEGATIVE_INFINITY, JS.stringToNumber("-Infinity"))
            assertTrue(JS.stringToNumber("12px").isNaN())
            assertTrue(JS.stringToNumber("-0x10").isNaN())
            assertTrue(JS.stringToNumber("inf").isNaN())
            assertTrue(JS.stringToNumber("1d").isNaN())
            assertEquals(12.0, JS.parseFloat("12px"))
            assertEquals(350.0, JS.parseFloat("  3.5e2xyz"))
            assertTrue(JS.parseFloat("abc").isNaN())
            assertEquals(1.0, JS.parseFloat("1e"))
        }

        @Test
        fun `loose equality`() {
            assertTrue(JS.looseEquals(v(5), v("5")))
            assertTrue(JS.looseEquals(v(0), v("")))
            assertFalse(JS.looseEquals(OpenUIValue.Null, v(0)))
            assertTrue(JS.looseEquals(OpenUIValue.Null, null))
            assertTrue(JS.looseEquals(v(true), v("1")))
            assertFalse(JS.looseEquals(v(true), v("true")))
            assertTrue(JS.looseEquals(v(listOf(1)), v(1)))
            assertTrue(JS.looseEquals(v(""), v(emptyList<Any>())))
            assertFalse(JS.looseEquals(v(Double.NaN), v(Double.NaN)))
        }

        @Test
        fun `conversions`() {
            assertEquals("1,,a", JS.toString(v(listOf(1, null, "a"))))
            assertEquals("[object Object]", JS.toString(obj("a" to 1)))
            assertTrue(JS.isTruthy(v("0")))
            assertFalse(JS.isTruthy(v(0)))
            assertFalse(JS.isTruthy(v("")))
            assertTrue(JS.isTruthy(v(emptyList<Any>())))
            assertEquals(3.0, JS.round(2.5))
            assertEquals(-2.0, JS.round(-2.5))
            assertEquals(0.0, JS.round(0.49999999999999994))
            assertEquals(
                """{"a":"x\"y\n","b":[1.5,null,true]}""",
                JS.jsonStringify(obj("a" to "x\"y\n", "b" to listOf(1.5, null, true))),
            )
            assertEquals(v(5), JS.property(v("héllo"), "length"))
            assertEquals(v(2), JS.property(v(listOf(1, 2)), "1"))
            assertNull(JS.property(v(listOf(1, 2)), "01"))
        }

        @Test
        fun `json parsing preserves key order`() {
            val value = OpenUIJson.parse("""{"z": 1, "a": {"y": [1, 2.5e1, -0.5], "b": "é😀"}, "m": null}""")
            assertEquals(listOf("z", "a", "m"), value.objectValue?.keys?.toList())
            assertEquals(v(listOf(1, 25, -0.5)), value["a"]["y"])
            assertEquals(v("é😀"), value["a"]["b"])
            assertThrows<OpenUIJsonException> { OpenUIJson.parse("""{"a": 1,}""") }
            assertThrows<OpenUIJsonException> { OpenUIJson.parse("\"raw\nnewline\"") }
        }

        @Test
        fun `json string literals decode like json parse`() {
            assertEquals("a\nb\u00e9", JS.jsonParseString("\"a\\nb\\u00e9\""))
            assertNull(JS.jsonParseString("\"raw\nnewline\""))
            assertNull(JS.jsonParseString("\"bad \\x escape\""))
            assertNull(JS.jsonParseString("\"partial \\u00\""))
        }
    }

    @Nested
    inner class Objects {
        @Test
        fun `objects can't be changed through their entries`() {
            val fields = OpenUIObject.of("a" to OpenUIValue.of(1))

            @Suppress("UNCHECKED_CAST")
            val entry = fields.entries.first() as MutableMap.MutableEntry<String, OpenUIValue>

            assertThrows<UnsupportedOperationException> { entry.setValue(OpenUIValue.of(2)) }
            assertEquals(OpenUIValue.of(1), fields["a"])
        }
    }

    @Nested
    inner class IncrementalEditing {
        private val original = """
            root = Stack([header, tbl])
            header = CardHeader("Tickets")
            tickets = Query("list_tickets", {}, {rows: []})
            tbl = Table([Col("Title", tickets.rows.title), Col("Status", tickets.rows.status)])
        """.trimIndent()

        @Test
        fun `merges patch by statement name`() {
            val patch = """
                root = Stack([header, chart, tbl])
                chart = PieChart(["Open", "Closed"], [@Count(@Filter(tickets.rows, "status", "==", "open")), @Count(@Filter(tickets.rows, "status", "==", "closed"))], "donut")
            """.trimIndent()
            val lines = mergeStatements(original, patch).split("\n")
            assertEquals(5, lines.size)
            assertEquals("root = Stack([header, chart, tbl])", lines[0])
            assertEquals("header = CardHeader(\"Tickets\")", lines[1])
            assertTrue(lines[4].startsWith("chart = PieChart("))
        }

        @Test
        fun `deletes and collects garbage`() {
            val merged = mergeStatements(
                original + "\n\$filter = \"open\"",
                "```openui-lang\nroot = Stack([header])\n```",
            )
            assertEquals("root = Stack([header])\nheader = CardHeader(\"Tickets\")\n\$filter = \"open\"", merged)
            assertEquals(
                "root = Stack([a, b])\na = 1",
                mergeStatements("root = Stack([a, b])\na = 1\nb = 2", "b = null"),
            )
        }

        @Test
        fun `empty inputs`() {
            assertEquals("root = Stack([])", mergeStatements("", "root = Stack([])"))
            assertEquals(original, mergeStatements(original, "   "))
        }
    }
}
