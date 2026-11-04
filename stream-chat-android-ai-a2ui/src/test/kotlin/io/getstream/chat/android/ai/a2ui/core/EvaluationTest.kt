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

/** Basic functions and expressions evaluated against agent input. */
internal class EvaluationTest {

    private val errors = mutableListOf<RendererError>()
    private val router = object : EvaluatorRouter {
        override fun agentFunctionValue(
            name: String,
            catalogId: String?,
            arguments: Map<String, Any?>,
            surfaceId: String,
            userActivated: Boolean,
        ): Any? = null

        override fun report(error: RendererError) {
            errors += error
        }
    }

    private fun evaluator(services: RendererServices = Harness.testServices()) =
        Evaluator(CatalogRegistry(listOf(BasicCatalog.catalog)), "s", A2UIProtocol.BASIC_CATALOG_ID, services, router)

    private val context = DataContext(DataModel(mapOf("a/b" to "slash", "items" to listOf(1))))

    private fun call(name: String, args: Map<String, Any?>, evaluator: Evaluator = evaluator()): Any? =
        evaluator.evaluateRaw(mapOf("@call" to name, "args" to args), context)

    @Test
    fun `regex rejects a pattern that could backtrack catastrophically`() {
        val result = call("regex", mapOf("pattern" to "(a+)+$", "value" to "a".repeat(40) + "b"))

        assertNull(result)
        assertEquals(RendererError.EXPRESSION_ERROR, errors.single().code)
    }

    @Test
    fun `regex rejects a bounded pattern that backtracks exponentially`() {
        val result = call("regex", mapOf("pattern" to "(a?){25}a{25}", "value" to "a".repeat(25)))

        assertNull(result)
        assertEquals(RendererError.EXPRESSION_ERROR, errors.single().code)
    }

    @Test
    fun `regex does not match a value longer than 4096 code points`() {
        val result = call("regex", mapOf("pattern" to "a", "value" to "a".repeat(A2UIRegex.MAX_VALUE_LENGTH + 1)))

        assertEquals(false, A2UIJson.map(result)!!["valid"])
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `openUrl reports a link the app could not open`() {
        val evaluator = evaluator(Harness.testServices(openUrl = { false }))

        evaluator.withUserActivation { call("openUrl", mapOf("url" to "https://example.com"), evaluator) }

        assertEquals(RendererError.EXECUTION_ERROR, errors.single().code)
    }

    @Test
    fun `a date with an offset no clock shows reads as no date`() {
        listOf("2026-01-01T10:00:00+24:00", "2026-01-01T10:00:00+05:60").forEach { date ->
            assertEquals(date, "", call("formatDate", mapOf("value" to date, "format" to "ISO")))
        }
        assertEquals(
            "2026-01-01T10:00:00.000+05:30",
            call("formatDate", mapOf("value" to "2026-01-01T10:00:00+05:30", "format" to "ISO")),
        )
    }

    @Test
    fun `a nested literal object keeps an explicit null`() {
        val result = evaluator().evaluateRaw(
            mapOf("outer" to mapOf("kept" to null, "unresolved" to mapOf("@path" to "/missing"), "n" to 1)),
            context,
        )

        assertEquals(mapOf("outer" to mapOf("kept" to null, "n" to 1)), result)
    }

    @Test
    fun `an interpolated path keeps its JSON Pointer escapes`() {
        assertEquals("slash", call("formatString", mapOf("value" to "\${/a~1b}")))
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `an interpolated path with a malformed escape is an error`() {
        call("formatString", mapOf("value" to "\${/a~2b}"))

        assertEquals(RendererError.EXPRESSION_ERROR, errors.single().code)
    }
}
