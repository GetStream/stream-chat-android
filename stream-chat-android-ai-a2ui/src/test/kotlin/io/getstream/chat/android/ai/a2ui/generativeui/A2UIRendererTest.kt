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

package io.getstream.chat.android.ai.a2ui.generativeui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.a2ui.compose.A2UICatalog
import io.getstream.chat.android.ai.a2ui.core.A2UIJson
import io.getstream.chat.android.ai.a2ui.core.DataPath
import io.getstream.chat.android.ai.a2ui.core.Fixtures
import io.getstream.chat.android.ai.compose.generativeui.AIUIAction
import io.getstream.chat.android.ai.compose.generativeui.AIUIComponent
import io.getstream.chat.android.ai.compose.generativeui.AIUIComponentDefinition
import io.getstream.chat.android.ai.compose.generativeui.AIUIDocument
import io.getstream.chat.android.ai.compose.generativeui.AIUIFormat
import io.getstream.chat.android.ai.compose.generativeui.AIUIHost
import io.getstream.chat.android.ai.compose.generativeui.AIUIParameter
import io.getstream.chat.android.ai.compose.generativeui.AIUIParameterType
import io.getstream.chat.android.ai.compose.generativeui.AIUIView
import io.getstream.chat.android.ai.compose.parts.AIClientTool
import io.getstream.chat.android.ai.compose.parts.AIClientToolResult
import io.getstream.chat.android.ai.compose.parts.AIToolCallPart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowLog

@Suppress("LargeClass")
@RunWith(AndroidJUnit4::class)
internal class A2UIRendererTest {

    @get:Rule
    val rule = createComposeRule()

    private val sent = mutableListOf<AIUIAction>()
    private val opened = mutableListOf<String>()

    private val placeCard = AIUIComponent(
        AIUIComponentDefinition(
            name = "place_card",
            description = "A short summary of a place",
            parameters = listOf(AIUIParameter.required("title", AIUIParameterType.String)),
            events = listOf(AIUIComponentDefinition.Event("ask_about", "The person asks about the place")),
        ),
    ) { arguments, context ->
        val title = arguments["title"] as? String ?: return@AIUIComponent
        Text("Place: $title")
        Text("Ask", modifier = Modifier.clickable { context.emit("ask_about", "Tell me about $title") })
    }

    // A component named like one of the basic catalog's, which mustn't replace it.
    private val card = AIUIComponent(
        AIUIComponentDefinition(name = "card", description = "Not the catalog's card"),
    ) { _, _ ->
        Text("App card")
    }

    // A component that remembers how often the person tapped it.
    private val tapCounter = AIUIComponent(
        AIUIComponentDefinition(name = "tap_counter", description = "Counts taps"),
    ) { _, _ ->
        var taps by remember { mutableIntStateOf(0) }
        Text("Taps: $taps", modifier = Modifier.clickable { taps++ })
    }

    private val renderer = A2UIRenderer()
    private val host = AIUIHost(
        renderers = listOf(renderer),
        components = listOf(placeCard, card, tapCounter),
    ) { sent.add(it) }

    private fun document(content: Any?, isStreaming: Boolean = false, version: String? = "1.0") =
        AIUIDocument(format = AIUIFormat.A2UI, version = version, isStreaming = isStreaming, content = content)

    private fun message(type: String, body: Map<String, Any?>, version: String = "1.0") =
        A2UIJson.write(mapOf("version" to version, type to body))

    private fun surface(
        vararg components: Map<String, Any?>,
        data: Map<String, Any?> = emptyMap(),
        catalogId: String? = null,
        surfaceId: String = "s",
        sendDataModel: Boolean = false,
    ) =
        message(
            "createSurface",
            buildMap {
                put("surfaceId", surfaceId)
                catalogId?.let { put("catalogId", it) }
                if (sendDataModel) put("sendDataModel", true)
                put("components", components.toList())
                put("dataModel", data)
            },
        )

    private fun show(document: AIUIDocument, messageId: String = "m1", host: AIUIHost = this.host) {
        val uriHandler = object : UriHandler {
            override fun openUri(uri: String) {
                opened.add(uri)
            }
        }
        rule.setContent {
            CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                MaterialTheme { AIUIView(host, document, messageId) }
            }
        }
    }

    @Test
    fun `it reads A2UI 1_0 under the names agents give it`() {
        assertEquals(AIUIFormat.A2UI, renderer.format)
        listOf(null, "1", "1.0", "1.0.0", "v1.0").forEach { assertTrue(it.toString(), renderer.supports(it)) }
        listOf("0.9", "v0.9", "2.0").forEach { assertFalse(it, renderer.supports(it)) }
    }

    @Test
    fun `its capabilities hold the fence, the catalog id and the instructions`() {
        val capabilities = renderer.capabilities(listOf(placeCard, card), emptyList())

        assertEquals(setOf("fence", "catalog_id", "instructions"), capabilities.keys)
        assertEquals("ui:a2ui@1.0", capabilities["fence"])
        assertEquals("getstream.io:app-catalog", capabilities["catalog_id"])
        val instructions = capabilities["instructions"] as String
        assertTrue(instructions.contains("`ui:a2ui@1.0`"))
        assertTrue(instructions.contains("# Generating user interfaces with A2UI v1.0"))
        assertTrue(instructions.contains("formatString"))
        val document = instructions.substringAfter("## The catalog `getstream.io:app-catalog`\n\n```json\n")
            .substringBefore("\n```")
        val catalog = A2UIJson.map(A2UIJson.parse(document))!!
        assertEquals("getstream.io:app-catalog", catalog["catalogId"])
        assertFalse(catalog.containsKey("instructions"))
        val components = A2UIJson.map(catalog["components"])!!
        assertTrue("PlaceCard" in components)
        assertTrue("Text" in components)
        assertFalse(A2UIJson.map(components["Card"])!!["description"] == "Not the catalog's card.")
        val placeCardSchema = A2UIJson.map(components["PlaceCard"])!!
        assertEquals("A short summary of a place. The person asks about the place.", placeCardSchema["description"])
        assertEquals(listOf("title", "component"), placeCardSchema["required"])
    }

    @Test
    fun `a component removed from the catalog isn't in the catalog the agent is told about`() {
        val renderer = A2UIRenderer(catalog = A2UICatalog.Basic.removing("Video", "Slider"))

        val instructions = renderer.capabilities(emptyList(), emptyList())["instructions"] as String
        val document = instructions.substringAfter("## The catalog `getstream.io:app-catalog`\n\n```json\n")
            .substringBefore("\n```")
        val components = A2UIJson.map(A2UIJson.map(A2UIJson.parse(document))!!["components"])!!
        assertFalse("Video" in components)
        assertFalse("Slider" in components)
        assertTrue("Text" in components)
        renderer.catalogs(emptyList()).forEach { catalog ->
            assertEquals(catalog.catalogId, setOf("Text", "Image"), catalog.components.keys.intersect(setOf("Text", "Image", "Video", "Slider")))
        }
    }

    @Test
    fun `a reply that uses a removed component shows the rest and reports it like an unknown one`() {
        val errors = mutableListOf<A2UIError>()
        val renderer = A2UIRenderer(
            catalog = A2UICatalog.Basic.removing("Video"),
            onError = { error, _ -> errors.add(error) },
        )
        val host = AIUIHost(renderers = listOf(renderer)) {}

        show(
            document(
                surface(
                    mapOf("id" to "root", "component" to "Column", "children" to listOf("title", "clip", "other")),
                    mapOf("id" to "title", "component" to "Text", "text" to "Highlights"),
                    mapOf("id" to "clip", "component" to "Video", "url" to "https://example.com/clip.mp4"),
                    mapOf("id" to "other", "component" to "Gallery"),
                ),
            ),
            host = host,
        )

        rule.onNodeWithText("Highlights").assertExists()
        rule.onNodeWithContentDescription("Play video").assertDoesNotExist()
        val video = errors.single { "'Video'" in it.message }
        val unknown = errors.single { "'Gallery'" in it.message }
        assertEquals(unknown.code, video.code)
        assertEquals(unknown.message.replace("Gallery", "Video"), video.message)
    }

    @Test
    fun `its instructions describe the host's tools before the catalog`() {
        val host = AIUIHost(
            renderers = listOf(renderer),
            tools = listOf(
                tool("search_places", description = "Places near the person.", schema = citySchema),
                tool("get_weather", readOnly = true),
            ),
        ) {}

        val instructions = (host.capabilities["renderers"] as List<*>).filterIsInstance<Map<*, *>>()
            .single()["instructions"] as String

        val introduction = instructions.substringAfter("## The app's tools\n\n").substringBefore("\n\n")
        assertTrue(introduction, introduction.contains("`{\"@call\": \"<name>\", \"args\": {…}}`"))
        assertTrue(introduction, introduction.contains("Any other runs only from a button's `functionCall` action"))
        assertEquals(
            "- `get_weather` (read-only)\n" +
                "- `search_places`: Places near the person. Arguments: " +
                "`{\"properties\":{\"city\":{\"type\":\"string\"}},\"type\":\"object\"}`",
            instructions.substringAfter(introduction).substringAfter("\n\n").substringBefore("\n\n"),
        )
        assertTrue(instructions.indexOf("## The app's tools") < instructions.indexOf("## The catalog"))
    }

    @Test
    fun `its instructions list no tools when the host has none`() {
        val instructions = renderer.capabilities(listOf(placeCard), emptyList())["instructions"] as String

        assertFalse(instructions.contains("## The app's tools"))
    }

    @Test
    fun `the app's instructions come first`() {
        val preamble = "Answer questions about places with UI."

        val instructions = A2UIRenderer(instructions = preamble).capabilities(emptyList(), emptyList())

        val text = instructions["instructions"] as String
        assertTrue(text, text.startsWith("$preamble\n\n## Writing UI in A2UI"))
        assertTrue((renderer.capabilities(emptyList(), emptyList())["instructions"] as String).startsWith("## Writing UI in A2UI"))
    }

    private val citySchema = mapOf("type" to "object", "properties" to mapOf("city" to mapOf("type" to "string")))

    private fun tool(
        name: String,
        description: String = "",
        schema: Map<String, Any?>? = null,
        readOnly: Boolean = false,
        runs: MutableList<String> = mutableListOf(),
        output: String = "{}",
    ) = object : AIClientTool {
        override val name: String = name
        override val description: String = description
        override val inputSchema: Map<String, Any?>? = schema
        override val isReadOnly: Boolean = readOnly
        override suspend fun run(call: AIToolCallPart): AIClientToolResult {
            runs.add(call.arguments.orEmpty())
            return AIClientToolResult.completed(output)
        }
    }

    private fun hostWith(vararg tools: AIClientTool) =
        AIUIHost(renderers = listOf(renderer), tools = tools.toList()) { sent.add(it) }

    private fun weatherText() = mapOf(
        "id" to "root",
        "component" to "Text",
        "text" to mapOf("@call" to "weather", "args" to mapOf("city" to "Ohrid")),
    )

    @Test
    fun `a read-only tool runs from a binding, and its output is the function's value`() {
        val runs = mutableListOf<String>()
        val host = hostWith(tool("weather", readOnly = true, runs = runs, output = "\"Sunny in Ohrid\""))

        show(document(surface(weatherText())), host = host)
        rule.waitForIdle()

        rule.onNodeWithText("Sunny in Ohrid").assertExists()
        assertEquals(1, runs.size)
        assertEquals(mapOf("city" to "Ohrid"), A2UIJson.parseOrNull(runs.single()))
    }

    @Test
    fun `a tool that isn't read-only doesn't run from a binding`() {
        val runs = mutableListOf<String>()
        val host = hostWith(tool("weather", runs = runs, output = "\"Sunny in Ohrid\""))

        show(document(surface(weatherText())), host = host)
        rule.waitForIdle()

        rule.onNodeWithText("Sunny in Ohrid").assertDoesNotExist()
        assertTrue(runs.isEmpty())
    }

    @Test
    fun `a tool that isn't read-only runs from a person's tap, each tap again`() {
        val runs = mutableListOf<String>()
        val host = hostWith(tool("add_to_cart", runs = runs))
        val action = mapOf("functionCall" to mapOf("@call" to "add_to_cart", "args" to mapOf("item" to "pizza")))
        show(
            document(
                surface(
                    mapOf("id" to "root", "component" to "Button", "child" to "label", "action" to action),
                    mapOf("id" to "label", "component" to "Text", "text" to "Add"),
                ),
            ),
            host = host,
        )

        rule.onNodeWithText("Add").performClick()
        rule.onNodeWithText("Add").performClick()
        rule.waitForIdle()

        assertEquals(2, runs.size)
        assertEquals(mapOf("item" to "pizza"), A2UIJson.parseOrNull(runs.first()))
        assertTrue(sent.isEmpty())
    }

    @Test
    fun `a function no tool answers fails without a crash`() {
        show(document(surface(weatherText())), host = hostWith())
        rule.waitForIdle()

        rule.onNodeWithTag("a2ui:s").assertExists()
        assertTrue(sent.isEmpty())
    }

    @Test
    fun `a reply shows its surfaces from text with prose around the messages`() {
        val text = "Here is your plan:\n" + surface(
            mapOf("id" to "root", "component" to "Text", "text" to mapOf("@path" to "/title")),
            data = mapOf("title" to "A weekend in Ohrid"),
        ) + "\nEnjoy!"

        show(document(text))

        rule.onNodeWithText("A weekend in Ohrid").assertExists()
    }

    @Test
    fun `a reply shows the messages after one the agent broke, and reports the broken one`() {
        val errors = mutableListOf<A2UIError>()
        val renderer = A2UIRenderer(onError = { error, _ -> errors.add(error) })
        val first = surface(mapOf("id" to "root", "component" to "Text", "text" to "Day 1"), surfaceId = "a")
        val broken = """{"version": "1.0", "createSurface": {"surfaceId": "b", "components": [{"id": "root", """ +
            """"component": "Text", "text">Tip"}]}}"""
        val last = surface(mapOf("id" to "root", "component" to "Text", "text" to "Day 2"), surfaceId = "c")

        show(document("$first\n$broken\n$last"), host = AIUIHost(renderers = listOf(renderer)) {})

        rule.onNodeWithText("Day 1").assertExists()
        rule.onNodeWithText("Day 2").assertExists()
        assertEquals(listOf(A2UIError("VALIDATION_FAILED", "Message 2 of the reply isn't a JSON object.")), errors)
    }

    @Test
    fun `a reply that leaves the conversation lets go of its surfaces`() {
        val engine = renderer.makeEngine(host) as A2UIEngine
        engine.content(document(surface(mapOf("id" to "root", "component" to "Text", "text" to "Hi"))), "m1")
        assertNotNull(engine.reply("m1"))

        engine.remove("m1")

        assertNull(engine.reply("m1"))
    }

    @Test
    fun `a reply reads messages sent as JSON, in a wrapper, and with another spelling of the version`() {
        val messages = listOf(
            A2UIJson.parse(surface(mapOf("id" to "root", "component" to "Text", "text" to "From JSON"))),
        ).map { (it as Map<*, *>) + ("version" to "v1") }

        show(document(mapOf("messages" to messages)))

        rule.onNodeWithText("From JSON").assertExists()
    }

    @Test
    fun `a button sends its event as an action labelled with the button's text`() {
        show(
            document(
                surface(
                    mapOf("id" to "root", "component" to "Button", "child" to "label", "action" to bookEvent()),
                    mapOf("id" to "label", "component" to "Text", "text" to "Book Now"),
                    data = mapOf("name" to "Lake View"),
                ),
            ),
        )

        rule.onNodeWithText("Book Now").performClick()

        val action = sent.single()
        assertEquals("m1", action.messageId)
        assertEquals(AIUIFormat.A2UI, action.format)
        assertEquals("book", action.name)
        assertEquals("Book Now", action.label)
        assertEquals("s", action.payload["surfaceId"])
        assertEquals("root", action.payload["sourceComponentId"])
        assertEquals(mapOf("restaurant" to "Lake View"), action.payload["context"])
        assertFalse("userMessage" in action.payload)
        assertFalse("dataModel" in action.payload)
    }

    @Test
    fun `a button whose text is bound is labelled with the text it shows`() {
        show(
            document(
                surface(
                    mapOf("id" to "root", "component" to "Button", "child" to "label", "action" to bookEvent()),
                    mapOf("id" to "label", "component" to "Text", "text" to mapOf("@path" to "/cta")),
                    data = mapOf("name" to "Lake View", "cta" to "Reserve a table"),
                ),
            ),
        )

        rule.onNodeWithText("Reserve a table").performClick()

        assertEquals("Reserve a table", sent.single().label)
    }

    @Test
    fun `a button without text is labelled with its event's name as words`() {
        show(
            document(
                surface(
                    mapOf("id" to "root", "component" to "Button", "child" to "icon", "action" to submitEvent()),
                    mapOf("id" to "icon", "component" to "Icon", "name" to "send"),
                ),
            ),
        )

        rule.onNodeWithTag("root").performClick()

        val action = sent.single()
        assertEquals("submit_form", action.name)
        assertEquals("Submit form", action.label)
    }

    @Test
    fun `an event's name reads as words`() {
        mapOf(
            "submit_form" to "Submit form",
            "submitForm" to "Submit form",
            "book-a-table" to "Book a table",
            "SubmitForm" to "Submit form",
            "  open  menu " to "Open menu",
            "loadURL" to "Load url",
            "URLLoad" to "Urlload",
            "go" to "Go",
            "__" to "",
            "" to "",
        ).forEach { (name, words) -> assertEquals(name, words, A2UIEngine.words(name)) }
    }

    @Test
    fun `an event's user message goes in the payload, never in the label`() {
        val userMessage = mapOf("@call" to "formatString", "args" to mapOf("value" to "Book \${/name}"))
        val event = mapOf("event" to mapOf("name" to "book", "userMessage" to userMessage))
        show(
            document(
                surface(
                    mapOf("id" to "root", "component" to "Button", "child" to "label", "action" to event),
                    mapOf("id" to "label", "component" to "Text", "text" to "Go"),
                    data = mapOf("name" to "Lake View"),
                ),
            ),
        )

        rule.onNodeWithText("Go").performClick()

        val action = sent.single()
        assertEquals("Go", action.label)
        assertEquals("Book Lake View", action.payload["userMessage"])
    }

    @Test
    fun `a form whose surface sends its data model sends its values with an empty context`() {
        val submit = mapOf("event" to mapOf("name" to "submit"))
        show(
            document(
                surface(
                    mapOf("id" to "root", "component" to "Button", "child" to "label", "action" to submit),
                    mapOf("id" to "label", "component" to "Text", "text" to "Send"),
                    data = mapOf("name" to "Ada", "time" to "19:30"),
                    sendDataModel = true,
                ),
            ),
        )

        rule.onNodeWithText("Send").performClick()

        val action = sent.single()
        assertEquals(emptyMap<String, Any?>(), action.payload["context"])
        assertEquals(mapOf("name" to "Ada", "time" to "19:30"), action.payload["dataModel"])
    }

    @Test
    fun `what a person types into an obscured field stays out of the action`() {
        val submit = mapOf(
            "event" to mapOf("name" to "sign_in", "context" to mapOf("password" to mapOf("@path" to "/password"))),
        )
        show(
            document(
                surface(
                    mapOf("id" to "root", "component" to "Column", "children" to listOf("user", "password", "go")),
                    mapOf("id" to "user", "component" to "TextField", "label" to "User", "value" to mapOf("@path" to "/user")),
                    mapOf(
                        "id" to "password",
                        "component" to "TextField",
                        "label" to "Password",
                        "variant" to "obscured",
                        "value" to mapOf("@path" to "/password"),
                    ),
                    mapOf("id" to "go", "component" to "Button", "child" to "label", "action" to submit),
                    mapOf("id" to "label", "component" to "Text", "text" to "Sign in"),
                    sendDataModel = true,
                ),
            ),
        )

        rule.onNodeWithText("User").performTextInput("ada")
        rule.onNodeWithText("Password").performTextInput("hunter2")
        rule.onNodeWithText("Sign in").performClick()

        val action = sent.single()
        assertEquals(mapOf("password" to null), action.payload["context"])
        assertEquals(mapOf("user" to "ada"), action.payload["dataModel"])
    }

    // A password field and a button whose action is [action].
    private fun passwordForm(action: Map<String, Any?>, vararg more: Map<String, Any?>) = surface(
        mapOf("id" to "root", "component" to "Column", "children" to listOf("password", "go") + more.map { it["id"] }),
        mapOf(
            "id" to "password",
            "component" to "TextField",
            "label" to "Password",
            "variant" to "obscured",
            "value" to mapOf("@path" to "/password"),
        ),
        mapOf("id" to "go", "component" to "Button", "child" to "label", "action" to action),
        mapOf("id" to "label", "component" to "Text", "text" to "Go"),
        *more,
    )

    @Test
    fun `a link an action opens never carries what a person typed into an obscured field`() {
        val url = mapOf("@call" to "formatString", "args" to mapOf("value" to "https://example.com/?p=\${/password}"))
        val action = mapOf("functionCall" to mapOf("@call" to "openUrl", "args" to mapOf("url" to url)))
        show(document(passwordForm(action)))

        rule.onNodeWithText("Password").performTextInput("hunter2")
        rule.onNodeWithText("Go").performClick()

        assertEquals(listOf("https://example.com/?p="), opened)
    }

    @Test
    fun `a tool's arguments never carry what a person typed into an obscured field`() {
        val runs = mutableListOf<String>()
        val reads = mutableListOf<String>()
        val host = hostWith(
            tool("sign_in", runs = runs),
            tool("strength", readOnly = true, runs = reads, output = "\"Checked\""),
        )
        val action = mapOf(
            "functionCall" to mapOf(
                "@call" to "sign_in",
                "args" to mapOf(
                    "password" to mapOf("@path" to "/password"),
                    "hint" to mapOf("@call" to "formatString", "args" to mapOf("value" to "p=\${/password}")),
                ),
            ),
        )
        val strength = mapOf(
            "id" to "strength",
            "component" to "Text",
            "text" to mapOf("@call" to "strength", "args" to mapOf("value" to mapOf("@path" to "/password"))),
        )
        show(document(passwordForm(action, strength)), host = host)

        rule.onNodeWithText("Password").performTextInput("hunter2")
        rule.onNodeWithText("Go").performClick()
        rule.waitForIdle()

        assertEquals(mapOf("hint" to "p="), A2UIJson.parseOrNull(runs.single()))
        assertTrue(reads.isNotEmpty())
        reads.forEach { assertFalse(it, "hunter2" in it) }
    }

    @Test
    fun `the agent places the app's component, which reports its events`() {
        show(
            document(
                surface(
                    mapOf("id" to "root", "component" to "PlaceCard", "title" to mapOf("@path" to "/place")),
                    data = mapOf("place" to "St. Naum"),
                    catalogId = A2UIRenderer.DEFAULT_CATALOG_ID,
                ),
            ),
        )

        rule.onNodeWithText("Place: St. Naum").assertExists()
        rule.onNodeWithText("Ask").performClick()

        val action = sent.single()
        assertEquals("ask_about", action.name)
        assertEquals("Tell me about St. Naum", action.label)
        assertEquals("place_card", action.payload["component"])
    }

    @Test
    fun `the agent's accessibility attributes apply to the app's component`() {
        show(
            document(
                surface(
                    mapOf(
                        "id" to "root",
                        "component" to "PlaceCard",
                        "title" to "St. Naum",
                        "accessibility" to mapOf("label" to "A monastery by the lake"),
                    ),
                    catalogId = A2UIRenderer.DEFAULT_CATALOG_ID,
                ),
            ),
        )

        rule.onNodeWithContentDescription("A monastery by the lake").assertExists()
    }

    @Test
    fun `a surface made after one was deleted doesn't take over what it remembered`() {
        val counter = mapOf("id" to "root", "component" to "TapCounter")
        val first = surface(counter, catalogId = A2UIRenderer.DEFAULT_CATALOG_ID, surfaceId = "a")
        val delete = message("deleteSurface", mapOf("surfaceId" to "a"))
        val second = surface(counter, catalogId = A2UIRenderer.DEFAULT_CATALOG_ID, surfaceId = "b")
        var current by mutableStateOf(document(first, isStreaming = true))
        rule.setContent { MaterialTheme { AIUIView(host, current, "m1") } }
        rule.onNodeWithText("Taps: 0").performClick()
        rule.onNodeWithText("Taps: 1").assertExists()

        rule.runOnIdle { current = document("$first\n$delete\n$second", isStreaming = true) }

        rule.onNodeWithText("Taps: 0").assertExists()
    }

    @Test
    fun `a component named like the catalog's keeps the catalog's look`() {
        show(
            document(
                surface(
                    mapOf("id" to "root", "component" to "Card", "child" to "text"),
                    mapOf("id" to "text", "component" to "Text", "text" to "Inside the card"),
                ),
            ),
        )

        rule.onNodeWithText("Inside the card").assertExists()
        rule.onNodeWithText("App card").assertDoesNotExist()
    }

    @Test
    fun `a link opens through the host from a person's tap`() {
        val action = openUrl("https://example.com/menu")
        show(
            document(
                surface(
                    mapOf("id" to "root", "component" to "Column", "children" to listOf("web", "bad")),
                    mapOf("id" to "web", "component" to "Button", "child" to "web_label", "action" to action),
                    mapOf("id" to "web_label", "component" to "Text", "text" to "Menu"),
                    mapOf(
                        "id" to "bad",
                        "component" to "Button",
                        "child" to "bad_label",
                        "action" to openUrl("javascript:alert(1)"),
                    ),
                    mapOf("id" to "bad_label", "component" to "Text", "text" to "Bad"),
                ),
            ),
        )

        rule.onNodeWithText("Menu").performClick()
        rule.onNodeWithText("Bad").performClick()

        assertEquals(listOf("https://example.com/menu"), opened)
        assertTrue(sent.isEmpty())
    }

    private fun media(audioUrl: String) = surface(
        mapOf("id" to "root", "component" to "Column", "children" to listOf("video", "audio", "image")),
        mapOf("id" to "video", "component" to "Video", "url" to "app:clip.mp4", "posterUrl" to "app:poster.png"),
        mapOf("id" to "audio", "component" to "AudioPlayer", "url" to audioUrl),
        mapOf("id" to "image", "component" to "Image", "url" to "https://example.com/photo.png"),
    )

    @Test
    fun `media load only from web links by default`() {
        show(document(media(audioUrl = "file:///sdcard/track.mp3")))

        rule.onNodeWithContentDescription("Play video").assertIsNotEnabled()
        rule.onNodeWithContentDescription("Play").assertIsNotEnabled()
    }

    @Test
    fun `the app resolves the URLs media load from`() {
        val asked = mutableListOf<String>()
        val renderer = A2UIRenderer(
            mediaUrl = { url ->
                asked.add(url)
                url.takeIf { it.startsWith("app:") }?.let { "https://cdn.example.com/${it.removePrefix("app:")}" }
            },
        )

        show(document(media(audioUrl = "https://example.com/track.mp3")), host = AIUIHost(renderers = listOf(renderer)) {})

        rule.onNodeWithContentDescription("Play video").assertIsEnabled()
        // The resolver turned down the web link, so it doesn't load.
        rule.onNodeWithContentDescription("Play").assertIsNotEnabled()
        assertEquals(
            setOf("app:clip.mp4", "app:poster.png", "https://example.com/track.mp3", "https://example.com/photo.png"),
            asked.toSet(),
        )
    }

    @Test
    fun `a streaming reply grows, and a reply that changed starts again`() {
        val create = surface(
            mapOf("id" to "root", "component" to "Text", "text" to mapOf("@path" to "/status")),
            data = mapOf("status" to "Looking…"),
        )
        val update = message(
            "updateDataModel",
            mapOf("surfaceId" to "s", "path" to "/status", "value" to "Found 3 places"),
        )
        var current by mutableStateOf(document(create, isStreaming = true))
        rule.setContent { MaterialTheme { AIUIView(host, current, "m1") } }
        rule.onNodeWithText("Looking…").assertExists()

        rule.runOnIdle { current = document("$create\n$update", isStreaming = true) }
        rule.onNodeWithText("Found 3 places").assertExists()

        val edited = surface(mapOf("id" to "root", "component" to "Text", "text" to "Edited"))
        rule.runOnIdle { current = document(edited, isStreaming = false) }
        rule.onNodeWithText("Edited").assertExists()
        rule.onNodeWithText("Found 3 places").assertDoesNotExist()
    }

    @Test
    fun `a part the agent writes later comes in on its own, under the reply's own entrance`() {
        val create = surface(
            mapOf("id" to "root", "component" to "Column", "children" to listOf("first")),
            mapOf("id" to "first", "component" to "Text", "text" to "First"),
        )
        val update = message(
            "updateComponents",
            mapOf(
                "surfaceId" to "s",
                "components" to listOf(
                    mapOf("id" to "root", "component" to "Column", "children" to listOf("first", "second")),
                    mapOf("id" to "second", "component" to "Text", "text" to "Second"),
                ),
            ),
        )
        var current by mutableStateOf(document(create, isStreaming = true))
        rule.setContent { MaterialTheme { AIUIView(host, current, "m1") } }
        rule.mainClock.advanceTimeBy(2_000)
        rule.mainClock.autoAdvance = false

        rule.runOnUiThread {
            current = document("$create\n$update", isStreaming = true)
            Snapshot.sendApplyNotifications()
        }
        repeat(3) { rule.mainClock.advanceTimeByFrame() }
        val coming = top("second")
        rule.mainClock.advanceTimeBy(2_000)

        assertTrue("the new part rises into place", coming - top("second") > 8f)
    }

    @Test
    fun `the app's component learns when the reply finishes, with no new messages`() {
        val status = AIUIComponent(
            AIUIComponentDefinition(name = "reply_status", description = "Whether the reply is still coming"),
        ) { _, context ->
            Text(if (context.isStreaming) "Writing" else "Done")
        }
        val host = AIUIHost(renderers = listOf(A2UIRenderer()), components = listOf(status)) {}
        val create = surface(
            mapOf("id" to "root", "component" to "ReplyStatus"),
            catalogId = A2UIRenderer.DEFAULT_CATALOG_ID,
        )
        var current by mutableStateOf(document(create, isStreaming = true))
        rule.setContent { MaterialTheme { AIUIView(host, current, "m1") } }
        rule.onNodeWithText("Writing").assertExists()

        rule.runOnIdle { current = document(create, isStreaming = false) }

        rule.onNodeWithText("Done").assertExists()
    }

    // Where the top of a component is, in dp.
    private fun top(tag: String): Float =
        rule.onNodeWithTag(tag).fetchSemanticsNode().positionInRoot.y / rule.density.density

    @Test
    fun `a finished reply without a surface, or without A2UI, shows its text`() {
        val engine = renderer.makeEngine(host)

        assertNull(engine.content(document("Just text, no UI."), "m1"))
        assertNull(engine.content(document(42), "m2"))
        assertNotNull(engine.content(document("Thinking…", isStreaming = true), "m3"))
    }

    @Test
    fun `the restaurant demo's reply shows through the host`() {
        val messages = A2UIJson.list(A2UIJson.map(Fixtures.json("fixtures/restaurant-list.json"))!!["messages"])!!

        show(document(messages))

        rule.onNodeWithText("Han Dynasty").assertExists()
    }

    @Test
    fun `a reply follows its messages while it is off screen`() {
        val engine = renderer.makeEngine(host) as A2UIEngine
        val create = surface(mapOf("id" to "root", "component" to "Text", "text" to mapOf("@path" to "/status")))
        val update = message("updateDataModel", mapOf("surfaceId" to "s", "path" to "/status", "value" to "Booked"))
        engine.content(document(create, isStreaming = true), "m1")

        engine.receive(document("$create\n$update"), "m1")

        val surface = engine.reply("m1")!!.processor.surface("s")!!
        assertEquals("Booked", surface.dataModel.get(DataPath.parse("/status")))
    }

    @Test
    fun `a reply lets go of its link handler once no composition shows it`() {
        val engine = renderer.makeEngine(host) as A2UIEngine
        val content = engine.content(document(surface(mapOf("id" to "root", "component" to "Text", "text" to "Hi"))), "m1")!!
        val handler = object : UriHandler {
            override fun openUri(uri: String) = Unit
        }
        var copies by mutableStateOf(2)
        rule.setContent {
            CompositionLocalProvider(LocalUriHandler provides handler) {
                MaterialTheme { Column { repeat(copies) { content() } } }
            }
        }
        val reply = engine.reply("m1")!!

        rule.runOnIdle { copies = 1 }
        rule.runOnIdle { assertEquals(handler, reply.uriHandler) }
        rule.runOnIdle { copies = 0 }
        rule.runOnIdle { assertNull(reply.uriHandler) }
    }

    @Test
    fun `a reply that arrives finished has its whole tree checked`() {
        ShadowLog.clear()
        val engine = renderer.makeEngine(host)
        val create = surface(mapOf("id" to "root", "component" to "Column", "children" to listOf("missing")))

        engine.content(document(create), "m1")

        assertTrue(ShadowLog.getLogsForTag("StreamA2UI").any { "missing" in it.msg })
    }

    @Test
    fun `the app's error handler gets each problem with the reply's message id, instead of the log`() {
        ShadowLog.clear()
        val errors = mutableListOf<Pair<A2UIError, String>>()
        val renderer = A2UIRenderer(onError = { error, messageId -> errors.add(error to messageId) })
        val engine = renderer.makeEngine(AIUIHost(renderers = listOf(renderer)) {})
        val create = surface(mapOf("id" to "root", "component" to "Column", "children" to listOf("missing")))

        engine.content(document(listOf(A2UIJson.parse(create), "oops")), "m1")

        assertEquals(listOf("m1", "m1"), errors.map { it.second })
        val (unread, missing) = errors.map { it.first }
        assertEquals(A2UIError("VALIDATION_FAILED", "Message 2 of the reply isn't a JSON object."), unread)
        assertEquals("s", missing.surfaceId)
        assertTrue(missing.message, "missing" in missing.message)
        assertTrue(ShadowLog.getLogsForTag("StreamA2UI").isEmpty())
    }

    @Test
    fun `a message that isn't a JSON object is logged without an error handler`() {
        ShadowLog.clear()
        val engine = renderer.makeEngine(host)

        engine.content(document(listOf("oops")), "m1")

        assertTrue(ShadowLog.getLogsForTag("StreamA2UI").any { "m1" in it.msg && "VALIDATION_FAILED" in it.msg })
    }

    @Test
    fun `entries of a message list that aren't objects are kept, to be logged`() {
        val create = A2UIJson.parse(surface(mapOf("id" to "root", "component" to "Text", "text" to "Hi")))

        assertEquals(2, A2UIEngine.messages(document(listOf(create, "oops")))!!.size)
        assertEquals(2, A2UIEngine.messages(document(mapOf("messages" to listOf(create, 42))))!!.size)
    }

    @Test
    fun `an app component with a parameter named component is left out`() {
        val clash = AIUIComponent(
            AIUIComponentDefinition(
                name = "badge",
                description = "",
                parameters = listOf(AIUIParameter.required("component", AIUIParameterType.String)),
            ),
        ) { _, _ -> }

        assertNull(PortableComponents.definition(clash))
    }

    @Test
    fun `the app's component names become A2UI types`() {
        assertEquals("PlaceCard", PortableComponents.typeName("place_card"))
        assertEquals("WeatherWidget", PortableComponents.typeName("weather-widget"))
        val invalid = AIUIComponent(AIUIComponentDefinition(name = "9 lives", description = "")) { _, _ -> }
        assertNull(PortableComponents.definition(invalid))
    }

    private fun bookEvent() =
        mapOf("event" to mapOf("name" to "book", "context" to mapOf("restaurant" to mapOf("@path" to "/name"))))

    private fun submitEvent() = mapOf("event" to mapOf("name" to "submit_form"))

    private fun openUrl(url: String) =
        mapOf("functionCall" to mapOf("@call" to "openUrl", "args" to mapOf("url" to url)))
}
