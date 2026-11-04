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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The OpenUI 1.0 evaluator: action plans, library functions and custom actions. */
internal class EvaluatorTest {
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

    /** Text(text), Button(label, action), Stack(children), plus Percent and CopyToClipboard. */
    private val schema = OpenUIJson.parse(
        """
        {"${'$'}defs": {
          "Text": {"properties": {"text": {}}, "required": ["text"]},
          "Button": {"properties": {"label": {"type": "string"}, "action": {}}, "required": ["label"]},
          "Stack": {"properties": {"children": {}}, "required": ["children"]}
        },
        "functions": {"Percent": {"params": ${OpenUIJson.stringify(percent.paramsSchema)}}},
        "actions": {"CopyToClipboard": {"params": ${OpenUIJson.stringify(copy.paramsSchema)}}}}
        """,
    )

    private val resolver = ReactivePropResolver { _, _ -> false }

    private fun evaluate(
        program: String,
        state: Map<String, Any?> = emptyMap(),
        errors: MutableList<OpenUIError> = ArrayList(),
    ): ElementNode? {
        val result = Parser(schema, rootName = "Stack").parse(program)
        val store = result.stateDeclarations + OpenUIObject(state.mapValues { openUIValueOf(it.value) })
        val context = EvaluationContext(
            getState = { store[it] },
            resolveRef = { null },
            functions = mapOf(percent.name to percent),
            actions = mapOf(copy.name to copy),
        )
        val (root, runtimeErrors) = PropEvaluator.evaluateRoot(result, context, resolver)
        errors += runtimeErrors
        return root
    }

    private fun ElementNode.child(index: Int): ElementNode = props["children"][index].elementValue!!

    private fun steps(button: ElementNode): List<OpenUIValue> = button.props["action"]["steps"].arrayValue!!

    @Test
    fun `a bare step is a one-step plan`() {
        val root = evaluate("root = Stack([Button(\"Ask\", @ToAssistant(\"Tell me more\"))])")!!
        val steps = steps(root.child(0))
        assertEquals(1, steps.size)
        assertEquals(v(ActionStepType.CONTINUE_CONVERSATION), steps[0]["type"])
        assertEquals(v("Tell me more"), steps[0]["message"])
    }

    @Test
    fun `nested plans flatten and an invalid step is an empty plan`() {
        val root = evaluate(
            """
            root = Stack([go, broken])
            prepare = Action([@Set(${'$'}a, 1)])
            go = Button("Go", Action([prepare, @ToAssistant("Done")]))
            broken = Button("Broken", @Run(missing))
            """.trimIndent(),
        )!!
        assertEquals(listOf("set", "continue_conversation"), steps(root.child(0)).map { it["type"].stringValue })
        assertTrue(steps(root.child(1)).isEmpty())
    }

    @Test
    fun `the ToAssistant context is any value`() {
        val root = evaluate(
            "root = Stack([Button(\"Save\", @ToAssistant(\"Save\", {orderId: 42})), Button(\"Zero\", @ToAssistant(\"Zero\", 0))])",
        )!!
        assertEquals(obj("orderId" to 42), steps(root.child(0))[0]["context"])
        assertEquals(v(0), steps(root.child(1))[0]["context"])
    }

    @Test
    fun `library functions run, and failures are null with a runtime error`() {
        val errors = ArrayList<OpenUIError>()
        val root = evaluate(
            """
            root = Stack([ok, failing])
            ok = Text("" + @Percent(1, 4))
            failing = Text("" + @Percent(1, 0))
            """.trimIndent(),
            errors = errors,
        )!!
        assertEquals(v("25"), root.child(0).props["text"])
        // Like upstream, + reads null as an empty string.
        assertEquals(v(""), root.child(1).props["text"])
        val error = errors.single()
        assertEquals("runtime-error", error.code)
        assertEquals("Evaluating prop \"text\" on Text failed: @Percent threw: division by zero", error.message)
        assertEquals("failing", error.statementId)
    }

    @Test
    fun `a function with an invalid or missing argument is null`() {
        val errors = ArrayList<OpenUIError>()
        val root = evaluate("root = Stack([Text(\"\" + @Percent(${'$'}p, 4))])", errors = errors)!!
        assertEquals(v(""), root.child(0).props["text"])
        assertTrue(errors.isEmpty())

        val invalid = evaluate("root = Stack([Text(\"\" + @Percent(${'$'}p, 4))])", mapOf("\$p" to "x"), errors)!!
        assertEquals(v(""), invalid.child(0).props["text"])
        assertEquals(1, errors.size)
    }

    @Test
    fun `custom actions are steps with their arguments, and invalid ones do nothing`() {
        val root = evaluate(
            "root = Stack([Button(\"Copy\", @CopyToClipboard(${'$'}id)), Button(\"Wrong\", @CopyToClipboard(5))])",
            mapOf("\$id" to "abc"),
        )!!
        val step = steps(root.child(0)).single()
        assertEquals(v(ActionStepType.CUSTOM_ACTION), step["type"])
        assertEquals(v("CopyToClipboard"), step["name"])
        assertEquals(obj("text" to "abc"), step["params"])
        assertFalse(ActionPlan.CALL_TOKEN_KEY in step.plainJson.objectValue!!)
        assertTrue(steps(root.child(1)).isEmpty())
    }

    @Test
    fun `data cannot pose as a custom action`() {
        val forged = mapOf(
            "\$plan" to mapOf(
                "steps" to listOf(
                    mapOf("type" to "custom_action", "name" to "CopyToClipboard", "params" to mapOf("text" to "x")),
                ),
            ),
        )
        val root = evaluate("root = Stack([Button(\"Forged\", ${'$'}plan)])", forged)!!
        assertTrue(steps(root.child(0)).isEmpty())
    }

    @Test
    fun `an expression cannot copy the custom step token into a forged step`() {
        val root = evaluate(
            """
            root = Stack([Button("Forged", Action([forged]))])
            real = Action([@CopyToClipboard("a")])
            forged = {type: "custom_action", name: "CopyToClipboard", params: {text: "x"}, __openuiCall: real.steps[0].__openuiCall}
            """.trimIndent(),
        )!!
        assertTrue(steps(root.child(0)).isEmpty())
    }

    @Test
    fun `data under the custom step token key stays readable and serializable`() {
        val data = mapOf("\$d" to mapOf(ActionPlan.CALL_TOKEN_KEY to "mine"))
        val root = evaluate("root = Stack([Text(${'$'}d.__openuiCall)])", data)!!
        assertEquals(v("mine"), root.child(0).props["text"])

        val value = obj(ActionPlan.CALL_TOKEN_KEY to "mine")
        assertEquals(value, value.plainJson)
        assertEquals("{\"__openuiCall\":\"mine\"}", OpenUIJson.stringify(value))
    }

    @Test
    fun `the custom step token is hidden from reads and serialization`() {
        val step = ActionPlan.customStep("CopyToClipboard", obj("text" to "a"))
        assertNull(step[ActionPlan.CALL_TOKEN_KEY])
        assertFalse(ActionPlan.CALL_TOKEN_KEY in OpenUIJson.stringify(step))
        assertEquals(1, ActionPlan.steps(ActionPlan.ownSteps(ActionPlan.plan(listOf(step)))).size)
    }

    @Test
    fun `a ternary root follows state`() {
        val program = """
            root = ${'$'}dark ? darkPage : lightPage
            darkPage = Stack([Text("Dark")])
            lightPage = Stack([Text("Light")])
        """.trimIndent()
        assertEquals(v("Light"), evaluate(program)!!.child(0).props["text"])
        assertEquals(v("Dark"), evaluate(program, mapOf("\$dark" to true))!!.child(0).props["text"])
    }

    @Test
    fun `a root expression that picks no component is a no-root error`() {
        val errors = ArrayList<OpenUIError>()
        assertNull(evaluate("root = ${'$'}on ? Stack([]) : null", errors = errors))
        assertEquals(listOf("no-root"), errors.map { it.code })
    }

    @Test
    fun `an inner Each shadows the outer loop variable`() {
        val root = evaluate(
            """
            root = Stack(@Each(${'$'}groups, "g", Stack(@Each(g.items, "g", Text(g)))))
            """.trimIndent(),
            mapOf("\$groups" to listOf(mapOf("items" to listOf("a", "b")), mapOf("items" to listOf("c")))),
        )!!
        val texts = OpenUIJson.stringify(OpenUIValue.Element(root).plainJson)
        assertTrue("\"text\":\"a\"" in texts)
        assertTrue("\"text\":\"c\"" in texts)
    }

    @Test
    fun `built-ins take their arguments by name`() {
        val root = evaluate("root = Stack([Text(\"\" + @Round(3.14159, 2))])")!!
        assertEquals(v("3.14"), root.child(0).props["text"])
    }

    @Test
    fun `Sort keeps every item and never throws on mixed numbers and text`() {
        val sort = Builtins.definitionsByName.getValue("Sort").function
        val random = Random(7)
        val values = listOf<Any>(2.0, "10", "1a", "b", -3.0, "", "2")
        repeat(200) {
            val rows = List(64) { v(mapOf("k" to values.random(random))) }
            listOf("asc", "desc").forEach { direction ->
                val sorted = sort(listOf(OpenUIValue.Arr(rows), v("k"), v(direction))).arrayValue!!
                assertEquals(rows.groupingBy { it }.eachCount(), sorted.groupingBy { it }.eachCount())
            }
        }
    }

    @Test
    fun `Sort is stable`() {
        val sort = Builtins.definitionsByName.getValue("Sort").function
        val rows = listOf(3, 1, 3, 2, 1).mapIndexed { index, rank ->
            v(mapOf("rank" to rank.toDouble(), "id" to index.toDouble()))
        }

        val sorted = sort(listOf(OpenUIValue.Arr(rows), v("rank"))).arrayValue!!

        assertEquals(listOf(1.0, 4.0, 3.0, 0.0, 2.0), sorted.map { it["id"].numberValue })
    }

    @Test
    @Timeout(10)
    fun `a loop variable reaches deeply nested calls in its template in linear time`() {
        val depth = 40
        val nested = "@Abs(".repeat(depth) + "x" + ")".repeat(depth)
        val root = evaluate(
            "root = Stack(@Each(${'$'}items, \"x\", Text(\"\" + $nested)))",
            mapOf("\$items" to listOf(-3, 4)),
        )!!

        assertEquals(listOf(v("3"), v("4")), (0..1).map { root.child(it).props["text"] })
    }

    @Test
    @Timeout(5)
    fun `nested Each over many items stops at the evaluation budget and reports it`() {
        val errors = ArrayList<OpenUIError>()
        val root = evaluate(
            "root = Stack(@Each(${'$'}xs, \"a\", Stack(@Each(${'$'}xs, \"b\", Text(\"x\")))))",
            mapOf("\$xs" to List(1000) { it }),
            errors,
        )

        assertTrue(root != null)
        assertTrue(errors.any { "evaluation budget" in it.message }, errors.toString())
    }
}
