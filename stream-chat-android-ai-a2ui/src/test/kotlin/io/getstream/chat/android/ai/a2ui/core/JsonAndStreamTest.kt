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

package io.getstream.chat.android.ai.a2ui.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

internal class JsonAndStreamTest {

    @Test
    fun `parses JSON into plain values`() {
        val value = A2UIJson.parse("""{"a": [1, 2.5, -3e2, true, null, "x\"\u00e9\n"], "b": {}}""")

        assertEquals(
            mapOf("a" to listOf(1L, 2.5, -300.0, true, null, "x\"é\n"), "b" to emptyMap<String, Any?>()),
            value,
        )
    }

    @Test
    fun `rejects text that isn't one JSON value`() {
        for (text in listOf("", "{", "{\"a\": 1,}", "[1 2]", "{} {}", "nul", "\"\\x\"", "{\"a\": 01x}")) {
            assertNull(text, A2UIJson.parseOrNull(text))
        }
        assertNull(A2UIJson.parseOrNull("[".repeat(1000) + "]".repeat(1000)))
    }

    @Test
    fun `writes JSON that parses back`() {
        val value = mapOf("b" to listOf(1L, 2.5, "q\"\\\u0001"), "a" to mapOf("n" to null, "t" to true))

        assertEquals(value, A2UIJson.parse(A2UIJson.write(value)))
        assertEquals("""{"a":{"n":null,"t":true},"b":[1,2.5,"q\"\\\u0001"]}""", A2UIJson.write(value, sortKeys = true))
    }

    @Test
    fun `values interpolate the way agents expect`() {
        assertEquals("4", A2UIJson.stringify(4.0))
        assertEquals("2.5", A2UIJson.stringify(2.5))
        assertEquals("", A2UIJson.stringify(null))
        assertEquals("""{"a":1}""", A2UIJson.stringify(mapOf("a" to 1L)))
        assertEquals(true, A2UIJson.isEqual(1L, 1.0))
        assertEquals(42.0, A2UIJson.double(" 42 "))
        assertNull(A2UIJson.double(Double.NaN))
    }

    @Test
    fun `the stream counts complete messages only`() {
        val text = """
            Here you go:
            {"version": "1.0", "createSurface": {"surfaceId": "s", "note": "a } in a string"}}
            {"version": "1.0", "updateComponents": {"surfaceId": "s", "components": [{"id": "root"
        """.trimIndent()

        val messages = A2UIStream.messages(text)

        assertEquals(1, messages.size)
        assertEquals(
            "s",
            A2UIJson.map(A2UIJson.map(A2UIJson.parse(messages.single()))!!["createSurface"])!!["surfaceId"],
        )
    }

    @Test
    fun `the stream reads messages written in an array`() {
        val messages = A2UIStream.messages("""[{"a": 1}, {"b": {"c": [2]}}]""")

        assertEquals(listOf("""{"a": 1}""", """{"b": {"c": [2]}}"""), messages)
    }

    @Test
    fun `the stream reads a pretty-printed message across its lines`() {
        val text = """
            {
              "version": "1.0",
              "updateComponents": {
                "surfaceId": "s",
                "components": [
                  {"id": "root", "component": "Column", "children": ["a"]},
                  {
                    "id": "a", "component": "Text", "text": "{ not a brace }"
                  }
                ]
              }
            }
            {"version": "1.0", "deleteSurface": {"surfaceId": "s"}}
        """.trimIndent()

        val messages = A2UIStream.messages(text)

        assertEquals(2, messages.size)
        assertTrue(messages.all { A2UIJson.parseOrNull(it) is Map<*, *> })
    }

    @Test
    fun `a message with a stray quote ends at its line, and the messages after it still count`() {
        val text = """
            {"version": "1.0", "createSurface": {"surfaceId": "a"}}
            {"version": "1.0", "updateComponents": {"surfaceId": "a", "components": [{"id": "t", "text">Day 1"}]}}
            {"version": "1.0", "createSurface": {"surfaceId": "b"}}
            {"version": "1.0", "createSurface": {"surfaceId": "c", "note": "a } and a \" in a string"}}
        """.trimIndent()

        val messages = A2UIStream.messages(text)

        assertEquals(text.lines(), messages)
        assertNull(A2UIJson.parseOrNull(messages[1]))
        assertEquals(listOf("a", "b", "c"), listOf(0, 2, 3).map { surfaceId(messages[it]) })
    }

    @Test
    fun `a message missing a brace ends where the next one starts`() {
        val text = listOf(
            """{"version": "1.0", "createSurface": {"surfaceId": "a"}""",
            """{"version": "1.0", "createSurface": {"surfaceId": "b"}}""",
            """{"version": "1.0", "createSurface": {"surfaceId": "c"}}""",
        ).joinToString("\n")

        val messages = A2UIStream.messages(text)

        assertEquals(text.lines(), messages)
        assertNull(A2UIJson.parseOrNull(messages[0]))
        assertEquals(listOf("b", "c"), messages.drop(1).map(::surfaceId))
    }

    @Test
    fun `the rest of a broken pretty-printed message is skipped, not read as messages of its own`() {
        val broken = """
            {"version": "1.0", "updateComponents": {"surfaceId": "a", "components": [
              {"id": "t", "text">Day 1"},
              {"id": "u", "component": "Text"},
              {"id": "w", "component": "Text"}
            ]}}
        """.trimIndent()
        val newline = """
            {"version": "1.0", "updateComponents": {"surfaceId": "a", "components": [
              {"id": "t", "component": "Text", "text": "Day 1
            Use {name} here"}
            ]}}
        """.trimIndent()
        val next = """{"version": "1.0", "createSurface": {"surfaceId": "b"}}"""

        listOf(broken, newline).forEach { text ->
            val messages = A2UIStream.messages(text + "\n" + next)

            assertEquals(2, messages.size)
            assertNull(A2UIJson.parseOrNull(messages[0]))
            assertEquals("b", surfaceId(messages[1]))
        }
    }

    @Test
    fun `a missing comma between indented objects stays inside its message`() {
        val text = """
            {"version": "1.0", "updateComponents": {"surfaceId": "a", "components": [
              {"id": "t", "component": "Text"}
              {"id": "u", "component": "Text"}
            ]}}
            {"version": "1.0", "createSurface": {"surfaceId": "b"}}
        """.trimIndent()

        val messages = A2UIStream.messages(text)

        assertEquals(2, messages.size)
        assertNull(A2UIJson.parseOrNull(messages[0]))
        assertEquals("b", surfaceId(messages[1]))
    }

    @Test
    fun `the line still being written after a broken message doesn't count yet`() {
        val broken = """{"version": "1.0", "updateComponents": {"surfaceId": "a", "text">x"}}"""
        val next = """{"version": "1.0", "createSurface": {"surfaceId": "b"}}"""

        for (length in 0 until next.length) {
            assertEquals(listOf(broken), A2UIStream.messages(broken + "\n" + next.take(length)))
        }
        assertEquals(listOf(broken, next), A2UIStream.messages(broken + "\n" + next))
    }

    @Test
    fun `a string still being written may hold braces and quotes`() {
        val partial = """{"version": "1.0", "createSurface": {"surfaceId": "s", "note": "a { and a \" so far"""

        assertTrue(A2UIStream.messages(partial).isEmpty())
        assertEquals(1, A2UIStream.messages("$partial\"}}").size)
    }

    private fun surfaceId(message: String): Any? =
        A2UIJson.map(A2UIJson.map(A2UIJson.parse(message))!!["createSurface"])!!["surfaceId"]

    @Test
    fun `dates keep the zone they were written in`() {
        val utc = TimeZone.getTimeZone("UTC")

        assertEquals(
            "2026-09-04T23:30:00.000-05:00",
            A2UIDates.iso(A2UIDates.parse("2026-09-04T23:30:00-05:00", utc)!!),
        )
        assertEquals("2026-09-04T23:30:00.000Z", A2UIDates.iso(A2UIDates.parse("2026-09-04 23:30", utc)!!))
        assertEquals("1970-01-01T09:30:00.000Z", A2UIDates.iso(A2UIDates.parse("09:30", utc)!!))
        assertEquals(1_700_000_000_000L, A2UIDates.parse(1_700_000_000L, utc)!!.millis)
        assertEquals(1_700_000_000_123L, A2UIDates.parse(1_700_000_000_123L, utc)!!.millis)
        assertNull(A2UIDates.parse("2026-02-30", utc))
        assertNull(A2UIDates.parse("tomorrow", utc))
    }

    @Test
    fun `data paths read relative paths in their scope`() {
        val item = DataPath.parse("/items/1")

        assertEquals("/items/1/name", item.appending(DataPath.parse("name")).toString())
        assertEquals("/items/1/name", item.appending(DataPath.parse("./name")).toString())
        assertEquals("/other", item.appending(DataPath.parse("/other")).toString())
        assertEquals("/a~1b/c~0d", DataPath.parse("/a~1b/c~0d").toString())
        assertTrue(DataPath.isValidSyntax("/a~0b"))
        assertTrue(!DataPath.isValidSyntax("/a~2b"))
    }

    @Test
    fun `the basic catalog has every component and function of the spec`() {
        val catalog = BasicCatalog.catalog

        assertEquals(A2UIProtocol.BASIC_CATALOG_ID, catalog.catalogId)
        assertEquals(
            setOf(
                "Text", "Image", "Icon", "Video", "AudioPlayer", "Row", "Column", "List", "Card", "Tabs", "Modal",
                "Divider",
                "Button", "TextField", "CheckBox", "ChoicePicker", "Slider", "DateTimeInput",
            ),
            catalog.components.keys,
        )
        assertEquals(BasicFunctions.implementations.keys, catalog.functions.keys)
        assertTrue(catalog.functions.values.all { it.implementation != null })
        assertEquals(
            listOf("checks", "child", "action", "variant", "weight").sorted(),
            catalog.component("Button")!!.properties.keys.sorted(),
        )
        assertEquals(true, catalog.function("openUrl")!!.requiresUserActivation)
    }
}
