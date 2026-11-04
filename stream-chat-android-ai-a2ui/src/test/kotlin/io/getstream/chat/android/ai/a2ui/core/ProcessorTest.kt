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

/** The message processor's own behaviour, beyond the conformance suite. */
internal class ProcessorTest {

    private val sent = mutableListOf<RendererMessage>()
    private val scheduled = mutableListOf<() -> Unit>()
    private val processor = MessageProcessor(
        catalogs = listOf(BasicCatalog.catalog),
        defaultCatalogId = A2UIProtocol.BASIC_CATALOG_ID,
        services = Harness.testServices(),
        schedule = { _, block -> scheduled.add(block) },
    ) { sent.add(it) }

    private val errors get() = sent.filterIsInstance<RendererMessage.Error>().map { it.error }

    private fun handle(vararg messages: Map<String, Any?>) =
        messages.forEach { processor.handle(it + ("version" to "1.0")) }

    private fun createSurface(
        components: List<Any?> = emptyList(),
        data: Map<String, Any?> = emptyMap(),
        sendDataModel: Boolean = false,
    ) =
        handle(
            mapOf(
                "createSurface" to mapOf(
                    "surfaceId" to "s",
                    "components" to components,
                    "dataModel" to data,
                    "sendDataModel" to sendDataModel,
                ),
            ),
        )

    private fun text(id: String = "root", text: Any?) = mapOf("id" to id, "component" to "Text", "text" to text)

    @Test
    fun `a renderer default catalog satisfies surfaces without one`() {
        createSurface(listOf(text(text = "Hello")))

        assertEquals(emptyList<RendererError>(), errors)
        assertEquals(
            "Hello",
            NodeContext(processor.surface("s")!!, processor.surface("s")!!.rootNode()!!).string("text"),
        )
    }

    @Test
    fun `updateComponents merges into an existing surface`() {
        createSurface(listOf(text(text = "Hello")))
        handle(
            mapOf(
                "updateComponents" to mapOf(
                    "surfaceId" to "s",
                    "components" to listOf(text(text = "Bye"), text("more", "More")),
                ),
            ),
        )

        val surface = processor.surface("s")!!
        assertEquals(listOf("root", "more"), surface.components.keys.toList())
        assertEquals("Bye", surface.root!!.properties["text"])
    }

    @Test
    fun `messages for an unknown surface are reported to the agent`() {
        handle(mapOf("updateDataModel" to mapOf("surfaceId" to "nope", "path" to "/a", "value" to 1)))

        assertEquals(RendererError.INTEGRITY_ERROR, errors.single().code)
        assertEquals("nope", errors.single().surfaceId)
    }

    @Test
    fun `an unsupported catalog is reported when the surface is created`() {
        handle(mapOf("createSurface" to mapOf("surfaceId" to "s", "catalogId" to "https://example.com/other.json")))

        assertEquals(RendererError.CATALOG_NOT_FOUND, errors.single().code)
    }

    @Test
    fun `only surfaces that opted in are synchronized to the agent`() {
        createSurface(data = mapOf("a" to 1), sendDataModel = true)
        handle(mapOf("createSurface" to mapOf("surfaceId" to "quiet", "dataModel" to mapOf("b" to 2))))

        assertEquals(mapOf("s" to mapOf("a" to 1L)), processor.synchronizedDataModel())
    }

    @Test
    fun `identical errors are reported only once`() {
        repeat(3) {
            handle(
                mapOf("deleteSurface" to mapOf("surfaceId" to "s"), "updateComponents" to emptyMap<String, Any?>()),
            )
        }

        assertEquals(1, errors.size)
    }

    @Test
    fun `a message that fails to decode is reported with its surface`() {
        processor.handle(mapOf("version" to "1.0", "updateComponents" to mapOf("surfaceId" to "s")))

        assertEquals(RendererError.VALIDATION_FAILED, errors.single().code)
        assertEquals("s", errors.single().surfaceId)
        assertEquals("/components", errors.single().path)
    }

    @Test
    fun `a malformed renderer call is answered with INVALID_FUNCTION_CALL`() {
        processor.handle(
            mapOf(
                "version" to "1.0",
                "callRendererFunction" to mapOf(
                    "functionCallId" to "c1",
                    "callFunction" to mapOf("@call" to "x"),
                ),
            ),
        )

        val error = errors.single()
        assertEquals(RendererError.INVALID_FUNCTION_CALL, error.code)
        assertEquals("c1", error.functionCallId)
        assertNull(error.surfaceId)
    }

    @Test
    fun `malformed renderer calls are each answered, even for the same reason`() {
        listOf("a", "b").forEach { id ->
            handle(mapOf("callRendererFunction" to mapOf("functionCallId" to id, "callFunction" to mapOf("@call" to "x"))))
        }

        assertEquals(listOf("a", "b"), errors.map { it.functionCallId })
        assertTrue(errors.all { it.code == RendererError.INVALID_FUNCTION_CALL })
    }

    @Test
    fun `a surface created again hears about a problem it was already told about`() {
        repeat(2) {
            createSurface()
            handle(mapOf("updateDataModel" to mapOf("surfaceId" to "s", "path" to "", "value" to "y")))
            handle(mapOf("updateDataModel" to mapOf("surfaceId" to "s", "path" to "", "value" to "y")))
            handle(mapOf("deleteSurface" to mapOf("surfaceId" to "s")))
        }

        assertEquals(listOf(RendererError.DATA_ERROR, RendererError.DATA_ERROR), errors.map { it.code })
    }

    @Test
    fun `component entries that are not components are reported where they are`() {
        createSurface(
            listOf(
                "oops",
                mapOf("component" to "Text", "text" to "No id"),
                mapOf("id" to "typeless"),
                text(text = "Hello"),
                text("1bad", "Bad id"),
            ),
        )

        assertEquals(
            listOf(
                RendererError(
                    RendererError.VALIDATION_FAILED,
                    "Component entry 0 must be an object.",
                    "s",
                    "/components/0",
                ),
                RendererError(RendererError.VALIDATION_FAILED, "Component entry 1 has no 'id'.", "s", "/components/1/id"),
                RendererError(
                    RendererError.VALIDATION_FAILED,
                    "Component 'typeless' has no 'component' type.",
                    "s",
                    "/components/2/component",
                ),
            ),
            errors.take(3),
        )
        assertEquals("/components/4/id", errors.single { it.message.contains("'1bad'") }.path)
        assertEquals(listOf("root", "1bad"), processor.surface("s")!!.components.keys.toList())
    }

    @Test
    fun `a renderer-only function cannot be called by the agent`() {
        handle(
            mapOf(
                "callRendererFunction" to mapOf(
                    "functionCallId" to "c1",
                    "callFunction" to mapOf(
                        "@call" to "formatString",
                        "catalogId" to A2UIProtocol.BASIC_CATALOG_ID,
                        "args" to mapOf("value" to "x"),
                    ),
                ),
            ),
        )

        val response = (sent.single() as RendererMessage.RendererFunctionResponse).response
        assertEquals(RendererError.INVALID_FUNCTION_CALL, response.error?.code)
    }

    @Test
    fun `unresolved functions go to the agent once and read its answer`() {
        createSurface(listOf(text(text = mapOf("@call" to "weather", "args" to mapOf("city" to "Ohrid")))))
        val surface = processor.surface("s")!!
        fun read() = NodeContext(surface, surface.rootNode()!!).string("text")

        assertNull(read())
        assertNull(read())
        val call = sent.filterIsInstance<RendererMessage.CallAgentFunction>().single()
        assertEquals(mapOf("city" to "Ohrid"), call.call.arguments)

        handle(mapOf("agentFunctionResponse" to mapOf("functionCallId" to call.functionCallId, "value" to "Sunny")))

        assertEquals("Sunny", read())
        assertEquals(1, sent.filterIsInstance<RendererMessage.CallAgentFunction>().size)
    }

    @Test
    fun `a call from a gesture says so and asks again each time`() {
        createSurface(listOf(text(text = mapOf("@call" to "weather", "args" to mapOf("city" to "Ohrid")))))
        val surface = processor.surface("s")!!
        NodeContext(surface, surface.rootNode()!!).string("text")
        val action = mapOf("functionCall" to mapOf("@call" to "weather", "args" to mapOf("city" to "Ohrid")))

        surface.performRaw(action, "root", DataContext(surface.dataModel))
        surface.performRaw(action, "root", DataContext(surface.dataModel))

        val calls = sent.filterIsInstance<RendererMessage.CallAgentFunction>()
        assertEquals(listOf(false, true, true), calls.map { it.userActivated })
        assertEquals(3, calls.map { it.functionCallId }.toSet().size)
        assertTrue(calls.none { "userActivated" in (it.toJson()["callAgentFunction"] as Map<*, *>) })
    }

    @Test
    fun `different arguments make a call of their own`() {
        createSurface(
            listOf(
                mapOf("id" to "root", "component" to "Column", "children" to listOf("a", "b")),
                text("a", mapOf("@call" to "weather", "args" to mapOf("city" to "Ohrid"))),
                text("b", mapOf("@call" to "weather", "args" to mapOf("city" to "Skopje"))),
            ),
        )
        val surface = processor.surface("s")!!
        NodeContext(surface, surface.rootNode()!!).children().forEach { NodeContext(surface, it).string("text") }

        assertEquals(2, sent.filterIsInstance<RendererMessage.CallAgentFunction>().size)
    }

    @Test
    fun `a call the agent doesn't answer in time fails and isn't retried`() {
        createSurface(listOf(text(text = mapOf("@call" to "weather"))))
        val surface = processor.surface("s")!!
        fun read() = NodeContext(surface, surface.rootNode()!!).string("text")
        read()

        scheduled.single().invoke()

        assertNull(read())
        assertEquals(RendererError.TIMEOUT, errors.last().code)
        assertEquals(1, sent.filterIsInstance<RendererMessage.CallAgentFunction>().size)
    }

    @Test
    fun `a response for an unknown call is ignored`() {
        handle(mapOf("agentFunctionResponse" to mapOf("functionCallId" to "unknown", "value" to 1)))

        assertTrue(sent.isEmpty())
    }

    @Test
    fun `a list of messages is applied one by one`() {
        processor.handleAny(
            mapOf(
                "messages" to listOf(
                    mapOf("version" to "1.0", "createSurface" to mapOf("surfaceId" to "a")),
                    "not a message",
                    mapOf("version" to "1.0", "createSurface" to mapOf("surfaceId" to "b")),
                ),
            ),
        )

        assertEquals(listOf("a", "b"), processor.surfaces.keys.toList())
        assertEquals(1, errors.size)
    }

    @Test
    fun `a change listener hears about components and data`() {
        var changes = 0
        processor.addChangeListener { changes++ }
        createSurface()
        handle(mapOf("updateDataModel" to mapOf("surfaceId" to "s", "path" to "/a", "value" to 1)))
        handle(mapOf("deleteSurface" to mapOf("surfaceId" to "s")))

        assertEquals(3, changes)
    }

    @Test
    fun `deleteSurface removes the surface and its data`() {
        createSurface(data = mapOf("a" to 1))
        handle(mapOf("deleteSurface" to mapOf("surfaceId" to "s")))

        assertNull(processor.surface("s"))
        createSurface()
        assertEquals(emptyMap<String, Any?>(), processor.surface("s")!!.dataModel.snapshot)
    }

    @Test
    fun `an empty path replaces the whole data model, which must stay an object`() {
        createSurface(data = mapOf("a" to "x"))
        handle(mapOf("updateDataModel" to mapOf("surfaceId" to "s", "path" to "", "value" to "y")))

        assertEquals(mapOf("a" to "x"), processor.surface("s")!!.dataModel.snapshot)
        assertEquals(1, errors.size)

        handle(mapOf("updateDataModel" to mapOf("surfaceId" to "s", "path" to "", "value" to mapOf("b" to "z"))))

        assertEquals(mapOf("b" to "z"), processor.surface("s")!!.dataModel.snapshot)
    }

    @Test
    fun `a call to the agent carries the catalog it resolved in`() {
        createSurface(listOf(text(text = mapOf("@call" to "lookup", "args" to mapOf("q" to "ohrid")))))
        val surface = processor.surface("s")!!
        NodeContext(surface, surface.rootNode()!!).string("text")

        val call = sent.filterIsInstance<RendererMessage.CallAgentFunction>().single().call
        assertEquals("lookup", call.name)
        assertEquals(A2UIProtocol.BASIC_CATALOG_ID, call.catalogId)
    }

    @Test
    fun `a very long data path is reported, not overflowing the stack`() {
        createSurface()
        val path = (1..10_000).joinToString("") { "/a" }
        handle(mapOf("updateDataModel" to mapOf("surfaceId" to "s", "path" to path, "value" to 1)))

        assertEquals(RendererError.RECURSION_LIMIT_EXCEEDED, errors.single().code)
        assertEquals(emptyMap<String, Any?>(), processor.surface("s")!!.dataModel.snapshot)
    }

    @Test
    fun `path segments count toward the nesting limit of a value`() {
        createSurface()
        val path = (1..30).joinToString("") { "/a" }
        val value = (1..30).fold<Int, Any?>(1) { inner, _ -> mapOf("b" to inner) }
        handle(mapOf("updateDataModel" to mapOf("surfaceId" to "s", "path" to path, "value" to value)))

        assertEquals(RendererError.RECURSION_LIMIT_EXCEEDED, errors.single().code)
    }

    @Test
    fun `a data model refuses a path longer than the nesting limit`() {
        val model = DataModel()
        val path = DataPath(List(10_000) { "a" }, isAbsolute = true)

        val error = runCatching { model.set(path, 1) }.exceptionOrNull()

        assertTrue(error is DataModelException)
        assertEquals(emptyMap<String, Any?>(), model.snapshot)
    }

    @Test
    fun `a very long component chain is reported, not overflowing the stack`() {
        val count = 10_000
        val components = (0 until count).map { index ->
            val id = if (index == 0) "root" else "c$index"
            val children = if (index < count - 1) listOf("c${index + 1}") else emptyList()
            mapOf("id" to id, "component" to "Column", "children" to children)
        }
        createSurface(components)

        val treeErrors = TopologyValidator().errors(processor.surface("s")!!)

        assertEquals(RendererError.RECURSION_LIMIT_EXCEEDED, treeErrors.single().code)
    }

    @Test
    fun `a very long component cycle is reported, not overflowing the stack`() {
        val count = 10_000
        val components = (0 until count).map { index ->
            val id = if (index == 0) "root" else "c$index"
            val next = if (index < count - 1) "c${index + 1}" else "root"
            mapOf("id" to id, "component" to "Column", "children" to listOf(next))
        }
        createSurface(components)

        val treeErrors = TopologyValidator().errors(processor.surface("s")!!)

        assertEquals(RendererError.RECURSION_LIMIT_EXCEEDED, treeErrors.single().code)
        assertTrue(treeErrors.single().message.startsWith("Circular reference detected: root -> c1"))
    }

    @Test
    fun `a createSurface whose components are not an array is reported`() {
        handle(mapOf("createSurface" to mapOf("surfaceId" to "s", "components" to mapOf("id" to "root"))))

        assertEquals(RendererError.VALIDATION_FAILED, errors.single().code)
        assertEquals("/components", errors.single().path)
        assertNull(processor.surface("s"))
    }

    @Test
    fun `the tree check and the node tree agree on the nesting limit`() {
        fun chain(levels: Int) = (0 until levels).map { index ->
            val id = if (index == 0) "root" else "c$index"
            val children = if (index < levels - 1) listOf("c${index + 1}") else emptyList()
            mapOf("id" to id, "component" to "Column", "children" to children)
        }
        val limit = A2UIProtocol.MAX_NESTING_DEPTH
        for ((levels, allowed) in listOf(limit to true, limit + 1 to false)) {
            sent.clear()
            handle(mapOf("deleteSurface" to mapOf("surfaceId" to "s")))
            createSurface(chain(levels))
            val surface = processor.surface("s")!!

            val treeErrors = TopologyValidator().errors(surface)
            var node = surface.rootNode()
            var depth = 0
            while (node != null) {
                node = NodeContext(surface, node).children().singleOrNull()
                if (node != null) depth++
            }

            assertEquals("$levels levels", allowed, treeErrors.isEmpty())
            assertEquals("$levels levels", allowed, errors.none { it.code == RendererError.RECURSION_LIMIT_EXCEEDED })
            assertEquals("$levels levels", limit - 1, depth)
        }
    }
}
