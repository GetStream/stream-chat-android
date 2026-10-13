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

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class JsonTest {

    @Test
    fun `strings escape what JSON requires and nothing more`() {
        val text = "a\"b\\c\nd\re\tf\bg\u000Ch\u0001i/j"

        assertEquals("\"a\\\"b\\\\c\\nd\\re\\tf\\bg\\fh\\u0001i/j\"", Json.write(text))
    }

    @Test
    fun `values write as their JSON form`() {
        val value = mapOf(
            "z" to null,
            "b" to true,
            "n" to 1.5,
            "list" to listOf(1, "two", JSONObject.NULL),
            "a" to Unit,
        )

        assertEquals("{\"a\":\"kotlin.Unit\",\"b\":true,\"list\":[1,\"two\",null],\"n\":1.5,\"z\":null}", Json.write(value))
    }

    @Test
    fun `org-json values write like plain collections`() {
        val value = listOf(JSONObject("""{"b":1,"a":[true]}"""), JSONArray("[1,2]"))

        assertEquals("[{\"a\":[true],\"b\":1},[1,2]]", Json.write(value))
    }

    @Test
    fun `text that is not a JSON object parses as empty`() {
        assertEquals(emptyMap<String, Any?>(), Json.parseObject("[1, 2]"))
        assertEquals(emptyMap<String, Any?>(), Json.parseObject("not json"))
    }

    @Test
    fun `a JSON object parses into plain collections`() {
        val parsed = Json.parseObject("""{"a":{"b":[1,null]},"c":null}""")

        assertEquals(mapOf("a" to mapOf("b" to listOf(1, null)), "c" to null), parsed)
    }

    @Test
    fun `json reads only objects and arrays`() {
        val fields = Fields(mapOf("text" to "x", "number" to 1, "list" to listOf(2, 1)))

        assertNull(fields.json("text"))
        assertNull(fields.json("number"))
        assertNull(fields.json("missing"))
        assertEquals("[2,1]", fields.json("list"))
    }

    @Test
    fun `a nested object with non-string keys reads by their text`() {
        val fields = Fields(mapOf("approval" to mapOf(1 to "one")))

        assertEquals("one", fields.nested("approval")?.string("1"))
        assertNull(fields.nested("missing"))
    }
}
