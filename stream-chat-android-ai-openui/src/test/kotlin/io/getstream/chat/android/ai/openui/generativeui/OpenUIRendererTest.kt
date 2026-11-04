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
import io.getstream.chat.android.ai.openui.library.openuiChatPromptOptions
import io.getstream.chat.android.ai.openui.library.openuiPromptOptions
import io.getstream.chat.android.ai.openui.rendering.openuiChatLibrary
import io.getstream.chat.android.ai.openui.rendering.openuiLibrary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class OpenUIRendererTest {

    private val program = "root = Card([TextContent(\"Ohrid\")])"
    private val placeCard = component("place_card", "A photo and a short summary of a place.")

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the host shows OpenUI replies with the OpenUI renderer`() {
        val host = AIUIHost(renderers = listOf(OpenUIRenderer())) {}
        val reply = AIUIDocument(AIUIFormat.OpenUI, content = program)

        assertTrue(host.canShow(reply))
        assertTrue(host.showsUI(reply, "m1"))
        assertFalse(host.canShow(AIUIDocument(AIUIFormat.A2UI, content = program)))
    }

    @Test
    fun `a reply whose UI isn't a program shows its text`() {
        val host = AIUIHost(renderers = listOf(OpenUIRenderer())) {}
        val reply = AIUIDocument(AIUIFormat.OpenUI, content = mapOf("root" to "Card"))

        assertTrue(host.canShow(reply))
        assertFalse(host.showsUI(reply, "m1"))
    }

    @Test
    fun `the capabilities give the fence and the instructions with the app's components`() {
        val renderer = OpenUIRenderer()
        val capabilities = renderer.capabilities(listOf(placeCard))
        val instructions = capabilities["instructions"] as String

        assertEquals("openui-lang", capabilities["fence"])
        assertTrue("PlaceCard(" in instructions)
        assertTrue("A photo and a short summary of a place." in instructions)
        assertEquals(openuiChatLibrary.prompt(openuiChatPromptOptions), renderer.capabilities(emptyList())["instructions"])
    }

    @Test
    fun `the instructions list the host's tools with Query and Mutation`() {
        val tool = object : AIClientTool {
            override val name = "get_weather"
            override suspend fun run(call: AIToolCallPart) = AIClientToolResult.completed("{}")
        }

        val withTools = OpenUIRenderer().capabilities(emptyList(), listOf(tool))["instructions"] as String
        val withoutTools = OpenUIRenderer().capabilities(emptyList(), emptyList())["instructions"] as String

        assertTrue("- get_weather" in withTools)
        assertTrue("## Query — Live Data Fetching" in withTools)
        assertTrue("## Mutation — Write Operations" in withTools)
        assertFalse("get_weather" in withoutTools)
        assertFalse("## Available Tools" in withoutTools)
    }

    @Test
    fun `the app's instructions come before OpenUI's own`() {
        val instructions = OpenUIRenderer(instructions = "Answer travel questions with UI.")
            .capabilities(emptyList(), emptyList())["instructions"] as String

        assertTrue(instructions.startsWith("Answer travel questions with UI.\n\n"))
        assertTrue(instructions.endsWith(openuiChatLibrary.prompt(openuiChatPromptOptions)))
    }

    @Test
    fun `the prompt is made with the library's own options by default`() {
        val standard = OpenUIRenderer(library = openuiLibrary)

        assertEquals(openuiPromptOptions, standard.promptOptions)
        assertEquals(openuiChatPromptOptions, OpenUIRenderer().promptOptions)
        assertEquals(openuiLibrary.prompt(openuiPromptOptions), standard.capabilities(emptyList())["instructions"])
        assertEquals(openuiLibrary.prompt(openuiPromptOptions), openuiLibrary.prompt())
    }

    @Test
    fun `the host's capabilities name the OpenUI format`() {
        val host = AIUIHost(renderers = listOf(OpenUIRenderer()), components = listOf(placeCard)) {}
        val renderers = host.capabilities["renderers"] as List<*>
        val openUI = renderers.single() as Map<*, *>

        assertEquals("openui", openUI["format"])
        assertEquals("openui-lang", openUI["fence"])
    }

    @Test
    fun `an app component never replaces one of OpenUI's own or another app component`() {
        val card = component("card", "Not OpenUI's card.")
        val again = component("place-card", "The same OpenUI name.")
        val uncallable = component("_", "No name a program can call.")

        val placeable = OpenUIRenderer.placeable(listOf(card, placeCard, again, uncallable), openuiChatLibrary.definition)

        assertEquals(listOf("PlaceCard"), placeable.map { it.second.name })
        val instructions = OpenUIRenderer().capabilities(listOf(card))["instructions"] as String
        assertFalse("Not OpenUI's card." in instructions)
    }

    @Test
    fun `an app component's parameters are its OpenUI props, in order`() {
        val schema = OpenUIRenderer.schema(placeCard)

        assertNotNull(schema)
        assertEquals("PlaceCard", schema.name)
        assertEquals(listOf("title", "rating"), schema.props.map { it.name })
        assertNull(OpenUIRenderer.schema(component("", "No name.")))
    }

    private fun component(name: String, description: String): AIUIComponent = AIUIComponent(
        AIUIComponentDefinition(
            name = name,
            description = description,
            parameters = listOf(
                AIUIParameter.required("title", AIUIParameterType.String),
                AIUIParameter.optional("rating", AIUIParameterType.Number),
            ),
        ),
    ) { _, _ -> }
}
