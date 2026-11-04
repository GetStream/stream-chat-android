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
import kotlin.test.assertNull

/** Parser errors enriched with hints for a correction loop. */
internal class OpenUIErrorTest {
    private val schema = OpenUIJson.parse(
        """{"${'$'}defs": {"Card": {"properties": {"title": {}, "body": {}}, "required": ["title"]}}}""",
    )

    private fun enrich(code: ValidationErrorCode, component: String = "Card", path: String = ""): OpenUIError =
        OpenUIError.enriched(
            listOf(ValidationError(code, component, path, "message", statementId = "s")),
            schema,
            listOf("Card", "Text"),
        ).single()

    @Test
    fun `unknown components list the available ones`() {
        val error = enrich(ValidationErrorCode.UnknownComponent, component = "Nope")
        assertEquals("Available components: Card, Text", error.hint)
        assertEquals(OpenUIError.Source.Parser, error.source)
        assertEquals("unknown-component", error.code)
        assertEquals("s", error.statementId)
        assertNull(error.path)
        val none = OpenUIError.enriched(
            listOf(ValidationError(ValidationErrorCode.UnknownComponent, "Nope", "", "message")),
            schema,
            emptyList(),
        ).single()
        assertNull(none.hint)
    }

    @Test
    fun `missing and null props show the signature`() {
        assertEquals("Signature: Card(title*, body) — * marks required", enrich(ValidationErrorCode.MissingRequired).hint)
        assertEquals("Signature: Card(title*, body) — * marks required", enrich(ValidationErrorCode.NullRequired).hint)
        assertNull(enrich(ValidationErrorCode.MissingRequired, component = "Unknown").hint)
    }

    @Test
    fun `inline reserved calls suggest a statement`() {
        val error = enrich(ValidationErrorCode.InlineReserved, component = "Query", path = "/data")
        assertEquals("Declare as a top-level statement: myVar = Query(...)", error.hint)
        assertEquals("/data", error.path)
    }

    @Test
    fun `other codes have no hint, and errors print their source and code`() {
        val error = enrich(ValidationErrorCode("excess-args"))
        assertNull(error.hint)
        assertEquals("parser/excess-args: message", error.toString())
        assertEquals("runtime", OpenUIError.Source.Runtime.toString())
        assertEquals(
            listOf("query", "mutation"),
            listOf(OpenUIError.Source.Query, OpenUIError.Source.Mutation).map { it.wireName },
        )
    }
}
