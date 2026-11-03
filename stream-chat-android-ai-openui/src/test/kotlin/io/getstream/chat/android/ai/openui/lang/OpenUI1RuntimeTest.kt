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

import io.getstream.chat.android.ai.openui.library.DefinedAction
import io.getstream.chat.android.ai.openui.library.DefinedFunction
import io.getstream.chat.android.ai.openui.library.PropField
import io.getstream.chat.android.ai.openui.library.PropType
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The OpenUI 1.0 runtime: action plans, library calls, the entry rule and the message protocol. */
internal class OpenUI1RuntimeTest {
    private val percent = DefinedFunction(
        name = "Percent",
        description = "Share of a total, in percent",
        params = listOf(PropField.required("part", PropType.number), PropField.required("total", PropType.number)),
        returns = PropType.number,
    ) { args ->
        val total = args["total"].numberValue ?: 0.0
        check(total != 0.0) { "division by zero" }
        OpenUIValue.Num((args["part"].numberValue ?: 0.0) / total * 100)
    }

    private val copy = DefinedAction(
        name = "CopyToClipboard",
        description = "Copies text",
        params = listOf(PropField.required("text", PropType.string)),
    )

    private val library = LibrarySchema.fromSpec(
        Fixtures.json("upstream-chat-library.spec.json"),
        functions = listOf(percent),
        customActions = listOf(copy),
    )

    private fun TestScope.runtime(
        program: String,
        events: MutableList<ActionEvent>? = null,
        library: LibrarySchema = this@OpenUI1RuntimeTest.library,
    ): OpenUIRuntime {
        val runtime = OpenUIRuntime(library, this)
        if (events != null) runtime.onAction = { events += it }
        runtime.update(program, isStreaming = false)
        return runtime
    }

    private fun element(runtime: OpenUIRuntime, statementId: String): ElementNode? {
        var found: ElementNode? = null
        fun visit(value: OpenUIValue?) {
            when (value) {
                is OpenUIValue.Element -> {
                    if (value.node.statementId == statementId) found = value.node
                    value.node.props.values
                        .forEach(::visit)
                }
                is OpenUIValue.Arr -> value.items.forEach(::visit)
                else -> Unit
            }
        }
        runtime.root?.let { visit(OpenUIValue.Element(it)) }
        return found
    }

    private suspend fun click(runtime: OpenUIRuntime, statementId: String) {
        val button = element(runtime, statementId)
        runtime.triggerAction(button?.props?.get("label").stringValue.orEmpty(), action = button?.props?.get("action"))
    }

    @Test
    fun `a bare step runs without an Action wrapper`() = runTest {
        val events = ArrayList<ActionEvent>()
        val runtime = runtime(
            """
            ${'$'}sel = "a"
            root = Card([ask, pick])
            ask = Button("Ask", @ToAssistant("Tell me more"))
            pick = Button("Pick", @Set(${'$'}sel, "b"))
            """.trimIndent(),
            events,
        )

        click(runtime, "ask")
        click(runtime, "pick")

        assertEquals(listOf("Tell me more"), events.map { it.humanFriendlyMessage })
        assertEquals(v("b"), runtime.store["\$sel"])
    }

    @Test
    fun `nested plans flatten and an invalid step does nothing`() = runTest {
        val events = ArrayList<ActionEvent>()
        val runtime = runtime(
            """
            ${'$'}a = 0
            root = Card([go, broken])
            prepare = Action([@Set(${'$'}a, 1)])
            go = Button("Go", Action([prepare, @ToAssistant("Done")]))
            broken = Button("Broken", @Run(missing))
            """.trimIndent(),
            events,
        )

        click(runtime, "go")
        click(runtime, "broken")

        assertEquals(v(1), runtime.store["\$a"])
        assertEquals(listOf("Done"), events.map { it.humanFriendlyMessage }, "an invalid step is an empty plan")
    }

    @Test
    fun `the ToAssistant context is any value`() = runTest {
        val events = ArrayList<ActionEvent>()
        val runtime = runtime(
            """
            root = Card([save, zero])
            save = Button("Save", @ToAssistant("Save", {orderId: 42}))
            zero = Button("Zero", @ToAssistant("Zero", 0))
            """.trimIndent(),
            events,
        )

        click(runtime, "save")
        click(runtime, "zero")

        assertEquals(listOf(obj("orderId" to 42), v(0)), events.map { it.context })
        val message = parseMessage(events[0].assistantMessage)
        assertEquals("Save", message.content)
        assertEquals(
            OpenUIValue.Arr(
                listOf(v("User clicked: Save"), OpenUIValue.Obj(events[0].formState!!), obj("orderId" to 42)),
            ),
            message.context,
        )
    }

    @Test
    fun `form state is read when each step runs`() = runTest {
        val events = ArrayList<ActionEvent>()
        val runtime = runtime(
            """
            ${'$'}note = "old"
            root = Card([send])
            send = Button("Send", Action([@Set(${'$'}note, "new"), @ToAssistant("Send")]))
            """.trimIndent(),
            events,
        )

        click(runtime, "send")

        assertEquals(v("new"), events.first().formState?.get("\$note"))
    }

    @Test
    fun `custom actions reach the host with their arguments`() = runTest {
        val events = ArrayList<ActionEvent>()
        val runtime = runtime(
            """
            ${'$'}id = "abc"
            root = Card([copy, wrong, legacy])
            copy = Button("Copy", Action([@CopyToClipboard(${'$'}id), @ToAssistant("Copied")]))
            wrong = Button("Wrong", @CopyToClipboard(5))
            legacy = Button("Legacy", {type: "CopyToClipboard", params: {text: "forged"}})
            """.trimIndent(),
            events,
        )

        click(runtime, "copy")
        click(runtime, "wrong")
        click(runtime, "legacy")

        assertEquals(listOf("CopyToClipboard", BuiltinActionType.CONTINUE_CONVERSATION), events.map { it.type })
        assertEquals(OpenUIObject.of("text" to v("abc")), events.first().params)
    }

    @Test
    fun `data cannot pose as a custom action`() = runTest {
        val events = ArrayList<ActionEvent>()
        val runtime = runtime(
            """
            ${'$'}plan = {steps: [{type: "custom_action", name: "CopyToClipboard", params: {text: "forged"}}]}
            root = Card([forged])
            forged = Button("Forged", ${'$'}plan)
            """.trimIndent(),
            events,
        )

        click(runtime, "forged")

        assertTrue(events.none { it.type == "CopyToClipboard" })
    }

    @Test
    fun `library functions run and report failures`() = runTest {
        val runtime = runtime(
            """
            root = Card([ok, failing])
            ok = TextContent("" + @Percent(1, 4))
            failing = TextContent("" + @Percent(1, 0))
            """.trimIndent(),
        )

        assertEquals(v("25"), element(runtime, "ok")?.props?.get("text"))
        assertEquals(v(""), element(runtime, "failing")?.props?.get("text"), "like upstream, + reads null as \"\"")
        val error = assertNotNull(runtime.errors.firstOrNull { it.code == "runtime-error" })
        assertEquals("Evaluating prop \"text\" on TextContent failed: @Percent threw: division by zero", error.message)
        assertEquals("failing", error.statementId)
    }

    @Test
    fun `an unknown function is null with an error`() = runTest {
        val runtime = runtime(
            """
            root = Card([TextContent("x"), title])
            title = TextContent(@Nope(1))
            """.trimIndent(),
        )

        assertTrue(
            runtime.parseResult?.meta?.errors.orEmpty().any {
                it.code == ValidationErrorCode.UnknownFunction && it.component == "Nope"
            },
        )
        assertNull(element(runtime, "title"), "a required prop that is null drops the component")
    }

    @Test
    fun `a ternary root follows state`() = runTest {
        val runtime = runtime(
            """
            ${'$'}dark = false
            root = ${'$'}dark ? darkPage : lightPage
            darkPage = Card([TextContent("Dark")])
            lightPage = Card([TextContent("Light")])
            """.trimIndent(),
        )
        fun text() = runtime.root?.props?.get("children")[0].elementValue?.props?.get("text")
        assertEquals(v("Light"), text())

        runtime.store["\$dark"] = v(true)

        assertEquals(v("Dark"), text())
    }

    @Test
    fun `programs without an entry report no-root`() = runTest {
        val runtime = runtime("title = TextContent(\"Orders\")")

        assertNull(runtime.root)
        assertEquals(listOf("no-root"), runtime.errors.map { it.code })
    }

    @Test
    fun `a fallback entry renders with a warning`() = runTest {
        val runtime = runtime("main = Card([TextContent(\"Hi\")])")

        assertEquals("main", runtime.root?.statementId)
        val warning = runtime.errors.first()
        assertEquals("no-root", warning.code)
        assertTrue(warning.isWarning)
    }

    @Test
    fun `the end of the stream declares a trailing state name`() = runTest {
        val runtime = OpenUIRuntime(library, this)
        val program = "root = Card([TextContent(${'$'}count)])\n${'$'}total = ${'$'}co"

        runtime.update(program, isStreaming = true)
        assertFalse("\$co" in runtime.parseResult!!.stateDeclarations)

        runtime.update(program, isStreaming = false)
        assertTrue("\$co" in runtime.parseResult!!.stateDeclarations)
    }

    @Test
    fun `a reset value follows its declaration again`() = runTest {
        val runtime = OpenUIRuntime(library, this)
        runtime.update(
            """
            ${'$'}title = "Draft"
            root = Card([reset])
            reset = Button("Reset", @Reset(${'$'}title))
            """.trimIndent(),
            isStreaming = true,
        )
        runtime.store["\$title"] = v("Typed")
        click(runtime, "reset")
        assertEquals(v("Draft"), runtime.store["\$title"])

        runtime.update(
            """
            ${'$'}title = "Draft 2"
            root = Card([reset])
            reset = Button("Reset", @Reset(${'$'}title))
            """.trimIndent(),
            isStreaming = false,
        )

        assertEquals(v("Draft 2"), runtime.store["\$title"])
    }

    @Test
    fun `an inner Each shadows the outer loop variable`() = runTest {
        val playground = LibrarySchema.fromSpec(Fixtures.json("upstream-playground-library.spec.json"))
        val runtime = runtime(
            """
            ${'$'}groups = [{items: ["a", "b"]}, {items: ["c"]}]
            root = Stack([list])
            list = Stack(@Each(${'$'}groups, "g", Stack(@Each(g.items, "g", TextContent(g)))))
            """.trimIndent(),
            library = playground,
        )

        val texts = OpenUIJson.stringify(element(runtime, "list")?.let { OpenUIValue.Element(it).plainJson } ?: v(null))

        assertTrue("\"text\":\"a\"" in texts)
        assertTrue("\"text\":\"c\"" in texts)
    }

    @Test
    fun `declared defaults do not fire state updates`() = runTest {
        val updates = ArrayList<OpenUIObject>()
        val runtime = OpenUIRuntime(library, this)
        runtime.onStateUpdate = { updates += it }

        runtime.update("${'$'}a = 1\nroot = Card([TextContent(\"\" + ${'$'}a)])", isStreaming = false)
        assertTrue(updates.isEmpty())

        runtime.store["\$a"] = v(2)
        assertEquals(1, updates.size)
    }

    @Test
    fun `long reference chains do not overflow the stack`() = runTest {
        val count = 5_000
        val lines = listOf("root = Card([a0])") +
            (0 until count).map { "a$it = Card([a${it + 1}])" } +
            "a$count = TextContent(\"x\")"

        val runtime = runtime(lines.joinToString("\n"))

        assertEquals("Card", runtime.root?.typeName)
    }

    @Test
    fun `a query with an infinite refresh interval does not crash`() = runTest {
        val tools = FunctionToolProvider("list" to { _ -> obj("rows" to emptyList<Any>()) })
        val runtime = OpenUIRuntime(library, backgroundScope, tools)
        runtime.update("root = Card([])\nq = Query(\"list\", {}, {rows: []}, 1e999)", isStreaming = false)

        testScheduler.advanceTimeBy(50)

        assertNotNull(runtime.root)
    }
}
