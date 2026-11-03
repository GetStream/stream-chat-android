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

package io.getstream.chat.android.ai.openui.rendering

import io.getstream.chat.android.ai.openui.library.PropField
import io.getstream.chat.android.ai.openui.library.PropType
import io.getstream.chat.android.ai.openui.library.defineAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** The components each built-in library renders; data-only ones (items, series) are read by their parent. */
internal class LibraryTest {

    private val chatRendered = setOf(
        "Accordion", "BoldText", "Button", "Buttons", "Callout", "Card", "CardHeader", "Carousel", "CheckBoxGroup",
        "Chips", "CodeBlock", "CompositeCardBlock", "ContextCardBlock", "DatePicker", "EditableTable", "EntityList",
        "FollowUpBlock", "Form", "FormControl", "Icon", "IconButton", "IconText", "Image", "ImageBlock",
        "ImageGallery", "ImageText", "ImageTextLarge", "InlineHeader", "Input", "Label", "ListBlock",
        "MarkDownRenderer", "MetricIndicatorInline", "MetricIndicatorWithStrikethrough", "OptionCards",
        "OverviewCardBlock", "RadioGroup", "SectionBlock", "Select", "Separator", "Slider", "SnippetCardBlock",
        "Steps", "SwitchGroup", "Table", "Tabs", "Tag", "TagBlock", "Text", "TextArea", "TextCallout", "TextContent",
        "VisualCardBlock",
    )
    private val standardRendered = setOf(
        "Accordion", "BoldText", "Button", "Buttons", "Callout", "Card", "CardHeader", "Carousel", "CheckBoxGroup",
        "Chips", "CodeBlock", "CompositeCardBlock", "ContextCardBlock", "DatePicker", "EditableTable", "EntityList",
        "Form", "FormControl", "Icon", "IconButton", "IconText", "Image", "ImageBlock", "ImageGallery", "ImageText",
        "ImageTextLarge", "InlineHeader", "Input", "Label", "ListBlock", "MarkDownRenderer", "MetricIndicatorInline",
        "MetricIndicatorWithStrikethrough", "Modal", "OptionCards", "OverviewCardBlock", "RadioGroup", "Select",
        "Separator", "Slider", "SnippetCardBlock", "Stack", "Steps", "SwitchGroup", "Table", "Tabs", "Tag", "TagBlock",
        "Text", "TextArea", "TextCallout", "TextContent", "VisualCardBlock",
    )

    private fun rendered(library: Library) =
        library.components.values.filter { it.component != null }.map { it.name }.toSet()

    private val placeCard = defineComponent(
        name = "PlaceCard",
        props = listOf(PropField.required("name", PropType.string)),
        description = "A place.",
    ) { }

    @Test
    fun `the chat library renders its UI components`() {
        assertEquals(chatRendered, rendered(openuiChatLibrary))
    }

    @Test
    fun `the standard library renders its UI components`() {
        assertEquals(standardRendered, rendered(openuiLibrary))
    }

    @Test
    fun `a created library lists its components, root and calls`() {
        val copy = defineAction("CopyToClipboard", "Copies text", listOf(PropField.required("text", PropType.string)))
        val library = createLibrary(components = listOf(placeCard), root = "PlaceCard", actions = listOf(copy))
        assertEquals(listOf("PlaceCard"), library.components.keys.toList())
        assertEquals("PlaceCard", library.root)
        assertSame(copy, library.actions["CopyToClipboard"])
        assertTrue("PlaceCard(name: string)" in library.prompt())
        assertEquals("PlaceCard", library.toSpec().components.single().name)
        assertFailsWith<IllegalArgumentException> { createLibrary(components = listOf(placeCard), root = "Card") }
    }

    @Test
    fun `replacing a renderer keeps the schema and prompt`() {
        val library = openuiChatLibrary.replacingRenderer("Callout") { }
        assertEquals(openuiChatLibrary.component("Callout")?.schema, library.component("Callout")?.schema)
        assertEquals(openuiChatLibrary.systemPrompt(), library.systemPrompt())
        assertTrue(library.renderer("Callout") !== openuiChatLibrary.renderer("Callout"))
    }

    @Test
    fun `replacing a view keeps the schema and prompt`() {
        val library = openuiChatLibrary.replacingView("Callout") { props -> props.DefaultView() }
        assertEquals(openuiChatLibrary.componentNames, library.componentNames)
        assertEquals(openuiChatLibrary.component("Callout")?.schema, library.component("Callout")?.schema)
        assertEquals(openuiChatLibrary.systemPrompt(), library.systemPrompt())
        assertTrue(library.renderer("Callout") !== openuiChatLibrary.renderer("Callout"))
    }

    @Test
    fun `replacing the view of a component without one keeps the library`() {
        assertSame(openuiChatLibrary, openuiChatLibrary.replacingView("Unknown") { })
        assertSame(openuiChatLibrary, openuiChatLibrary.replacingView("Col") { })
    }

    @Test
    fun `adding a component of a name the library has keeps its renderer`() {
        val callout = openuiChatLibrary.component("Callout")!!
        val library = openuiChatLibrary.adding(listOf(DefinedComponent(callout.schema) { }))
        assertSame(openuiChatLibrary.renderer("Callout"), library.renderer("Callout"))
    }

    @Test
    fun `an added content component renders and joins the prompt`() {
        val library = openuiChatLibrary.addingContent(listOf(placeCard))
        assertSame(placeCard.component, library.renderer("PlaceCard"))
        assertTrue("PlaceCard" in library.systemPrompt())
    }

    @Test
    fun `removing components keeps the renderers of the ones that stay`() {
        val library = openuiChatLibrary.removing(setOf("Card", "Callout"))
        assertNull(library.component("Callout"))
        assertNull(library.renderer("Callout"))
        assertNotNull(library.renderer("Card"))
    }

    @Test
    fun `extending adds a component to the slots it names`() {
        val library = openuiChatLibrary.extend(LibraryExtension(add = listOf(SlottedComponent(placeCard, listOf("Card")))))
        assertSame(placeCard.component, library.renderer("PlaceCard"))
        assertTrue("PlaceCard" in library.components.getValue("Card").props.single { it.name == "children" }.type.signature)
        assertTrue("PlaceCard" !in openuiChatLibrary.components.getValue("Card").props.first().type.signature)
    }

    @Test
    fun `extending with a plain component only lists it`() {
        val library = openuiChatLibrary.extend(LibraryExtension(add = listOf(placeCard)))
        assertNotNull(library.components["PlaceCard"])
        assertTrue("PlaceCard" !in library.components.getValue("Card").props.single { it.name == "children" }.type.signature)
    }

    @Test
    fun `extending overrides and removes components`() {
        val callout = defineComponent("Callout", emptyList(), "A plain callout.") { }
        val library = openuiChatLibrary.extend(LibraryExtension(override = listOf(callout), remove = listOf("Steps")))
        assertSame(callout.component, library.renderer("Callout"))
        assertEquals("A plain callout.", library.components.getValue("Callout").description)
        assertNull(library.components["Steps"])
        assertTrue(library.componentGroups.none { "Steps" in it.components })
    }

    @Test
    fun `extending fails like upstream on invalid changes`() {
        val card = openuiChatLibrary.components.getValue("Callout")
        assertFailsWith<IllegalArgumentException> { openuiChatLibrary.extend(LibraryExtension(add = listOf(card))) }
        assertFailsWith<IllegalArgumentException> {
            openuiChatLibrary.extend(LibraryExtension(override = listOf(placeCard)))
        }
        assertFailsWith<IllegalArgumentException> { openuiChatLibrary.extend(LibraryExtension(remove = listOf("Card"))) }
        assertFailsWith<IllegalArgumentException> {
            openuiChatLibrary.extend(LibraryExtension(remove = listOf("Missing")))
        }
        assertFailsWith<IllegalArgumentException> {
            openuiChatLibrary.extend(LibraryExtension(add = listOf(SlottedComponent(placeCard, listOf("Missing")))))
        }
    }
}
