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

package io.getstream.chat.android.ai.openui.generativeui

import io.getstream.chat.android.ai.compose.generativeui.AIUIAction
import io.getstream.chat.android.ai.compose.generativeui.AIUIComponent
import io.getstream.chat.android.ai.compose.generativeui.AIUIComponentDefinition
import io.getstream.chat.android.ai.compose.generativeui.AIUIDocument
import io.getstream.chat.android.ai.compose.generativeui.AIUIFormat
import io.getstream.chat.android.ai.compose.generativeui.AIUIHost
import io.getstream.chat.android.ai.compose.generativeui.AIUIParameter
import io.getstream.chat.android.ai.compose.generativeui.AIUIParameterType
import io.getstream.chat.android.ai.compose.parts.AIClientTool
import io.getstream.chat.android.ai.compose.parts.AIClientToolResult
import io.getstream.chat.android.ai.compose.parts.AIToolCallPart
import io.getstream.chat.android.ai.openui.lang.ActionEvent
import io.getstream.chat.android.ai.openui.lang.BuiltinActionType
import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.rendering.openuiChatLibrary
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class OpenUIEngineTest {

    private val sent = mutableListOf<AIUIAction>()
    private val placeCard = AIUIComponent(
        AIUIComponentDefinition(
            name = "place_card",
            description = "A place.",
            parameters = listOf(AIUIParameter.required("title", AIUIParameterType.String)),
        ),
    ) { _, _ -> }

    private fun TestScope.engine(host: AIUIHost = host()): OpenUIEngine =
        OpenUIEngine(openuiChatLibrary, host, HostToolProvider(host), backgroundScope)

    private fun host(tools: List<AIClientTool> = emptyList()) =
        AIUIHost(renderers = listOf(OpenUIRenderer()), components = listOf(placeCard), tools = tools) { sent += it }

    private fun reply(program: String, isStreaming: Boolean = false) =
        AIUIDocument(AIUIFormat.OpenUI, isStreaming = isStreaming, content = program)

    @Test
    fun `an OpenUI action reaches the agent shaped like on every platform`() {
        val event = ActionEvent(
            type = BuiltinActionType.CONTINUE_CONVERSATION,
            params = OpenUIObject.of("context" to OpenUIValue.Str("hotel-7"), "night" to OpenUIValue.Num(2.0)),
            humanFriendlyMessage = "Book it",
            formState = OpenUIObject.of("booking" to OpenUIValue.obj("guests" to OpenUIValue.Num(3.0))),
            formName = "booking",
        )

        val action = OpenUIEngine.action(event, "m1")

        assertEquals(
            AIUIAction(
                messageId = "m1",
                format = AIUIFormat.OpenUI,
                name = "continue_conversation",
                label = "Book it",
                payload = mapOf(
                    "context" to "hotel-7",
                    "params" to mapOf("night" to 2),
                    "form" to "booking",
                    "values" to mapOf("booking" to mapOf("guests" to 3)),
                ),
            ),
            action,
        )
    }

    @Test
    fun `an action without a label is named by its context, then its type`() {
        val withContext = ActionEvent(
            "continue_conversation",
            OpenUIObject.of("context" to OpenUIValue.Str("More")),
            humanFriendlyMessage = "",
        )
        val custom = ActionEvent("add_to_cart", OpenUIObject.of("sku" to OpenUIValue.Str("a1")), humanFriendlyMessage = "")

        assertEquals("More", OpenUIEngine.action(withContext, "m1")?.label)
        assertEquals(mapOf<String, Any?>("context" to "More"), OpenUIEngine.action(withContext, "m1")?.payload)
        assertEquals("add_to_cart", OpenUIEngine.action(custom, "m1")?.label)
        assertEquals(mapOf("params" to mapOf("sku" to "a1")), OpenUIEngine.action(custom, "m1")?.payload)
    }

    @Test
    fun `a link is no action for the agent`() {
        val link = ActionEvent("open_url", OpenUIObject.of("url" to OpenUIValue.Str("https://example.com")), "")
        assertNull(OpenUIEngine.action(link, "m1"))
    }

    @Test
    fun `a reply keeps its runtime, which follows its program`() = runTest {
        val engine = engine()
        val partial = "root = Card([TextContent(\"Oh"

        assertNotNull(engine.content(reply(partial, isStreaming = true), "m1"))
        val runtime = assertNotNull(engine.runtime("m1"))
        assertEquals(partial, runtime.response)

        val program = "root = Card([TextContent(\"Ohrid\")])"
        engine.receive(reply(program), "m1")
        engine.content(reply(program), "m1")

        assertSame(runtime, engine.runtime("m1"))
        assertEquals(program, runtime.response)
        assertEquals(false, runtime.isStreaming)
        assertEquals("Card", runtime.root?.typeName)
    }

    @Test
    fun `a reply that never showed has no runtime`() = runTest {
        val engine = engine()
        engine.receive(reply("root = Card([])"), "m1")
        assertNull(engine.runtime("m1"))
        assertNull(engine.content(AIUIDocument(AIUIFormat.OpenUI, content = listOf(1)), "m1"))
    }

    @Test
    fun `a reply's state is kept by the host, and restored`() = runTest {
        val host = host()
        val program = "\$name = \"\"\nroot = Card([Input(\"name\", \"Your name\", \"text\", null, \$name)])"
        val first = engine(host)
        first.content(reply(program), "m1")
        assertNotNull(first.runtime("m1")).setFieldValue(null, "Input", "\$name", OpenUIValue.Str("Ana"))

        val state = host.state("m1") as Map<*, *>
        assertEquals("Ana", (state["\$name"] as Map<*, *>)["value"])

        val second = engine(host)
        second.content(reply(program), "m1")
        assertEquals(OpenUIValue.Str("Ana"), assertNotNull(second.runtime("m1")).fieldValue(null, "\$name"))
    }

    @Test
    fun `a follow-up reaches the host as the person's message`() = runTest {
        val engine = engine()
        engine.content(reply("root = Card([FollowUpBlock([FollowUpItem(\"Tell me more\")])])"), "m1")

        assertNotNull(engine.runtime("m1")).triggerAction("Tell me more")

        val action = sent.single()
        assertEquals("m1", action.messageId)
        assertEquals("continue_conversation", action.name)
        assertEquals("Tell me more", action.label)
    }

    @Test
    fun `the app's components are placed by their OpenUI name`() = runTest {
        val engine = engine()
        engine.content(reply("root = Card([PlaceCard(\"Ohrid\")])"), "m1")

        assertNotNull(engine.library.renderer("PlaceCard"))
        val card = assertNotNull(engine.runtime("m1")?.root)
        val child = (card.props["children"] as OpenUIValue.Arr).items.single() as OpenUIValue.Element
        assertEquals("PlaceCard", child.node.typeName)
        assertEquals(OpenUIValue.Str("Ohrid"), child.node.props["title"])
    }

    @Test
    fun `every built-in part comes in as it shows`() = runTest {
        val engine = engine()
        openuiChatLibrary.components.values.forEach { component ->
            val renderer = engine.library.renderer(component.name)
            if (component.component == null) assertNull(renderer) else assertNotNull(renderer)
        }
    }

    @Test
    fun `queries run the host's tools`() = runTest {
        val tool = object : AIClientTool {
            override val name = "weather"
            override suspend fun run(call: AIToolCallPart) = AIClientToolResult.completed("{\"temperature\":21.5}")
        }
        val provider = HostToolProvider(host(listOf(tool)))

        val output = provider.callTool("weather", OpenUIObject.of("city" to OpenUIValue.Str("Ohrid")))

        assertEquals(OpenUIValue.obj("temperature" to OpenUIValue.Num(21.5)), output)
    }
}
