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

package io.getstream.chat.android.ai.compose.parts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class AIClientToolDefinitionTest {

    private val schema = mapOf(
        "type" to "object",
        "properties" to mapOf("day" to mapOf("type" to "string")),
        "required" to listOf("day"),
    )

    @Test
    fun `a tool that only names itself describes nothing and is not read-only`() {
        val tool = object : AIClientTool {
            override val name = "calendar"
            override suspend fun run(call: AIToolCallPart) = AIClientToolResult.completed("{}")
        }

        assertEquals("", tool.description)
        assertNull(tool.inputSchema)
        assertEquals(false, tool.isReadOnly)
        assertEquals(AIClientToolDefinition("calendar"), tool.definition)
        assertEquals(
            mapOf("name" to "calendar", "annotations" to mapOf("readOnlyHint" to false)),
            tool.definition.toJson(),
        )
    }

    @Test
    fun `a tool's definition has its name, description, schema and read-only hint`() {
        val tool = object : AIClientTool {
            override val name = "calendar"
            override val description = "The person's events on a day."
            override val inputSchema = schema
            override val isReadOnly = true
            override suspend fun run(call: AIToolCallPart) = AIClientToolResult.completed("{}")
        }

        assertEquals(
            mapOf(
                "name" to "calendar",
                "description" to "The person's events on a day.",
                "inputSchema" to schema,
                "annotations" to mapOf("readOnlyHint" to true),
            ),
            tool.definition.toJson(),
        )
    }

    @Test
    fun `a tool's definition always follows its properties`() {
        val tool = object : AIClientTool {
            override var name = "calendar"
            override var description = ""
            override var inputSchema: Map<String, Any?>? = null
            override var isReadOnly = false
            override suspend fun run(call: AIToolCallPart) = AIClientToolResult.completed("{}")
        }

        tool.name = "agenda"
        tool.description = "The person's events."
        tool.inputSchema = schema
        tool.isReadOnly = true

        assertEquals(AIClientToolDefinition("agenda", "The person's events.", schema, isReadOnly = true), tool.definition)
    }

    @Test
    fun `a definition reads back what it writes, and a tool described for MCP`() {
        val definition = AIClientToolDefinition("calendar", "Events.", schema, isReadOnly = true)

        assertEquals(definition, AIClientToolDefinition.fromJsonString(definition.toJsonString()))
        assertEquals(
            AIClientToolDefinition("search", "Finds things."),
            AIClientToolDefinition.fromJsonString("""{"name":"search","description":"Finds things.","title":"Search"}"""),
        )
        assertNull(AIClientToolDefinition.fromJson(mapOf("description" to "No name")))
        assertNull(AIClientToolDefinition.fromJsonString("not json"))
    }
}
