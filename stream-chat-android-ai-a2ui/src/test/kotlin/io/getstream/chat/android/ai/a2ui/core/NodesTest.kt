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

package io.getstream.chat.android.ai.a2ui.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Builds trees from backend payloads (the restaurant demo's, upgraded to 1.0) and from small surfaces. */
internal class NodesTest {

    private val list = surfaceOf("fixtures/restaurant-list.json")
    private val form = surfaceOf("fixtures/booking-form.json")

    @Test
    fun `the backend payloads process without errors`() {
        for (file in listOf("restaurant-list", "booking-form", "booking-confirmation")) {
            val harness = Harness(listOf(BasicCatalog.catalog))
            harness.process(A2UIJson.list(A2UIJson.map(Fixtures.json("fixtures/$file.json"))!!["messages"])!!)
            harness.processor.validateTopology()
            assertEquals(file, emptyList<RendererError>(), harness.errors)
        }
    }

    @Test
    fun `resolves absolute bindings and static children`() {
        val root = list.context(list.rootNode()!!)

        assertEquals(listOf("title_heading", "restaurant_list"), root.children().map { it.component.id })
        assertEquals("## Top 3 restaurants in New York", list.context(root.children().first()).string("text"))
    }

    @Test
    fun `a template child list repeats the component for every item with its own scope`() {
        val cards = list.context(list.find("restaurant_list")!!).children()

        assertEquals(listOf("/items/0", "/items/1", "/items/2"), cards.map { it.context.scope.toString() })
        assertEquals(listOf(0, 1, 2), cards.map { it.context.itemIndex })
        assertTrue(cards.all { it.component.id == "restaurant_card" })
        assertEquals(cards.size, cards.map { it.key }.toSet().size)
    }

    @Test
    fun `relative bindings inside a template resolve against the list item`() {
        val names = list.findAll("restaurant_name").map { list.context(it).string("text") }

        assertEquals((0..2).map { "#### " + list.dataModel.get(DataPath.parse("/items/$it/name")) }, names)
    }

    @Test
    fun `an action context resolves against the item of the pressed button`() {
        val sent = mutableListOf<RendererMessage>()
        val surface = surfaceOf("fixtures/restaurant-list.json", sent)

        surface.context(surface.findAll("restaurant_book_button")[1]).performAction()

        val action = sent.single() as RendererMessage.Action
        assertEquals("book_restaurant", action.name)
        assertEquals("restaurant_book_button", action.sourceComponentId)
        assertEquals(
            listOf("name", "address", "imageUrl").map { surface.dataModel.get(DataPath.parse("/items/1/$it")) },
            listOf("restaurantName", "address", "imageUrl").map { action.context[it] },
        )
    }

    @Test
    fun `an input exposes the absolute path of its two-way binding`() {
        val partySize = form.context(form.find("booking_party")!!)

        assertEquals("/partySize", partySize.boundPath("value").toString())
        assertEquals("2", partySize.string("value"))
        assertNull(partySize.boundPath("label"))
        assertEquals("Party size", partySize.string("label"))
    }

    @Test
    fun `an action context reads the values the person entered`() {
        val sent = mutableListOf<RendererMessage>()
        val surface = surfaceOf("fixtures/booking-form.json", sent)
        surface.context(surface.find("booking_party")!!).write("value", "6")
        surface.context(surface.find("booking_dietary")!!).write("value", "Vegan")

        surface.context(surface.find("booking_submit")!!).performAction()

        val action = sent.single() as RendererMessage.Action
        assertEquals("submit_booking", action.name)
        assertEquals("6", action.context["partySize"])
        assertEquals("Vegan", action.context["dietary"])
        assertEquals(surface.dataModel.get(DataPath.parse("/reservationTime")), action.context["reservationTime"])
    }

    @Test
    fun `numbers are shown without a trailing zero when they are whole`() {
        val surface = surface(
            listOf(mapOf("id" to "root", "component" to "Text", "text" to mapOf("@path" to "/n"))),
            data = mapOf("n" to 4.0),
        )

        assertEquals("4", surface.context(surface.rootNode()!!).string("text"))
        assertEquals("2.5", A2UIJson.stringify(2.5))
    }

    @Test
    fun `missing children are skipped`() {
        val surface = surface(
            listOf(
                mapOf("id" to "root", "component" to "Column", "children" to listOf("missing", "text")),
                mapOf("id" to "text", "component" to "Text", "text" to "Hi"),
            ),
        )

        assertEquals(listOf("text"), surface.context(surface.rootNode()!!).children().map { it.component.id })
    }

    @Test
    fun `a component that contains itself is not repeated`() {
        val surface = surface(
            listOf(
                mapOf("id" to "root", "component" to "Column", "children" to listOf("loop")),
                mapOf("id" to "loop", "component" to "Card", "child" to "loop"),
            ),
        )

        assertEquals(listOf("root", "loop"), surface.allNodes().map { it.component.id })
    }

    @Test
    fun `a cycle with many children stops at once`() {
        val surface = surface(
            listOf(mapOf("id" to "root", "component" to "Column", "children" to listOf("root", "root"))),
        )

        assertEquals(1, surface.allNodes().size)
    }

    @Test
    fun `a template can repeat itself for nested data`() {
        // A comment shows its text and then its replies, with the same component.
        val surface = surface(
            listOf(
                mapOf(
                    "id" to "root",
                    "component" to "List",
                    "children" to mapOf("componentId" to "comment", "path" to "/comments"),
                ),
                mapOf("id" to "comment", "component" to "Column", "children" to listOf("comment-text", "replies")),
                mapOf("id" to "comment-text", "component" to "Text", "text" to mapOf("@path" to "text")),
                mapOf(
                    "id" to "replies",
                    "component" to "List",
                    "children" to mapOf("componentId" to "comment", "path" to "replies"),
                ),
            ),
            data = mapOf(
                "comments" to listOf(
                    mapOf(
                        "text" to "First",
                        "replies" to listOf(mapOf("text" to "Reply", "replies" to emptyList<Any>())),
                    ),
                ),
            ),
        )

        val texts = surface.allNodes()
            .filter { it.component.id == "comment-text" }
            .map { surface.context(it).string("text") }
        assertEquals(listOf("First", "Reply"), texts)
    }

    @Test
    fun `function calls resolve, with the index of the item`() {
        val surface = surface(
            listOf(
                mapOf(
                    "id" to "root",
                    "component" to "List",
                    "children" to mapOf("componentId" to "row", "path" to "/items"),
                ),
                mapOf(
                    "id" to "row",
                    "component" to "Text",
                    "text" to mapOf(
                        "@call" to "formatString",
                        "args" to mapOf(
                            "value" to
                                "\${@index(offset: 1)}. \${name} costs " +
                                "\${formatCurrency(value: price, currency: 'EUR')}",
                        ),
                    ),
                ),
            ),
            data = mapOf("items" to listOf(mapOf("name" to "Tea", "price" to 3.5))),
        )

        val row = surface.context(surface.rootNode()!!).children().single()
        assertEquals("1. Tea costs €3.50", surface.context(row).string("text"))
    }

    @Test
    fun `a failing check passes once the person fills the field in`() {
        val surface = surface(
            listOf(
                mapOf(
                    "id" to "root",
                    "component" to "TextField",
                    "label" to "Email",
                    "value" to mapOf("@path" to "/email"),
                    "checks" to listOf(
                        mapOf(
                            "condition" to mapOf(
                                "@call" to "required",
                                "args" to mapOf("value" to mapOf("@path" to "/email")),
                            ),
                        ),
                    ),
                ),
            ),
        )
        val field = surface.context(surface.rootNode()!!)

        assertEquals(listOf("This field is required."), field.failingChecks().map { it.message })
        field.write("value", "ada@example.com")
        assertTrue(field.failingChecks().isEmpty())
    }

    private fun Surface.context(node: Node) = NodeContext(this, node)

    private fun Surface.allNodes(node: Node = rootNode()!!): List<Node> {
        val context = context(node)
        return listOf(node) + (listOfNotNull(context.child()) + context.children()).flatMap { allNodes(it) }
    }

    private fun Surface.find(id: String): Node? = allNodes().firstOrNull { it.component.id == id }

    private fun Surface.findAll(id: String): List<Node> = allNodes().filter { it.component.id == id }

    private fun surfaceOf(path: String, sent: MutableList<RendererMessage> = mutableListOf()): Surface {
        val processor = MessageProcessor(listOf(BasicCatalog.catalog), services = Harness.testServices()) {
            sent.add(it)
        }
        A2UIJson.list(A2UIJson.map(Fixtures.json(path))!!["messages"])!!
            .forEach { processor.handle(A2UIJson.map(it)!!) }
        return processor.surfaces.values.single()
    }

    private fun surface(components: List<Map<String, Any?>>, data: Map<String, Any?> = emptyMap()): Surface {
        val processor = MessageProcessor(listOf(BasicCatalog.catalog), services = Harness.testServices()) { }
        processor.handle(
            mapOf(
                "version" to "1.0",
                "createSurface" to mapOf(
                    "surfaceId" to "s",
                    "catalogId" to A2UIProtocol.BASIC_CATALOG_ID,
                    "components" to components,
                    "dataModel" to data,
                ),
            ),
        )
        return processor.surface("s")!!
    }
}
