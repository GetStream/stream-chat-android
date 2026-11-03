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
import io.getstream.chat.android.ai.openui.library.PropField
import io.getstream.chat.android.ai.openui.library.PropType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * The interaction cases of the OpenUI 1.0 conformance suite (`spec/fixtures/1.0`, level 2), run the
 * way upstream's `conformance.interactions.test.tsx` runs them: Buttons and Inputs are found by
 * statement name in their Form's scope, a click triggers the Button's action, and typing writes the
 * Input's field.
 */
internal class SpecInteractionTest {

    class Case(val name: String, val program: String, val interactions: List<OpenUIValue>, val expected: OpenUIValue)

    /** Tools that return scripted results, set per tool as the interactions go, and record calls. */
    private class ScriptedTools : ToolProvider {
        val results = LinkedHashMap<String, OpenUIValue>()
        val calls = ArrayList<OpenUIValue>()

        override suspend fun callTool(name: String, arguments: OpenUIObject): OpenUIValue {
            val result = results[name] ?: throw ToolNotFoundException(name, results.keys.sorted())
            calls += OpenUIValue.obj("tool" to v(name), "args" to OpenUIValue.Obj(arguments).plainJson)
            return result
        }
    }

    private class Control(val element: ElementNode, val formName: String?)

    companion object {
        val cases: List<Case> by lazy {
            Fixtures.json("spec-interactions.json").arrayValue!!.map { item ->
                Case(
                    name = item["name"].stringValue!!,
                    program = item["program"].stringValue!!,
                    interactions = item["interactions"].arrayValue.orEmpty(),
                    expected = item["expected"] ?: obj(),
                )
            }
        }

        /** The spec's test library: its schema as is, `Input.value` and `Select.value` bindable, its custom action. */
        val library: LibrarySchema by lazy {
            val spec = Fixtures.json("spec-library.json")
            LibrarySchema(
                jsonSchema = spec["schema"]!!,
                root = spec["root"].stringValue,
                reactiveProps = { component, prop -> prop == "value" && component in setOf("Input", "Select") },
                customActions = listOf(
                    DefinedAction(
                        name = "CopyToClipboard",
                        description = "Copies text to the clipboard",
                        params = listOf(PropField.required("text", PropType.string)),
                    ),
                ),
            )
        }
    }

    private fun controls(runtime: OpenUIRuntime): Map<String, Control> {
        val controls = LinkedHashMap<String, Control>()
        fun visit(value: OpenUIValue?, formName: String?) {
            when (value) {
                is OpenUIValue.Element -> {
                    val element = value.node
                    val scope = if (element.typeName == "Form") element.props["name"].stringValue else formName
                    element.statementId?.let { if (it !in controls) controls[it] = Control(element, formName) }
                    element.props.values.forEach { visit(it, scope) }
                }
                is OpenUIValue.Arr -> value.items.forEach { visit(it, formName) }
                else -> Unit
            }
        }
        runtime.root?.let { visit(OpenUIValue.Element(it), null) }
        return controls
    }

    private fun json(event: ActionEvent): OpenUIObject = OpenUIObject.of(
        "type" to v(event.type),
        "params" to OpenUIValue.Obj(event.params).plainJson,
        "humanFriendlyMessage" to v(event.humanFriendlyMessage),
        "formState" to (event.formState?.let { OpenUIValue.Obj(it).plainJson } ?: OpenUIValue.Null),
        "formName" to (event.formName?.let(::v) ?: OpenUIValue.Null),
    )

    @Test
    fun `cases are loaded`() {
        assertEquals(12, cases.size)
    }

    @TestFactory
    fun `interactions match the spec`(): List<DynamicTest> = cases.map { case ->
        DynamicTest.dynamicTest(case.name) { run(case) }
    }

    private fun run(case: Case) = runTest {
        val tools = ScriptedTools()
        val steps = ArrayDeque(case.interactions)
        while (steps.firstOrNull()?.get("tool").stringValue != null) {
            val step = steps.removeFirst()
            tools.results[step["tool"].stringValue!!] = step["result"] ?: OpenUIValue.Null
        }
        val events = ArrayList<ActionEvent>()
        val runtime = OpenUIRuntime(library, this, tools)
        runtime.onAction = { events += it }
        runtime.update(case.program, isStreaming = false)
        testScheduler.advanceUntilIdle()

        for (step in steps) {
            val tool = step["tool"].stringValue
            val click = step["click"].stringValue
            val typing = step["type"].arrayValue
            when {
                tool != null -> tools.results[tool] = step["result"] ?: OpenUIValue.Null
                click != null -> {
                    val control = assertNotNull(controls(runtime)[click], "${case.name}: nothing rendered for $click")
                    runtime.triggerAction(
                        label = control.element.props["label"].stringValue.orEmpty(),
                        formName = control.formName,
                        action = control.element.props["action"],
                    )
                }
                typing != null -> {
                    val id = typing.first().stringValue!!
                    val control = assertNotNull(controls(runtime)[id], "${case.name}: nothing rendered for $id")
                    runtime.stateField(
                        name = control.element.props["name"].stringValue.orEmpty(),
                        binding = control.element.props["value"],
                        formName = control.formName,
                        componentType = "input",
                    ).setValue(typing.getOrNull(1) ?: v(""))
                }
            }
            testScheduler.advanceUntilIdle()
        }
        runtime.dispose()

        val expectedEvents = case.expected["events"].arrayValue.orEmpty()
        assertEquals(expectedEvents.size, events.size, "${case.name}: events ${events.map(::json)}")
        for ((expected, actual) in expectedEvents.zip(events.map(::json))) {
            for ((key, value) in expected.objectValue.orEmpty()) {
                val difference = firstDifference(actual[key] ?: OpenUIValue.Null, value.plainJson)
                assertNull(difference, "${case.name}: event $key")
            }
        }
        for ((key, value) in case.expected["state"].objectValue.orEmpty()) {
            val actual = runtime.store[key]?.plainJson ?: OpenUIValue.Null
            assertNull(firstDifference(actual, value), "${case.name}: state $key is $actual")
        }
        case.expected["toolCalls"]?.let { expectedCalls ->
            val calls = tools.calls.filterIndexed { index, call -> index == 0 || call != tools.calls[index - 1] }
            assertNull(firstDifference(OpenUIValue.Arr(calls), expectedCalls), "${case.name}: tool calls $calls")
        }
    }
}
