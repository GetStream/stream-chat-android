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

import io.getstream.chat.android.ai.openui.lang.OpenUIJson
import io.getstream.chat.android.ai.openui.lang.arrayValue
import io.getstream.chat.android.ai.openui.lang.get
import io.getstream.chat.android.ai.openui.lang.obj
import io.getstream.chat.android.ai.openui.lang.objectValue
import io.getstream.chat.android.ai.openui.lang.v
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class LibraryApiTest {
    private val chat = LibraryDefinition.chat

    // Prop types

    @Test
    fun `prop types compare by value`() {
        assertEquals(PropType.array(PropType.string), PropType.array(PropType.string))
        assertNotEquals(PropType.array(PropType.string), PropType.array(PropType.string, minItems = 1))
        assertEquals(
            PropType.union(listOf(PropType.component("A"), PropType.component("B"))),
            PropType.union(listOf(PropType.component("A"), PropType.component("B"))),
        )
        assertNotEquals(PropType.enumeration(listOf("a")), PropType.literal("a"))
        assertNotEquals(PropType.actions(listOf("OpenUrl")), PropType.action)
    }

    @Test
    fun `prop type signatures`() {
        assertEquals(
            "(A | B)[]",
            PropType.array(PropType.union(listOf(PropType.component("A"), PropType.component("B")))).signature,
        )
        assertEquals("Record<string, number>", PropType.record(PropType.number).signature)
        assertEquals(
            "{a: string, b?: number}",
            PropType.objectOf(listOf(required("a", string), optional("b", number))).signature,
        )
        assertEquals("ActionExpression", PropType.action.signature)
        assertEquals("@CopyToClipboard | @OpenUrl", PropType.actions(listOf("CopyToClipboard", "OpenUrl")).signature)
    }

    @Test
    fun `action props refer to action definitions`() {
        assertEquals(obj("\$ref" to "#/\$defs/ActionExpression"), PropType.action.jsonSchema)
        assertEquals(
            obj("anyOf" to listOf(obj("\$ref" to "#/\$defs/CopyToClipboard"), obj("\$ref" to "#/\$defs/OpenUrl"))),
            PropType.actions(listOf("CopyToClipboard", "OpenUrl")).jsonSchema,
        )
    }

    // Functions and actions

    @Test
    fun `function and action signatures`() {
        assertEquals("Upper(text: string) → string", SpecLibrary.upper.signature)
        assertEquals("CopyToClipboard(text: string)", SpecLibrary.copyToClipboard.signature)
    }

    @Test
    fun `library json holds action definitions where they are first used`() {
        val defs = SpecLibrary.library.jsonSchema["\$defs"].objectValue!!

        assertEquals(
            listOf("Stack", "Card", "Text", "Tag", "Metric", "Button", "ActionExpression", "CopyToClipboard", "OpenUrl"),
            defs.keys.take(9),
        )
        assertEquals(PropType.actionSchema, defs["ActionExpression"])
        assertEquals(PropType.openUrlSchema, defs["OpenUrl"])
        assertEquals(
            obj(
                "type" to "object",
                "properties" to obj(
                    "type" to obj("type" to "string", "const" to "CopyToClipboard"),
                    "params" to obj(
                        "type" to "object",
                        "properties" to obj("text" to obj("type" to "string")),
                        "required" to listOf("text"),
                        "additionalProperties" to false,
                    ),
                ),
                "required" to listOf("type", "params"),
                "additionalProperties" to false,
            ),
            defs["CopyToClipboard"],
        )
        assertNull(defs["ToAssistant"], "only the definitions a slot uses are listed")
    }

    @Test
    fun `library json lists functions and actions`() {
        val schema = SpecLibrary.library.jsonSchema

        assertEquals(
            obj(
                "description" to "The text in upper case",
                "params" to obj(
                    "type" to "object",
                    "properties" to obj("text" to obj("type" to "string")),
                    "required" to listOf("text"),
                    "additionalProperties" to false,
                ),
                "returns" to obj("type" to "string"),
            ),
            schema["functions"]["Upper"],
        )
        assertEquals(v("Copies text to the clipboard"), schema["actions"]["CopyToClipboard"]["description"])
        assertNull(schema["actions"]["CopyToClipboard"]["returns"])
        assertNull(chat.jsonSchema["functions"])
    }

    @Test
    fun `spec json describes functions and actions`() {
        val spec = SpecLibrary.library.specJson

        assertEquals(
            obj("Upper" to obj("signature" to "Upper(text: string) → string", "description" to "The text in upper case")),
            spec["functions"],
        )
        assertEquals(
            obj(
                "CopyToClipboard" to
                    obj("signature" to "CopyToClipboard(text: string)", "description" to "Copies text to the clipboard"),
            ),
            spec["actions"],
        )
        assertNull(spec["componentGroups"], "a library without groups leaves them out, as upstream does")
    }

    @Test
    fun `library actions and restricted slots parse`() {
        val result = SpecLibrary.library.schema.parser().parse(
            """
            root = Stack([copy, share])
            copy = Button("Copy", @CopyToClipboard("abc"))
            share = Button("Share", null, "primary", @OpenUrl("https://getstream.io"))
            """.trimIndent(),
        )

        assertTrue(result.meta.errors.isEmpty(), result.meta.errors.toString())
        assertEquals(2, result.root?.props?.get("children").arrayValue?.size)
    }

    @Test
    fun `a function or action named like a built-in or component is left out`() {
        val each = DefinedFunction("Each", "Not the built-in", emptyList()) { v(null) }
        val stack = DefinedAction("Stack", "Not the component", emptyList())
        val twice = DefinedAction("Upper", "Not the function", emptyList())
        val library = SpecLibrary.library.withCalls(
            functions = listOf(SpecLibrary.upper, each),
            customActions = listOf(SpecLibrary.copyToClipboard, stack, twice),
        )

        assertEquals(listOf("Upper"), library.functions.map { it.name })
        assertEquals(listOf("CopyToClipboard"), library.customActions.map { it.name })
    }

    // Removing

    @Test
    fun `removing strips components from slots and groups`() {
        val library = chat.removing(setOf("Table", "Col", "EditableTable"))

        assertNull(library.component("Table"))
        assertFalse(library.componentGroups.any { it.name == "Tables" })
        assertFalse("Table" in library.component("Card")!!.signature)
        assertFalse("#/\$defs/Table\"" in OpenUIJson.stringify(library.jsonSchema))
        assertFalse("### Tables" in library.systemPrompt())
    }

    @Test
    fun `removing drops the default examples and rules that use a removed component`() {
        val library = chat.removing(setOf("Table", "Col", "EditableTable"))
        val prompt = library.systemPrompt()

        assertFalse(Regex("\\b(Table|Col|EditableTable)\\(").containsMatchIn(prompt), prompt)
        assertTrue(library.defaultPromptOptions.examples.size < chat.defaultPromptOptions.examples.size)
        assertTrue(library.defaultPromptOptions.examples.isNotEmpty())
    }

    @Test
    fun `removing drops the group notes that name a removed component`() {
        val library = chat.removing(setOf("EditableTable"))
        val tables = library.componentGroups.single { it.name == "Tables" }

        assertEquals(listOf("Table", "Col"), tables.components)
        assertFalse(tables.notes.any { "EditableTable" in it }, tables.notes.toString())
        assertTrue(tables.notes.isNotEmpty())
        assertFalse("EditableTable" in library.systemPrompt())
    }

    @Test
    fun `removing a name the library lacks keeps the default prompt options`() {
        val library = chat.removing(setOf("Stack"))

        assertEquals(chat.defaultPromptOptions, library.defaultPromptOptions)
    }

    @Test
    fun `removing keeps the root and a slot's only type`() {
        val library = chat.removing(setOf("Card", "FollowUpItem", "Callout"))

        assertEquals("Card", library.root)
        assertNotNull(library.component("Card"))
        assertNotNull(library.component("FollowUpItem"), "FollowUpBlock takes only FollowUpItem")
        assertNull(library.component("Callout"))
    }

    // Adding

    @Test
    fun `adding keeps the library's own components and skips uncallable names`() {
        val replacement = ComponentSchema("Card", "Not the chat card", listOf(required("x", string)))
        val lowercase = ComponentSchema("placeCard", "", emptyList())
        val builtin = ComponentSchema("Each", "", emptyList())
        val valid = ComponentSchema("PlaceCard", "A place", listOf(required("title", string)))
        val candidates = listOf(replacement, lowercase, builtin, valid)

        val added = chat.adding(candidates)
        val content = chat.addingContent(candidates)

        for (library in listOf(added, content)) {
            assertEquals(chat.component("Card")?.description, library.component("Card")?.description)
            assertFalse(library.component("Card")!!.props.any { it.name == "x" })
            assertNull(library.component("placeCard"))
            assertNull(library.component("Each"))
            assertNotNull(library.component("PlaceCard"))
        }
        assertEquals(listOf("PlaceCard"), content.componentGroups.last().components)
        assertTrue("PlaceCard" in content.component("Card")!!.signature)
    }

    // Extending

    @Test
    fun `extending rejects component names programs can't call`() {
        val library = SpecLibrary.library
        listOf("placeCard", "Each", "Query", "Upper", "CopyToClipboard").forEach { name ->
            assertFailsWith<IllegalArgumentException>(name) {
                library.extended(
                    add = listOf(ComponentSchema(name, "", emptyList()) to null),
                    override = emptyList(),
                    remove = emptyList(),
                    functions = emptyList(),
                    actions = emptyList(),
                )
            }
        }
    }

    @Test
    fun `extending drops union options that only hold a removed component`() {
        val slot = PropType.union(listOf(PropType.array(PropType.component("A")), PropType.component("B")))
        val library = LibraryDefinition(
            root = "Root",
            components = listOf(
                ComponentSchema("Root", "", listOf(PropField.required("children", slot))),
                ComponentSchema("A", "", emptyList()),
                ComponentSchema("B", "", emptyList()),
            ),
        )

        val extended = library.extended(emptyList(), emptyList(), listOf("A"), emptyList(), emptyList())

        assertNull(extended.component("A"))
        assertEquals(
            PropType.union(listOf(PropType.component("B"))),
            extended.component("Root")!!.props.single().type,
        )
    }

    @Test
    fun `adding content with nothing addable leaves the library`() {
        assertEquals(chat.componentGroups.size, chat.addingContent(emptyList()).componentGroups.size)
    }

    // Prompt

    @Test
    fun `custom functions and actions get their own sections without built-ins`() {
        val library = LibraryDefinition(
            root = "Box",
            components = listOf(ComponentSchema("Box", "A box", listOf(required("text", string)))),
            functions = listOf(SpecLibrary.upper),
            customActions = listOf(SpecLibrary.copyToClipboard),
        )

        val prompt = library.systemPrompt()

        assertTrue("## Built-in Functions\n\n@Upper(text: string) → string — The text in upper case\n" in prompt)
        assertTrue(
            "## Action — Button Behavior\n\nPass one step, or Action([...]) with several steps, as a button's " +
                "action.\n\nAvailable steps:\n- @CopyToClipboard(text: string) — Copies text to the clipboard" in prompt,
        )
        assertFalse("@Count(" in prompt)
    }

    @Test
    fun `restricted slots are explained in the prompt`() {
        val prompt = SpecLibrary.library.systemPrompt()

        assertTrue("Props typed like `@OpenUrl | @ToAssistant` accept only the steps listed." in prompt)
        assertTrue("available steps (@ToAssistant, @OpenUrl, @CopyToClipboard)" in prompt)
    }

    // Portable components

    @Test
    fun `json schema types read as prop types`() {
        fun type(vararg pairs: Pair<String, Any?>) = PropType.fromJsonSchema(obj(*pairs))
        assertEquals(PropType.string, type("type" to listOf("string", "null")))
        assertEquals(PropType.literal("a"), type("const" to "a"))
        assertEquals(PropType.enumeration(listOf("a", "b")), type("enum" to listOf("a", "b")))
        assertEquals(PropType.any, type("enum" to listOf(1, 2)))
        assertEquals(PropType.number, type("anyOf" to listOf(obj("type" to "number"), obj("type" to "null"))))
        assertEquals(
            PropType.union(listOf(PropType.number, PropType.string)),
            type("oneOf" to listOf(obj("type" to "number"), obj("type" to "string"))),
        )
        assertEquals(PropType.record(PropType.number), type("type" to "object", "additionalProperties" to obj("type" to "number")))
        assertEquals(PropType.any, type("type" to "object"))
        assertEquals("PlaceCard", ComponentSchema.typeName("place_card"))
    }
}
