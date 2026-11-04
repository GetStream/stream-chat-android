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
import io.getstream.chat.android.ai.compose.parts.AIClientToolDefinition
import io.getstream.chat.android.ai.compose.parts.AIClientToolResult
import io.getstream.chat.android.ai.compose.parts.AIToolCallPart
import io.getstream.chat.android.ai.compose.parts.definition
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
        val removed = mutableListOf<String>()
        val asked = mutableListOf<String>()

        override fun receive(document: AIUIDocument, messageId: String) {
            received += messageId to document
        }

        override fun remove(messageId: String) {
            removed += messageId
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

        override fun capabilities(components: List<AIUIComponent>, tools: List<AIClientTool>): Map<String, Any?> =
            mapOf("components" to components.map { it.name }, "tools" to tools.map { it.definition.toJson() })

        override fun makeEngine(host: AIUIHost): AIUIEngine = Engine(showsNothing).also { engines += it }
    }

    // A renderer whose UI runs none of the app's tools, so it lists none.
    private class ToolessRenderer : AIUIRenderer {
        override val format: AIUIFormat = AIUIFormat.A2UI

        override fun capabilities(components: List<AIUIComponent>, tools: List<AIClientTool>): Map<String, Any?> =
            mapOf("components" to components.size)

        override fun makeEngine(host: AIUIHost): AIUIEngine = Engine(showsNothing = false)
    }

    private fun tool(
        name: String,
        readOnly: Boolean = false,
        ran: MutableList<String>? = null,
    ) = object : AIClientTool {
        override val name: String = name
        override val description: String = "The $name."
        override val inputSchema: Map<String, Any?> = mapOf("type" to "object")
        override val isReadOnly: Boolean = readOnly
        override suspend fun run(call: AIToolCallPart): AIClientToolResult {
            ran?.add(name)
            return AIClientToolResult.completed("{}")
        }
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

        assertTrue(host.showsUI(document("x", version = "1.0"), "m1"))
        assertTrue(host.showsUI(document("x", format = AIUIFormat.A2UI), "m2"))
        assertTrue(host.showsUI(document("x", version = "0.4"), "m3"))

        assertEquals(listOf("m3"), old.engines.single().asked)
        assertEquals(listOf("m1"), current.engines.single().asked)
        assertEquals(listOf("m2"), a2ui.engines.single().asked)
    }

    @Test
    fun `a document no renderer reads, or one whose renderer shows nothing, shows the reply's text`() {
        val host = AIUIHost(listOf(Renderer(AIUIFormat.OpenUI, showsNothing = true))) {}

        assertFalse(host.canShow(document("x", format = AIUIFormat("acme"))))
        assertFalse(host.showsUI(document("x", format = AIUIFormat("acme")), "m1"))
        assertTrue(host.canShow(document("x")))
        assertFalse(host.showsUI(document("x"), "m1"))
    }

    @Test
    fun `each renderer gets one engine, made the first time it is needed and kept`() {
        val renderer = Renderer(AIUIFormat.OpenUI)
        val host = AIUIHost(listOf(renderer)) {}

        assertTrue(renderer.engines.isEmpty())
        host.showsUI(document("a"), "m1")
        host.receive(listOf(Reply("m2", document("b"))))
        host.showsUI(document("c"), "m3")

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
    fun `a reply that leaves the conversation, or whose UI does, is forgotten`() {
        val renderer = Renderer(AIUIFormat.OpenUI)
        val host = AIUIHost(listOf(renderer)) {}
        host.receive(listOf(Reply("m1", document("a")), Reply("m2", document("b")), Reply("m3", document("c"))))
        host.setState("m1", mapOf("name" to "Ana"))

        host.receive(listOf(Reply("m2", null), Reply("m3", document("c"))))
        host.receive(listOf(Reply("m1", document("a")), Reply("m3", document("c"))))

        val engine = renderer.engines.single()
        assertEquals(listOf("m1", "m2"), engine.removed)
        assertNull(host.state("m1"))
        assertEquals(listOf("m1", "m2", "m3", "m1"), engine.received.map { it.first })
    }

    @Test
    fun `the host keeps its own copy of the renderers and components`() {
        val renderers = mutableListOf<AIUIRenderer>(Renderer(AIUIFormat.OpenUI))
        val components = mutableListOf(component)
        val host = AIUIHost(renderers, components) {}

        renderers.clear()
        components.clear()

        assertTrue(host.canShow(document("x")))
        assertNotNull(host.component("place_card"))
    }

    @Test
    fun `a tool that throws, or answers with text that is not JSON, fails`() = runTest {
        val throwing = object : AIClientTool {
            override val name = "contacts"
            override suspend fun run(call: AIToolCallPart): AIClientToolResult = throw SecurityException("No access")
        }
        val bare = object : AIClientTool {
            override val name = "status"
            override suspend fun run(call: AIToolCallPart) = AIClientToolResult.completed("ok")
        }
        val host = AIUIHost(emptyList(), tools = listOf(throwing, bare)) {}

        assertFailure("contacts failed") { host.runTool("contacts", userInitiated = true) }
        assertFailure("status returned output that is not JSON") { host.runTool("status", userInitiated = true) }
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
            override val isReadOnly = true
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
        assertFailure("Location not shared") { host.runTool("location", userInitiated = true) }
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
        assertFalse(host.canOpen("https://:443/path"))
        assertFalse(host.canOpen("https://ana@:443"))
        assertTrue(host.canOpen("https://example.com:8443/path"))
        assertTrue(host.canOpen("https://[::1]:8443/"))
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
        val host = AIUIHost(
            renderers = listOf(Renderer(AIUIFormat.OpenUI), ToolessRenderer()),
            components = listOf(component),
            tools = listOf(tool("weather", readOnly = true), tool("contacts"), tool("weather")),
        ) {}

        assertEquals(
            mapOf(
                "v" to 1,
                "renderers" to listOf(
                    // The renderer gets the host's tools, each once, by name, with their definitions.
                    mapOf(
                        "format" to "openui",
                        "components" to listOf("place_card"),
                        "tools" to listOf(
                            AIClientToolDefinition("contacts", "The contacts.", mapOf("type" to "object")).toJson(),
                            AIClientToolDefinition("weather", "The weather.", mapOf("type" to "object"), isReadOnly = true).toJson(),
                        ),
                    ),
                    mapOf("format" to "a2ui", "components" to 1),
                ),
                "components" to listOf(component.definition.toJson()),
            ),
            host.capabilities,
        )
        assertNotNull(host.component("place_card"))
        assertNull(host.component("map"))
    }

    @Test
    fun `a read-only tool runs whenever the UI asks, any other only when the person asks`() = runTest {
        val ran = mutableListOf<String>()
        val host = AIUIHost(
            emptyList(),
            tools = listOf(tool("calendar", readOnly = true, ran = ran), tool("book", ran = ran)),
        ) {}

        assertEquals(emptyMap<String, Any?>(), host.runTool("calendar"))
        assertFailure("book isn't read-only, so it runs only when the person asks for it") { host.runTool("book") }
        assertEquals(listOf("calendar"), ran)

        assertEquals(emptyMap<String, Any?>(), host.runTool("book", userInitiated = true))
        assertEquals(listOf("calendar", "book"), ran)
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
