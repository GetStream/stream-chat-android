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
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class OpenUIJsonTest {

    @Test
    fun `parses every value kind`() {
        assertEquals(OpenUIValue.Null, OpenUIJson.parse("null"))
        assertEquals(OpenUIValue.True, OpenUIJson.parse("true"))
        assertEquals(OpenUIValue.False, OpenUIJson.parse(" false "))
        assertEquals(v("hi"), OpenUIJson.parse("\"hi\""))
        assertEquals(v(listOf(1, "a", null, listOf<Any>())), OpenUIJson.parse("[1, \"a\", null, []]"))
        assertEquals(obj("a" to obj()), OpenUIJson.parse("\t{\r\n\"a\" : {} }\n"))
    }

    @Test
    fun `numbers are doubles like JavaScript`() {
        assertEquals(OpenUIValue.Num(0.0), OpenUIJson.parse("0"))
        assertEquals(-0.0, (OpenUIJson.parse("-0") as OpenUIValue.Num).value)
        assertEquals(OpenUIValue.Num(-12.5), OpenUIJson.parse("-1.25e1"))
        assertEquals(OpenUIValue.Num(1e-3), OpenUIJson.parse("1E-3"))
        assertEquals(OpenUIValue.Num(250.0), OpenUIJson.parse("2.5e+2"))
        assertEquals(OpenUIValue.Num(9007199254740992.0), OpenUIJson.parse("9007199254740993"))
        assertEquals(OpenUIValue.Num(0.30000000000000004), OpenUIJson.parse("0.30000000000000004"))
        assertEquals(OpenUIValue.Num(Double.POSITIVE_INFINITY), OpenUIJson.parse("1e400"))
    }

    @Test
    fun `objects keep key order and the last duplicate key wins in the first position`() {
        val value = OpenUIJson.parse("""{"b": 1, "a": 2, "b": 3}""")
        assertEquals(listOf("b", "a"), value.objectValue?.keys?.toList())
        assertEquals(v(3), value["b"])
    }

    @Test
    fun `string escapes decode like JSON parse`() {
        assertEquals(v("\" \\ / \b \u000C \n \r \t"), OpenUIJson.parse("\"\\\" \\\\ \\/ \\b \\f \\n \\r \\t\""))
        assertEquals(v("é"), OpenUIJson.parse("\"\\u00E9\""))
        assertEquals(v("😀"), OpenUIJson.parse("\"\\uD83D\\uDE00\""))
        assertEquals(v("😀"), OpenUIJson.parse("\"😀\""))
        assertEquals(v("\uD83D"), OpenUIJson.parse("\"\\ud83d\""))
        assertEquals(v("\u007F"), OpenUIJson.parse("\"\u007F\""))
    }

    @Test
    fun `rejects what JSON parse rejects`() {
        val invalid = listOf(
            "", " ", "\"raw\nnewline\"", "\"tab\there\"", "\"\u0000\"", "[1,]", "{\"a\": 1,}", "[,1]", "{,}",
            "01", "-01", "00", "1.", ".5", "1e", "1e+", "-", "+1", "NaN", "Infinity", "-Infinity",
            "'single'", "{'a': 1}", "{a: 1}", "\"\\x41\"", "\"\\u12G4\"", "\"\\u12\"", "\"unterminated",
            "[1 2]", "{\"a\" 1}", "{\"a\": 1 \"b\": 2}", "tru", "nul", "true false", "[1]]", "\uFEFF1",
            "\u000B1", "// comment\n1", "0x10",
        )
        for (text in invalid) {
            assertThrows<OpenUIJsonException>("expected `$text` to be rejected") { OpenUIJson.parse(text) }
            assertNull(OpenUIJson.parseOrNull(text))
        }
    }

    @Test
    fun `errors report the position`() {
        val error = assertThrows<OpenUIJsonException> { OpenUIJson.parse("{\"a\": [1, 2,]}") }
        assertTrue(error.message!!.endsWith("at position 12"), error.message)
        val leadingZero = assertThrows<OpenUIJsonException> { OpenUIJson.parse("[1, 007]") }
        assertTrue(leadingZero.message!!.endsWith("at position 4"), leadingZero.message)
    }

    @Test
    fun `deep nesting fails cleanly instead of overflowing the stack`() {
        val deep = "[".repeat(100_000) + "]".repeat(100_000)
        assertThrows<OpenUIJsonException> { OpenUIJson.parse(deep) }
        val ok = "[".repeat(500) + "]".repeat(500)
        assertEquals(OpenUIJson.stringify(OpenUIJson.parse(ok)), ok)
    }

    @Test
    fun `stringify round-trips parsed values`() {
        val text = """{"z":1,"a":{"y":[1,25,-0.5],"b":"é😀\n"},"m":null,"t":true}"""
        assertEquals(text, OpenUIJson.stringify(OpenUIJson.parse(text)))
    }
}
