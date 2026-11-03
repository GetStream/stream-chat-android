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

package io.getstream.chat.android.ai.openui.library

import io.getstream.chat.android.ai.openui.lang.Fixtures
import io.getstream.chat.android.ai.openui.lang.LibrarySchema
import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.arrayValue
import io.getstream.chat.android.ai.openui.lang.firstDifference
import io.getstream.chat.android.ai.openui.lang.get
import io.getstream.chat.android.ai.openui.lang.objectValue
import io.getstream.chat.android.ai.openui.lang.stringValue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The Kotlin library definitions are wire-compatible with upstream's React libraries: identical
 * component signatures, descriptions, groups and JSON Schema.
 */
internal class LibraryConformanceTest {

    private val pairs = listOf(
        Triple("chat", LibraryDefinition.chat, "upstream-chat-library.spec.json"),
        Triple("standard", LibraryDefinition.standard, "upstream-playground-library.spec.json"),
    )

    @TestFactory
    fun `components match`(): List<DynamicTest> = pairs.map { (name, library, file) ->
        DynamicTest.dynamicTest(name) {
            val upstream = Fixtures.json(file)
            val actual = library.specJson
            assertEquals(upstream["root"], actual["root"], "$name root")
            val upstreamComponents = upstream["components"].objectValue!!
            val actualComponents = actual["components"].objectValue!!
            assertEquals(upstreamComponents.keys.toList(), actualComponents.keys.toList(), "$name component order")
            for (key in upstreamComponents.keys) {
                assertEquals(
                    upstreamComponents[key]["signature"],
                    actualComponents[key]["signature"],
                    "$name.$key signature",
                )
                assertEquals(
                    upstreamComponents[key]["description"],
                    actualComponents[key]["description"],
                    "$name.$key description",
                )
            }
            assertNull(firstDifference(actual["componentGroups"], upstream["componentGroups"]), "$name groups")
        }
    }

    @TestFactory
    fun `default prompt options match upstream`(): List<DynamicTest> = pairs.map { (name, library, file) ->
        DynamicTest.dynamicTest(name) {
            val upstream = Fixtures.json(file)["promptOptions"]!!
            val examples = upstream["examples"].arrayValue.orEmpty().mapNotNull { it.stringValue }
            val rules = upstream["additionalRules"].arrayValue.orEmpty().mapNotNull { it.stringValue }
            assertEquals(examples, library.defaultPromptOptions.examples, "$name examples")
            if (rules.isNotEmpty()) assertEquals(rules, library.defaultPromptOptions.additionalRules, "$name rules")
        }
    }

    @TestFactory
    fun `json schema matches`(): List<DynamicTest> = pairs.map { (name, library, file) ->
        DynamicTest.dynamicTest(name) {
            val upstream = Fixtures.json(file)["schema"]!!
            val actual = library.jsonSchema
            val upstreamDefs = upstream["\$defs"].objectValue!!
            val actualDefs = actual["\$defs"].objectValue!!
            // $defs order drives the "Available components" listing in error messages.
            assertEquals(upstreamDefs.keys.toList(), actualDefs.keys.toList(), "$name \$defs order")
            for (key in upstreamDefs.keys) {
                val difference = firstDifference(actualDefs[key], upstreamDefs[key])
                assertNull(difference, "$name.$key: $difference")
                // Positional order is the properties order.
                assertEquals(
                    upstreamDefs[key]["properties"].objectValue?.keys?.toList(),
                    actualDefs[key]["properties"].objectValue?.keys?.toList(),
                    "$name.$key property order",
                )
            }
            for (key in listOf("\$schema", "type", "properties", "required", "additionalProperties")) {
                assertNull(firstDifference(actual[key], upstream[key]), "$name $key")
            }
        }
    }

    @Test
    fun `reactive props match upstream bindings`() {
        val upstream = LibrarySchema.fromSpec(Fixtures.json("upstream-chat-library.spec.json"))
        var count = 0
        for (component in LibraryDefinition.chat.components) {
            for (prop in component.props) {
                assertEquals(
                    upstream.reactiveProps.isReactiveProp(component.name, prop.name),
                    LibraryDefinition.chat.isReactiveProp(component.name, prop.name),
                    "${component.name}.${prop.name}",
                )
                if (prop.isReactive) count++
            }
        }
        assertTrue(count > 0)
    }

    @Test
    fun `param map matches the upstream spec`() {
        for ((name, library, file) in pairs) {
            val upstream = LibrarySchema.fromSpec(Fixtures.json(file))
            assertEquals(upstream.componentNames, library.schema.componentNames, name)
        }
    }

    // The customization examples from the README.

    private val tripCard = ComponentSchema(
        "TripCard",
        "A trip with its length in days",
        listOf(PropField.required("title", PropType.string), PropField.optional("days", PropType.number)),
    )
    private val trips =
        ComponentSchema(
            "Trips",
            "A list of trips",
            listOf(PropField.required("items", PropType.array(PropType.component("TripCard")))),
        )

    @Test
    fun `custom library parses and generates its prompt`() {
        val library = LibraryDefinition(root = "Trips", components = listOf(trips, tripCard))
        val result = library.schema.parser().parse(
            """
            root = Trips([lisbon, porto])
            lisbon = TripCard("Lisbon", 3)
            porto = TripCard("Porto", 2)
            """.trimIndent(),
        )
        assertTrue(result.meta.errors.isEmpty())
        assertEquals("Trips", result.root?.typeName)
        val prompt = library.systemPrompt()
        assertTrue("TripCard(title: string, days?: number) — A trip with its length in days" in prompt)
        assertTrue("Trips(items: TripCard[]) — A list of trips" in prompt)
    }

    @Test
    fun `prompt lists tools`() {
        val options = LibraryDefinition.chat.defaultPromptOptions.copy(
            tools = listOf(
                ToolDescriptor.Spec(
                    ToolSpec(
                        name = "search_hotels",
                        description = "Search hotels in a city",
                        inputSchema = obj(
                            "type" to "object",
                            "properties" to obj("city" to obj("type" to "string")),
                            "required" to listOf("city"),
                        ),
                        outputSchema = obj(
                            "type" to "object",
                            "properties" to
                                obj(
                                    "hotels" to
                                        obj(
                                            "type" to "array",
                                            "items" to
                                                obj(
                                                    "type" to "object",
                                                    "properties" to obj("name" to obj("type" to "string")),
                                                ),
                                        ),
                                ),
                        ),
                    ),
                ),
            ),
        )
        val prompt = LibraryDefinition.chat.systemPrompt(options)
        assertTrue("- search_hotels(city: string) → {hotels?: {name?: string}[]}" in prompt)
        assertTrue("## Query — Live Data Fetching" in prompt)
    }

    @Test
    fun `portable components join the content unions`() {
        val placeCard = ComponentSchema.fromPortableDefinition(
            obj(
                "name" to "place_card",
                "description" to "A photo and a short summary of a place",
                "parameters" to obj(
                    "type" to "object",
                    "properties" to
                        obj(
                            "summary" to obj("type" to "string"),
                            "title" to obj("type" to "string", "description" to "The place name"),
                        ),
                    "required" to listOf("title"),
                    "x-order" to listOf("title", "summary"),
                ),
                "events" to listOf(obj("name" to "ask_about", "description" to "The person asks about the place")),
            ),
        )
        assertEquals("PlaceCard(title: string, summary?: string)", placeCard.signature)
        assertEquals(
            "A photo and a short summary of a place. title: The place name. The person asks about the place.",
            placeCard.description,
        )
        val library = LibraryDefinition.chat.addingContent(listOf(placeCard))
        assertTrue("PlaceCard" in library.component("Card")!!.signature)
        val result = library.schema.parser().parse("root = Card([PlaceCard(\"Lisbon\")])")
        assertTrue(result.meta.errors.isEmpty(), result.meta.errors.toString())
    }

    @Suppress("unused")
    private val empty = OpenUIValue.Obj(OpenUIObject.Empty)
}
