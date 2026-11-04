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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class AIUIComponentDefinitionTest {

    private val placeCard = AIUIComponentDefinition(
        name = "place_card",
        description = "A photo and a short summary of a place.",
        parameters = listOf(
            AIUIParameter.required("title", AIUIParameterType.String, description = "The place's Wikipedia title."),
            AIUIParameter.optional("rating", AIUIParameterType.Number),
            AIUIParameter.optional("kind", AIUIParameterType.enumeration(listOf("church", "beach"))),
            AIUIParameter.optional("tags", AIUIParameterType.array(AIUIParameterType.String)),
            AIUIParameter.optional(
                "visit",
                AIUIParameterType.obj(listOf(AIUIParameter.required("day", AIUIParameterType.Date))),
            ),
        ),
        events = listOf(
            AIUIComponentDefinition.Event("ask_about", "The person asks about the place."),
            AIUIComponentDefinition.Event(
                "book",
                "The person books a visit.",
                listOf(AIUIParameter.required("time", AIUIParameterType.DateTime)),
            ),
        ),
    )

    @Test
    fun `of parameters with the same name, the first one counts`() {
        val definition = AIUIComponentDefinition(
            "rating",
            "A rating.",
            listOf(
                AIUIParameter.required("stars", AIUIParameterType.Number),
                AIUIParameter.optional("stars", AIUIParameterType.String),
            ),
        )
        val schema = definition.parametersSchema

        assertEquals(listOf("stars"), schema["x-order"])
        assertEquals(listOf("stars"), schema["required"])
        assertEquals(mapOf("stars" to mapOf("type" to "number")), schema["properties"])
    }

    @Test
    fun `parameters are a JSON Schema object that keeps their order`() {
        val schema = placeCard.parametersSchema

        assertEquals("object", schema["type"])
        assertEquals(listOf("title"), schema["required"])
        assertEquals(listOf("title", "rating", "kind", "tags", "visit"), schema["x-order"])
        assertEquals(
            mapOf("type" to "string", "description" to "The place's Wikipedia title."),
            (schema["properties"] as Map<*, *>)["title"],
        )
        assertEquals(
            mapOf("type" to "array", "items" to mapOf("type" to "string")),
            (schema["properties"] as Map<*, *>)["tags"],
        )
    }

    @Test
    fun `a definition reads back the JSON it writes`() {
        val text = placeCard.toJsonString()

        assertEquals(placeCard, AIUIComponentDefinition.fromJsonString(text))
        assertEquals(placeCard, AIUIComponentDefinition.fromJson(placeCard.toJson()))
    }

    @Test
    fun `parameters without an order follow the ones with one, by name`() {
        val definition = AIUIComponentDefinition.fromJson(
            mapOf(
                "name" to "card",
                "parameters" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "b" to mapOf("type" to "string"),
                        "a" to mapOf("type" to "string"),
                        "c" to mapOf("type" to "string"),
                    ),
                    "required" to listOf("a"),
                    "x-order" to listOf("c", "missing"),
                ),
            ),
        )!!

        assertEquals(listOf("c", "a", "b"), definition.parameters.map { it.name })
        assertEquals(listOf(false, true, false), definition.parameters.map { it.isRequired })
        assertEquals("", definition.description)
        assertEquals(emptyList<AIUIComponentDefinition.Event>(), definition.events)
    }

    @Test
    fun `a definition without a name, or text that is not one, reads as none`() {
        assertNull(AIUIComponentDefinition.fromJson(mapOf("description" to "x")))
        assertNull(AIUIComponentDefinition.fromJsonString("[1, 2]"))
        assertNull(AIUIComponentDefinition.fromJsonString("{\"name\": \"card\"} trailing"))
        assertNull(AIUIComponentDefinition.fromJsonString("not json"))
    }
}
