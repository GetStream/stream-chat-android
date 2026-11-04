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

package io.getstream.chat.android.ai.openui.lang

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** A tool provider that records calls and returns scripted results. */
internal class RecordingToolProvider(
    override val minimumRefreshInterval: Duration = 5.seconds,
    private val handler: suspend (name: String, arguments: OpenUIObject, count: Int) -> OpenUIValue,
) : ToolProvider {
    val calls = ArrayList<Pair<String, OpenUIObject>>()

    /** Whether the person asked for each call, in the order of [calls]. */
    val userInitiated = ArrayList<Boolean>()

    override suspend fun callTool(name: String, arguments: OpenUIObject): OpenUIValue {
        calls += name to arguments
        return handler(name, arguments, calls.size)
    }

    override suspend fun callTool(name: String, arguments: OpenUIObject, userInitiated: Boolean): OpenUIValue {
        this.userInitiated += userInitiated
        return callTool(name, arguments)
    }
}

internal class ToolFailure : Exception("boom")

internal class RuntimeTest {

    @Nested
    inner class Store {
        @Test
        fun `set get and change notifications`() {
            val store = OpenUIStore()
            val changes = ArrayList<String>()
            store.onChange = { changes += it }
            store["\$a"] = v(1)
            store["\$a"] = v(1)
            store["\$a"] = v(2)
            store["\$b"] = obj("x" to 1)
            store["\$b"] = obj("x" to 1)
            assertEquals(v(2), store["\$a"])
            assertEquals(listOf("\$a", "\$a", "\$b"), changes)
            store["\$a"] = null
            assertEquals(null, store["\$a"])
        }

        @Test
        fun `initialize only adds new defaults and applies persisted`() {
            val store = OpenUIStore()
            store["\$days"] = v("30")
            store.initialize(
                OpenUIObject.of("\$days" to v("7"), "\$title" to v("")),
                OpenUIObject.of("\$restored" to v(true)),
            )
            assertEquals(v("30"), store["\$days"])
            assertEquals(v(""), store["\$title"])
            assertEquals(v(true), store["\$restored"])
            // Declarations disappearing never delete user state.
            store.initialize(OpenUIObject.Empty, OpenUIObject.Empty)
            assertEquals(v("30"), store["\$days"])
        }

        @Test
        fun `defaults follow streaming declarations until the user changes them`() {
            val store = OpenUIStore()
            // `$servings = "` mid-stream, then the complete declaration.
            store.initialize(OpenUIObject.of("\$servings" to v("")), OpenUIObject.Empty)
            store.initialize(OpenUIObject.of("\$servings" to v("2")), OpenUIObject.Empty)
            assertEquals(v("2"), store["\$servings"])
            store["\$servings"] = v("4")
            store.initialize(OpenUIObject.of("\$servings" to v("6")), OpenUIObject.Empty)
            assertEquals(v("4"), store["\$servings"])
            // Restored values are kept like user changes.
            store.initialize(OpenUIObject.of("\$days" to v("7")), OpenUIObject.of("\$days" to v("30")))
            store.initialize(OpenUIObject.of("\$days" to v("14")), OpenUIObject.Empty)
            assertEquals(v("30"), store["\$days"])
        }
    }

    @Nested
    inner class Queries {
        private fun node(id: String, args: OpenUIValue = obj(), deps: OpenUIValue? = null, refresh: Double? = null) =
            QueryManager.QueryNode(id, "list", args, obj("rows" to emptyList<Any>()), deps, refresh)

        @Test
        fun `fetches and caches results`() = runTest {
            val provider = RecordingToolProvider { _, args, _ -> obj("rows" to listOf(args["q"] ?: v("none"))) }
            val manager = QueryManager(provider, this)
            manager.evaluateQueries(listOf(node("data", args = obj("q" to "a"))))
            assertEquals(obj("rows" to emptyList<Any>()), manager.result("data"))
            assertTrue(manager.isLoading("data"))
            advanceUntilIdle()
            assertEquals(obj("rows" to listOf("a")), manager.result("data"))
            assertFalse(manager.isAnyLoading)

            // Same cache key: no new fetch.
            manager.evaluateQueries(listOf(node("data", args = obj("q" to "a"))))
            advanceUntilIdle()
            assertEquals(1, provider.calls.size)
        }

        @Test
        fun `refetches when arguments change and keeps previous data while loading`() = runTest {
            val provider = RecordingToolProvider { _, args, _ ->
                delay(80)
                obj("rows" to listOf(args["q"] ?: v("none")))
            }
            val manager = QueryManager(provider, this)
            manager.evaluateQueries(listOf(node("data", args = obj("q" to "a"), deps = obj("\$q" to "a"))))
            advanceUntilIdle()
            assertEquals(obj("rows" to listOf("a")), manager.result("data"))

            manager.evaluateQueries(listOf(node("data", args = obj("q" to "b"), deps = obj("\$q" to "b"))))
            assertTrue(manager.isLoading("data"))
            assertEquals(
                obj("rows" to listOf("a")),
                manager.result("data"),
                "previous data stays visible while refetching",
            )
            assertEquals(listOf("data"), manager.snapshot.value.refetching)
            advanceUntilIdle()
            assertEquals(obj("rows" to listOf("b")), manager.result("data"))
            assertEquals(2, provider.calls.size)
        }

        @Test
        fun `invalidate refetches and queues while in flight`() = runTest {
            val provider = RecordingToolProvider { _, _, count ->
                delay(50)
                obj("n" to count)
            }
            val manager = QueryManager(provider, this)
            manager.evaluateQueries(listOf(node("data")))
            manager.invalidate(listOf("data")) // in flight: queued
            advanceUntilIdle()
            assertEquals(2, provider.calls.size)
            assertFalse(manager.isAnyLoading)
            assertEquals(obj("n" to 2), manager.result("data"))
            manager.invalidate()
            advanceUntilIdle()
            assertEquals(obj("n" to 3), manager.result("data"))
        }

        @Test
        fun `fetches with the latest tool provider`() = runTest {
            val first = RecordingToolProvider { _, _, _ -> obj("from" to "first") }
            val second = RecordingToolProvider { _, _, _ -> obj("from" to "second") }
            val manager = QueryManager(first, this)
            manager.toolProvider = second
            manager.evaluateQueries(listOf(node("data")))
            advanceUntilIdle()
            assertEquals(obj("from" to "second"), manager.result("data"))
            assertTrue(first.calls.isEmpty())
        }

        @Test
        fun `reports tool errors`() = runTest {
            val manager = QueryManager(FunctionToolProvider("other" to { _ -> OpenUIValue.Null }), this)
            manager.evaluateQueries(listOf(node("data")))
            advanceUntilIdle()
            val error = manager.snapshot.value.errors
                .single()
            assertEquals("tool-not-found", error.code)
            assertEquals(OpenUIError.Source.Query, error.source)
            assertEquals("data", error.statementId)
            assertEquals("Available tools: other", error.hint)
            assertEquals(obj("rows" to emptyList<Any>()), manager.result("data"), "defaults remain")

            val failing = QueryManager(RecordingToolProvider { _, _, _ -> throw ToolFailure() }, this)
            failing.evaluateQueries(listOf(node("data")))
            advanceUntilIdle()
            assertEquals("tool-error", failing.snapshot.value.errors.first().code)
            assertEquals("Query \"list\" failed: boom", failing.snapshot.value.errors.first().message)
        }

        @Test
        fun `refresh interval refetches periodically and stops when removed`() = runTest {
            val provider = RecordingToolProvider(minimumRefreshInterval = 10.milliseconds) { _, _, count ->
                obj("n" to count)
            }
            val manager = QueryManager(provider, backgroundScope)
            manager.evaluateQueries(listOf(node("health", refresh = 0.05)))
            advanceTimeBy(160)
            runCurrent()
            assertTrue(provider.calls.size >= 3)
            manager.evaluateQueries(emptyList())
            val count = provider.calls.size
            advanceTimeBy(500)
            runCurrent()
            assertEquals(count, provider.calls.size)
        }

        @Test
        fun `a query sharing a key keeps the result when the fetching query moves away`() = runTest {
            val provider = RecordingToolProvider { _, args, _ ->
                delay(80)
                obj("rows" to listOf(args["q"] ?: v("none")))
            }
            val manager = QueryManager(provider, this)
            manager.evaluateQueries(listOf(node("a", args = obj("q" to "x"))))
            // Joins the in-flight fetch, then the query that started it moves to other args.
            manager.evaluateQueries(listOf(node("a", args = obj("q" to "x")), node("b", args = obj("q" to "x"))))
            manager.evaluateQueries(listOf(node("a", args = obj("q" to "y")), node("b", args = obj("q" to "x"))))
            advanceUntilIdle()
            assertEquals(obj("rows" to listOf("x")), manager.result("b"))
            assertEquals(obj("rows" to listOf("y")), manager.result("a"))
            assertFalse(manager.isAnyLoading)
            assertEquals(2, provider.calls.size)
        }

        @Test
        fun `a query that moves onto a key another query is fetching stops loading when it settles`() = runTest {
            val provider = RecordingToolProvider { _, args, _ ->
                delay(if (args["q"] == v("x")) 40 else 80)
                obj("rows" to listOf(args["q"] ?: v("none")))
            }
            val manager = QueryManager(provider, this)
            manager.evaluateQueries(listOf(node("a", args = obj("q" to "y")), node("b", args = obj("q" to "x"))))
            manager.evaluateQueries(listOf(node("a", args = obj("q" to "y")), node("b", args = obj("q" to "y"))))
            assertTrue(manager.isLoading("b"))
            advanceUntilIdle()
            assertFalse(manager.isLoading("b"))
            assertEquals(emptyList(), manager.snapshot.value.loading)
            assertEquals(obj("rows" to listOf("y")), manager.result("b"))
        }

        @Test
        fun `a query invalidated while sharing an in-flight fetch refetches when it settles`() = runTest {
            val provider = RecordingToolProvider { _, _, count ->
                delay(50)
                obj("n" to count)
            }
            val manager = QueryManager(provider, this)
            manager.evaluateQueries(listOf(node("a"), node("b")))
            manager.invalidate(listOf("b"))
            advanceUntilIdle()
            assertEquals(2, provider.calls.size)
            assertEquals(obj("n" to 2), manager.result("a"))
            assertEquals(obj("n" to 2), manager.result("b"))
            assertFalse(manager.isAnyLoading)
        }

        @Test
        fun `a failed shared fetch reports an error for every query on the key`() = runTest {
            val manager = QueryManager(RecordingToolProvider { _, _, _ -> throw ToolFailure() }, this)
            manager.evaluateQueries(listOf(node("a"), node("b")))
            advanceUntilIdle()
            assertEquals(listOf("a", "b"), manager.snapshot.value.errors.map { it.statementId })
            assertFalse(manager.isAnyLoading)
        }

        @Test
        fun `refresh waits at least the tool provider's minimum and is clamped like setInterval`() {
            assertEquals(30.seconds, QueryManager.refreshDelay(30.0, 5.seconds))
            assertEquals(5.seconds, QueryManager.refreshDelay(0.001, 5.seconds))
            assertEquals(5.seconds, QueryManager.refreshDelay(Double.NaN, 5.seconds))
            assertEquals(Int.MAX_VALUE.toLong().milliseconds, QueryManager.refreshDelay(Double.POSITIVE_INFINITY, 5.seconds))
            assertEquals(Int.MAX_VALUE.toLong().milliseconds, QueryManager.refreshDelay(1e300, 5.seconds))
            assertEquals(1.milliseconds, QueryManager.refreshDelay(1e-12, Duration.ZERO))
            assertEquals(5.seconds, FunctionToolProvider().minimumRefreshInterval)
            assertEquals(1.seconds, FunctionToolProvider(emptyMap(), 1.seconds).minimumRefreshInterval)
        }

        @Test
        fun `returning to cached arguments while another fetch is in flight stops loading`() = runTest {
            val provider = RecordingToolProvider { _, args, _ ->
                delay(100)
                obj("rows" to listOf(args["q"] ?: v("none")))
            }
            val manager = QueryManager(provider, this)
            manager.evaluateQueries(listOf(node("data", args = obj("q" to "a"))))
            advanceUntilIdle()
            manager.evaluateQueries(listOf(node("data", args = obj("q" to "b"))))
            runCurrent()
            assertTrue(manager.isLoading("data"))

            manager.evaluateQueries(listOf(node("data", args = obj("q" to "a"))))

            assertFalse(manager.isLoading("data"))
            assertEquals(obj("rows" to listOf("a")), manager.result("data"))
            advanceUntilIdle()
            assertFalse(manager.isLoading("data"))
            assertEquals(obj("rows" to listOf("a")), manager.result("data"))
        }

        @Test
        fun `mutations report status and reject concurrent calls`() = runTest {
            val provider = RecordingToolProvider { name, args, _ ->
                delay(30)
                if (name == "fail") throw ToolFailure()
                obj("id" to "42", "title" to (args["title"] ?: OpenUIValue.Null))
            }
            val manager = QueryManager(provider, this)
            manager.registerMutations(
                listOf(QueryManager.MutationNode("create", "create"), QueryManager.MutationNode("broken", "fail")),
            )
            assertEquals(obj("status" to "idle", "data" to null, "error" to null), manager.mutationResult("create"))

            val first = async { manager.fireMutation("create", OpenUIObject.of("title" to v("A"))) }
            runCurrent()
            assertEquals(v("loading"), manager.mutationResult("create")["status"])
            assertFalse(manager.fireMutation("create", OpenUIObject.Empty))
            assertTrue(first.await())
            assertEquals(
                obj("status" to "success", "data" to mapOf("id" to "42", "title" to "A")),
                manager.mutationResult("create"),
            )

            assertFalse(manager.fireMutation("broken", OpenUIObject.Empty))
            assertEquals(obj("status" to "error", "error" to "boom"), manager.mutationResult("broken"))
            assertEquals(OpenUIError.Source.Mutation, manager.snapshot.value.errors.first().source)
        }

        @Test
        fun `queries run tools on their own and mutations as the person asked`() = runTest {
            val provider = RecordingToolProvider { _, _, _ -> obj("ok" to true) }
            val manager = QueryManager(provider, this)
            manager.evaluateQueries(listOf(QueryManager.QueryNode("data", "list", obj(), obj(), null, null)))
            advanceUntilIdle()
            manager.registerMutations(listOf(QueryManager.MutationNode("save", "save")))
            assertTrue(manager.fireMutation("save", OpenUIObject.Empty))

            assertEquals(listOf("list", "save"), provider.calls.map { it.first })
            assertEquals(listOf(false, true), provider.userInitiated)
        }

        @Test
        fun `a cancelled mutation goes back to idle and runs again`() = runTest {
            val provider = RecordingToolProvider { _, _, _ ->
                delay(100)
                obj("saved" to true)
            }
            val manager = QueryManager(provider, this)
            manager.registerMutations(listOf(QueryManager.MutationNode("save", "save")))
            val call = launch { manager.fireMutation("save", OpenUIObject.Empty) }
            runCurrent()
            assertEquals(v("loading"), manager.mutationResult("save")["status"])

            call.cancel()
            runCurrent()

            assertEquals(v("idle"), manager.mutationResult("save")["status"])
            assertTrue(manager.fireMutation("save", OpenUIObject.Empty))
            assertEquals(v("success"), manager.mutationResult("save")["status"])
            assertEquals(2, provider.calls.size)
        }

        @Test
        fun `arguments that change quickly fetch once after they settle`() = runTest {
            val provider = RecordingToolProvider { _, args, _ ->
                delay(50)
                obj("rows" to listOf(args["q"] ?: v("none")))
            }
            val manager = QueryManager(provider, this)
            manager.evaluateQueries(listOf(node("data", args = obj("q" to ""))))
            advanceUntilIdle()
            provider.calls.clear()

            val typed = "barcelona"
            for (length in 1..typed.length) {
                manager.evaluateQueries(listOf(node("data", args = obj("q" to typed.take(length)))))
                advanceTimeBy(100)
            }
            assertTrue(provider.calls.isEmpty(), "no fetch while the arguments keep changing")
            assertTrue(manager.isLoading("data"))
            advanceUntilIdle()

            assertEquals(listOf(v(typed)), provider.calls.map { it.second["q"] })
            assertEquals(obj("rows" to listOf(typed)), manager.result("data"))
            assertFalse(manager.isLoading("data"))
        }
    }

    @Nested
    inner class RuntimeLifecycle {
        private val library = LibrarySchema.fromSpec(Fixtures.json("upstream-chat-library.spec.json"))

        private fun TestScope.runtime(tools: ToolProvider? = null, initialState: OpenUIObject? = null) =
            OpenUIRuntime(library, this, tools, initialState)

        private fun texts(runtime: OpenUIRuntime): List<String> {
            val result = ArrayList<String>()
            fun walk(value: OpenUIValue) {
                when (value) {
                    is OpenUIValue.Element -> {
                        if (value.node.typeName == "TextContent") {
                            value.node.props["text"]?.let { result += JS.toString(it) }
                        }
                        value.node.props.values
                            .forEach(::walk)
                    }
                    is OpenUIValue.Arr -> value.items.forEach(::walk)
                    is OpenUIValue.Obj -> value.fields.values.forEach(::walk)
                    else -> Unit
                }
            }
            runtime.root?.let { walk(OpenUIValue.Element(it)) }
            return result
        }

        private fun firstChild(runtime: OpenUIRuntime) = runtime.root
            ?.props
            ?.get("children")
            .arrayValue
            ?.first()
            ?.elementValue

        @Test
        fun `streams and evaluates state`() = runTest {
            val runtime = runtime()
            runtime.update(
                "root = Card([greeting])\n\$name = \"Ada\"\ngreeting = TextContent(\"Hi \" + \$na",
                isStreaming = true,
            )
            assertEquals("Card", runtime.root?.typeName)
            assertEquals(true, runtime.parseResult?.meta?.incomplete)
            runtime.update(
                "root = Card([greeting])\n\$name = \"Ada\"\ngreeting = TextContent(\"Hi \" + \$name)",
                isStreaming = false,
            )
            assertEquals(listOf("Hi Ada"), texts(runtime))
            runtime.store["\$name"] = v("Grace")
            assertEquals(listOf("Hi Grace"), texts(runtime))
        }

        @Test
        fun `reactive fields write the bound variable`() = runTest {
            val runtime = runtime()
            runtime.update(
                """
                root = Card([field, echo])
                field = Input("name", "Name", "text", null, ${'$'}name)
                echo = TextContent("Typed: " + ${'$'}name)
                """.trimIndent(),
                isStreaming = false,
            )
            val binding = firstChild(runtime)?.props?.get("value")
            assertEquals("\$name", ReactiveBinding.target(binding))
            val field = runtime.stateField("name", binding, formName = null)
            assertTrue(field.isReactive)
            field.setValue(v("Linus"))
            assertEquals(v("Linus"), runtime.store["\$name"])
            assertTrue("Typed: Linus" in texts(runtime))
        }

        @Test
        fun `form fields are stored under the form name`() = runTest {
            val updates = ArrayList<OpenUIObject>()
            val runtime = runtime()
            runtime.onStateUpdate = { updates += it }
            runtime.update("root = Card([TextContent(\"x\")])", isStreaming = false)
            val field = runtime.stateField("email", null, formName = "contact", componentType = "Input")
            assertFalse(field.isReactive)
            field.setValue(v("a@b.co"))
            assertEquals(v("a@b.co"), runtime.fieldValue("contact", "email"))
            val expected = obj("email" to mapOf("value" to "a@b.co", "componentType" to "Input"))
            assertEquals(expected, runtime.store["contact"])
            assertNotNull(updates.last()["contact"])
            assertEquals(OpenUIObject.of("contact" to expected), runtime.formPayload("contact"))
        }

        @Test
        fun `action plans run steps in order`() = runTest {
            val events = ArrayList<ActionEvent>()
            val provider = RecordingToolProvider { name, args, _ ->
                if (name == "save") {
                    obj("ok" to true, "title" to (args["title"] ?: OpenUIValue.Null))
                } else {
                    obj("rows" to emptyList<Any>())
                }
            }
            val runtime = runtime(provider)
            runtime.onAction = { events += it }
            runtime.update(
                """
                ${'$'}title = "Draft"
                ${'$'}saved = false
                data = Query("list", {}, {rows: []})
                save = Mutation("save", {title: ${'$'}title})
                root = Card([btn])
                btn = Button("Save", Action([@Run(save), @Set(${'$'}saved, true), @Run(data), @Reset(${'$'}title), @ToAssistant("Saved " + ${'$'}title, "ctx"), @OpenUrl("https://example.com")]))
                """.trimIndent(),
                isStreaming = false,
            )
            runtime.store["\$title"] = v("Final")
            advanceUntilIdle()
            assertFalse(runtime.isQueryLoading)

            runtime.triggerAction("Save", action = firstChild(runtime)?.props?.get("action"))
            advanceUntilIdle()

            assertEquals(OpenUIObject.of("title" to v("Final")), provider.calls.first { it.first == "save" }.second)
            assertEquals(v(true), runtime.store["\$saved"])
            assertEquals(v("Draft"), runtime.store["\$title"], "@Reset restores the declared default")
            assertEquals(v("success"), runtime.queryManager.mutationResult("save")["status"])
            assertEquals(2, events.size)
            assertEquals(BuiltinActionType.CONTINUE_CONVERSATION, events[0].type)
            assertEquals("Saved Final", events[0].humanFriendlyMessage, "evaluated when the plan was built")
            assertEquals(v("ctx"), events[0].context)
            assertEquals(BuiltinActionType.OPEN_URL, events[1].type)
            assertEquals("https://example.com", events[1].url)
            assertEquals(2, provider.calls.count { it.first == "list" }, "@Run(query) refetches")
        }

        @Test
        fun `failing mutation halts the plan`() = runTest {
            val events = ArrayList<ActionEvent>()
            val runtime = runtime(RecordingToolProvider { _, _, _ -> throw ToolFailure() })
            runtime.onAction = { events += it }
            runtime.update(
                """
                ${'$'}done = false
                save = Mutation("save", {})
                root = Card([Button("Go", Action([@Run(save), @Set(${'$'}done, true), @ToAssistant("next")]))])
                """.trimIndent(),
                isStreaming = false,
            )
            runtime.triggerAction("Go", action = firstChild(runtime)?.props?.get("action"))
            assertEquals(v(false), runtime.store["\$done"])
            assertTrue(events.isEmpty())
            assertEquals(v("error"), runtime.queryManager.mutationResult("save")["status"])
        }

        @Test
        fun `legacy and default actions`() = runTest {
            val events = ArrayList<ActionEvent>()
            val runtime = runtime()
            runtime.onAction = { events += it }
            runtime.update("root = Card([TextContent(\"x\")])", isStreaming = false)
            runtime.triggerAction("Plan", action = obj("type" to "continue_conversation", "context" to "Help me plan"))
            runtime.triggerAction("Open", action = obj("type" to "open_url", "url" to "https://openui.com"))
            runtime.triggerAction("Tell me more")
            assertEquals(listOf("continue_conversation", "open_url", "continue_conversation"), events.map { it.type })
            assertEquals(v("Help me plan"), events[0].context)
            assertEquals("https://openui.com", events[1].url)
            assertEquals("Tell me more", events[2].humanFriendlyMessage)
            assertTrue(events[2].assistantMessage.startsWith(MessageEnvelope.CONTENT_MARKER + "\nTell me more"))
        }

        @Test
        fun `query results flow into the tree`() = runTest {
            val tools = FunctionToolProvider(
                "list_hotels" to { args ->
                    val max = JS.toNumber(args["maxPrice"])
                    val hotels = listOf(
                        obj("name" to "Alfama Inn", "price" to 90),
                        obj("name" to "Chiado Grand", "price" to 210),
                    )
                    obj("hotels" to hotels.filter { JS.toNumber(it["price"]) <= max })
                },
            )
            val runtime = runtime(tools)
            val program = """
                ${'$'}max = 300
                hotels = Query("list_hotels", {maxPrice: ${'$'}max}, {hotels: []})
                root = Card([count])
                count = TextContent("" + @Count(hotels.hotels) + " hotels: " + hotels.hotels.name)
            """.trimIndent()
            runtime.update(program, isStreaming = true)
            assertEquals(listOf("0 hotels: "), texts(runtime), "queries wait for streaming to end")
            runtime.update(program, isStreaming = false)
            advanceUntilIdle()
            assertEquals(listOf("2 hotels: Alfama Inn,Chiado Grand"), texts(runtime))
            runtime.store["\$max"] = v(100)
            advanceUntilIdle()
            assertEquals(listOf("1 hotels: Alfama Inn"), texts(runtime))
        }

        @Test
        fun `binding defaults split across chunks use the final value`() = runTest {
            val tools =
                RecordingToolProvider { _, args, _ -> obj("servings" to (args["servings"] ?: OpenUIValue.Null)) }
            val runtime = runtime(tools)
            val program = """
                root = Card([summary])
                ${'$'}servings = "2"
                recipe = Query("scale_recipe", {servings: ${'$'}servings}, {servings: 0})
                summary = TextContent("Serves " + recipe.servings)
            """.trimIndent()
            // Render at every chunk boundary, including inside `"2"`.
            for (end in 1 until program.length) runtime.update(program.substring(0, end), isStreaming = true)
            runtime.update(program, isStreaming = false)
            assertEquals(v("2"), runtime.store["\$servings"])
            advanceUntilIdle()
            assertEquals(listOf("Serves 2"), texts(runtime))
            assertEquals(listOf("2"), tools.calls.map { JS.toString(it.second["servings"]) })
        }

        @Test
        fun `reports errors after streaming`() = runTest {
            val reported = ArrayList<List<OpenUIError>>()
            val runtime = runtime()
            runtime.onError = { reported += it }
            runtime.update("root = Card([Ghost(\"x\")])", isStreaming = true)
            assertTrue(reported.isEmpty())
            runtime.update("root = Card([Ghost(\"x\")])", isStreaming = false)
            assertEquals("unknown-component", reported.last().first().code)
            assertTrue(reported.last().first().hint!!.startsWith("Available components: Card"))
            runtime.update("root = Card([Ghost(\"x\")])\n", isStreaming = true)
            assertEquals(emptyList(), reported.last())

            var emptyErrors = emptyList<OpenUIError>()
            val empty = runtime()
            empty.onError = { emptyErrors = it }
            empty.update("just some text", isStreaming = false)
            assertEquals("parse-failed", emptyErrors.first().code)
        }

        @Test
        fun `restores initial state`() = runTest {
            val runtime = runtime(
                initialState = OpenUIObject.of(
                    "\$name" to v("Restored"),
                    "contact" to obj("email" to mapOf("value" to "x@y.z")),
                ),
            )
            runtime.update("\$name = \"Default\"\nroot = Card([TextContent(\$name)])", isStreaming = false)
            assertEquals(listOf("Restored"), texts(runtime))
            assertEquals(v("x@y.z"), runtime.fieldValue("contact", "email"))
        }
    }
}
