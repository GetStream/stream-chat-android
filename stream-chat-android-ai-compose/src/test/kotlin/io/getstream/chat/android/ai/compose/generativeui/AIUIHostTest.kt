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

package io.getstream.chat.android.ai.compose.generativeui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.UriHandler
import io.getstream.chat.android.ai.compose.conversation.AIMessage
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.parts.AIClientTool
import io.getstream.chat.android.ai.compose.parts.AIClientToolResult
import io.getstream.chat.android.ai.compose.parts.AIToolCallPart
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

internal class AIUIHostTest {

    private class Reply(
        override val id: String,
        override val uiDocument: AIUIDocument?,
        override val role: AIMessageRole = AIMessageRole.Assistant,
        override val text: String = "",
    ) : AIMessage

    private class Engine(val showsNothing: Boolean) : AIUIEngine {
        val received = mutableListOf<Pair<String, AIUIDocument>>()
        val asked = mutableListOf<String>()

        override fun receive(document: AIUIDocument, messageId: String) {
            received += messageId to document
        }

        override fun content(document: AIUIDocument, messageId: String): (@Composable () -> Unit)? {
            asked += messageId
            return if (showsNothing) null else ({ Text(document.text.orEmpty()) })
        }
    }

    private class Renderer(
        override val format: AIUIFormat,
        val versions: Set<String?>? = null,
        val showsNothing: Boolean = false,
    ) : AIUIRenderer {
        val engines = mutableListOf<Engine>()

        override fun supports(version: String?): Boolean = versions == null || version in versions

        override fun capabilities(components: List<AIUIComponent>): Map<String, Any?> =
            mapOf("components" to components.map { it.name })

        override fun makeEngine(host: AIUIHost): AIUIEngine = Engine(showsNothing).also { engines += it }
    }

    private fun document(text: String, format: AIUIFormat = AIUIFormat.OpenUI, version: String? = null) =
        AIUIDocument(format = format, version = version, content = text)

    private val component = AIUIComponent(AIUIComponentDefinition("place_card", "A place.")) { _, _ -> }

    @Test
    fun `the first renderer that reads a document's format and version shows it`() {
        val old = Renderer(AIUIFormat.OpenUI, versions = setOf("0.4"))
        val current = Renderer(AIUIFormat.OpenUI)
        val a2ui = Renderer(AIUIFormat.A2UI)
        val host = AIUIHost(listOf(old, current, a2ui)) {}

        assertTrue(host.canShow(document("x", version = "1.0"), "m1"))
        assertTrue(host.canShow(document("x", format = AIUIFormat.A2UI), "m2"))
        assertTrue(host.canShow(document("x", version = "0.4"), "m3"))

        assertEquals(listOf("m3"), old.engines.single().asked)
        assertEquals(listOf("m1"), current.engines.single().asked)
        assertEquals(listOf("m2"), a2ui.engines.single().asked)
    }

    @Test
    fun `a document no renderer reads, or one whose renderer shows nothing, shows the reply's text`() {
        val host = AIUIHost(listOf(Renderer(AIUIFormat.OpenUI, showsNothing = true))) {}

        assertFalse(host.reads(document("x", format = AIUIFormat("acme"))))
        assertFalse(host.canShow(document("x", format = AIUIFormat("acme")), "m1"))
        assertTrue(host.reads(document("x")))
        assertFalse(host.canShow(document("x"), "m1"))
    }

    @Test
    fun `each renderer gets one engine, made the first time it is needed and kept`() {
        val renderer = Renderer(AIUIFormat.OpenUI)
        val host = AIUIHost(listOf(renderer)) {}

        assertTrue(renderer.engines.isEmpty())
        host.canShow(document("a"), "m1")
        host.receive(listOf(Reply("m2", document("b"))))
        host.canShow(document("c"), "m3")

        assertEquals(1, renderer.engines.size)
    }

    @Test
    fun `engines receive every reply's document oldest first, and again only when it changes`() {
        val renderer = Renderer(AIUIFormat.OpenUI)
        val host = AIUIHost(listOf(renderer)) {}
        val question = Reply("q1", document("from the person"), role = AIMessageRole.User)

        host.receive(listOf(question, Reply("m1", document("a")), Reply("m2", null), Reply("m3", document("b"))))
        host.receive(listOf(question, Reply("m1", document("a")), Reply("m3", document("b2"))))

        assertEquals(
            listOf("m1" to document("a"), "m3" to document("b"), "m3" to document("b2")),
            renderer.engines.single().received,
        )
    }

    @Test
    fun `a reply's state is kept while the host is`() {
        val host = AIUIHost(emptyList()) {}

        host.setState("m1", mapOf("name" to "Ana"))

        assertEquals(mapOf("name" to "Ana"), host.state("m1"))
        assertNull(host.state("m2"))
        host.setState("m1", null)
        assertNull(host.state("m1"))
    }

    @Test
    fun `actions go where the app sends them`() {
        val sent = mutableListOf<AIUIAction>()
        val host = AIUIHost(emptyList()) { sent += it }
        val action = AIUIAction("m1", AIUIFormat.OpenUI, "continue_conversation", "More")

        host.send(action)

        assertEquals(listOf(action), sent)
    }

    @Test
    fun `tools run on the device with the arguments a reply's UI gives them`() = runTest {
        val calls = mutableListOf<AIToolCallPart>()
        val tool = object : AIClientTool {
            override val name = "calendar"
            override suspend fun run(call: AIToolCallPart): AIClientToolResult {
                calls += call
                return AIClientToolResult.completed("{\"events\":[\"Lunch\"]}")
            }
        }
        val failing = object : AIClientTool {
            override val name = "location"
            override suspend fun run(call: AIToolCallPart) = AIClientToolResult.failed("Location not shared")
        }
        val host = AIUIHost(emptyList(), tools = listOf(tool, failing)) {}

        val output = host.runTool("calendar", mapOf("day" to "2026-11-14"))

        assertEquals(mapOf("events" to listOf("Lunch")), output)
        assertEquals("{\"day\":\"2026-11-14\"}", calls.single().arguments)
        assertEquals(AIToolCallPart.Executor.Client, calls.single().executor)
        assertFailure("Location not shared") { host.runTool("location") }
        assertFailure("No tool named weather") { host.runTool("weather") }
    }

    @Test
    fun `web and email links open, unless the app picks other schemes`() {
        val host = AIUIHost(emptyList()) {}
        val httpsOnly = AIUIHost(emptyList(), linkSchemes = setOf("HTTPS")) {}

        assertTrue(host.canOpen("https://en.wikipedia.org/wiki/Ohrid"))
        assertTrue(host.canOpen("HTTPS://example.com"))
        assertTrue(host.canOpen("http://example.com"))
        assertTrue(host.canOpen("mailto:ana@example.com"))
        assertFalse(host.canOpen("javascript:alert(1)"))
        assertFalse(host.canOpen("intent://scan/#Intent;scheme=zxing;end"))
        assertFalse(host.canOpen("file:///sdcard/secret.txt"))
        assertFalse(host.canOpen("https:example.com"))
        assertFalse(host.canOpen("https:///path"))
        assertFalse(host.canOpen("https://exa mple.com"))
        assertFalse(host.canOpen("/relative"))
        assertTrue(httpsOnly.canOpen("https://example.com"))
        assertFalse(httpsOnly.canOpen("http://example.com"))
        assertFalse(httpsOnly.canOpen("mailto:ana@example.com"))
    }

    @Test
    fun `a link opens with the handler it is given only when the host lets it`() {
        val opened = mutableListOf<String>()
        val handler = object : UriHandler {
            override fun openUri(uri: String) {
                opened += uri
            }
        }
        val host = AIUIHost(emptyList()) {}

        assertTrue(host.open("https://example.com", handler))
        assertFalse(host.open("javascript:alert(1)", handler))

        assertEquals(listOf("https://example.com"), opened)
    }

    @Test
    fun `capabilities name each format with what its renderer adds, and the app's components`() {
        val host = AIUIHost(listOf(Renderer(AIUIFormat.OpenUI)), components = listOf(component)) {}

        assertEquals(
            mapOf(
                "v" to 1,
                "renderers" to listOf(mapOf("format" to "openui", "components" to listOf("place_card"))),
                "components" to listOf(component.definition.toJson()),
            ),
            host.capabilities,
        )
        assertNotNull(host.component("place_card"))
        assertNull(host.component("map"))
    }

    private suspend fun assertFailure(message: String, block: suspend () -> Unit) {
        try {
            block()
            fail("Expected $message")
        } catch (expected: AIUIToolException) {
            assertEquals(message, expected.message)
        }
    }
}
