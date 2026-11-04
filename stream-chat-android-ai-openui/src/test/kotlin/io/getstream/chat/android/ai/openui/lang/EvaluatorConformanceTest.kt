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
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Reactive props derived from upstream signatures (`value?: $binding<string>`). */
internal class SignatureReactiveResolver(spec: OpenUIValue) : ReactivePropResolver {
    private val reactive: Set<String> = buildSet {
        val pattern = Regex("(\\w+)\\??: \\\$binding<")
        for ((name, component) in spec["components"].objectValue.orEmpty()) {
            val signature = component["signature"].stringValue.orEmpty()
            pattern.findAll(signature).forEach { add("$name.${it.groupValues[1]}") }
        }
    }

    override fun isReactiveProp(component: String, prop: String): Boolean = "$component.$prop" in reactive
}

/** Differential tests of the evaluator against `evaluateElementProps` / `evaluate` in the reference. */
internal class EvaluatorConformanceTest {

    class Case(
        val name: String,
        val library: String,
        val input: String,
        val state: OpenUIObject,
        val refs: OpenUIObject,
        val evaluatedRoot: OpenUIValue,
        val queries: OpenUIValue,
        val runtimeErrors: OpenUIValue,
    )

    companion object {
        val cases: List<Case> by lazy {
            Fixtures.json("evaluator-conformance.json").arrayValue!!.map { item ->
                Case(
                    name = item["name"].stringValue!!,
                    library = item["library"].stringValue!!,
                    input = item["input"].stringValue!!,
                    state = item["state"].objectValue ?: OpenUIObject.Empty,
                    refs = item["refs"].objectValue ?: OpenUIObject.Empty,
                    evaluatedRoot = item["evaluatedRoot"] ?: OpenUIValue.Null,
                    queries = item["queries"] ?: OpenUIValue.EmptyArray,
                    runtimeErrors = item["runtimeErrors"] ?: OpenUIValue.EmptyArray,
                )
            }
        }

        val resolvers: Map<String, SignatureReactiveResolver> by lazy {
            mapOf(
                "chat" to SignatureReactiveResolver(Fixtures.json("upstream-chat-library.spec.json")),
                "playground" to SignatureReactiveResolver(Fixtures.json("upstream-playground-library.spec.json")),
                "spec" to SignatureReactiveResolver(Fixtures.json("spec-library.json")),
            )
        }

        // The spec library's function and action, as upstream's conformance runner supplies them.
        val specFunctions = mapOf(
            "Upper" to DefinedFunction(
                name = "Upper",
                description = "The text in upper case",
                params = listOf(PropField.required("text", PropType.string)),
                returns = PropType.string,
            ) { args -> OpenUIValue.Str(JS.toStringOrEmpty(args["text"]).uppercase()) },
        )
        val specActions = mapOf(
            "CopyToClipboard" to DefinedAction(
                name = "CopyToClipboard",
                description = "Copies text to the clipboard",
                params = listOf(PropField.required("text", PropType.string)),
            ),
        )
    }

    @Test
    fun `fixtures are loaded`() {
        assertTrue(cases.size > 180)
        assertEquals(3, resolvers.size)
    }

    @TestFactory
    fun `evaluation matches reference`(): List<DynamicTest> = cases.map { case ->
        DynamicTest.dynamicTest(case.name) {
            val (paramMap, rootName) = Fixtures.libraries.getValue(case.library)
            val resolver = resolvers.getValue(case.library)
            val result = Parser(paramMap, rootName).parse(case.input)

            val store = result.stateDeclarations + case.state
            val context = EvaluationContext(
                getState = { name ->
                    val value = store[name]
                    value.objectValue?.get("value") ?: value
                },
                resolveRef = { case.refs[it] },
                functions = if (case.library == "spec") specFunctions else emptyMap(),
                actions = if (case.library == "spec") specActions else emptyMap(),
            )

            val (evaluatedRoot, runtimeErrors) = PropEvaluator.evaluateRoot(result, context, resolver)
            val actualRoot = evaluatedRoot?.let { OpenUIValue.Element(it).plainJson } ?: OpenUIValue.Null
            if (case.name == "expressions") {
                // Guard against vacuous passes: the expression case must render real values.
                assertTrue((actualRoot["props"]["children"].arrayValue?.size ?: 0) >= 100)
                assertEquals(OpenUIValue.Str("0.10.2"), actualRoot["props"]["children"][0]["props"]["text"])
            }
            assertNull(firstDifference(actualRoot, case.evaluatedRoot), case.name)

            fun eval(node: ASTNode?): OpenUIValue =
                node?.let { Evaluator.evaluate(it, context)?.plainJson } ?: OpenUIValue.Null
            val queries = OpenUIValue.Arr(
                result.queryStatements.map { query ->
                    OpenUIValue.obj(
                        "statementId" to OpenUIValue.Str(query.statementId),
                        "toolName" to (query.toolAST?.let { eval(it) } ?: OpenUIValue.Str("")),
                        "args" to eval(query.argsAST),
                        "defaults" to eval(query.defaultsAST),
                        "refresh" to eval(query.refreshAST),
                    )
                },
            )
            assertNull(firstDifference(queries, case.queries), "${case.name} queries")

            val errors = OpenUIValue.Arr(
                runtimeErrors.map { error ->
                    OpenUIValue.obj(
                        "source" to OpenUIValue.Str(error.source.wireName),
                        "code" to OpenUIValue.Str(error.code),
                        "component" to error.component?.let { OpenUIValue.Str(it) },
                        "statementId" to error.statementId?.let { OpenUIValue.Str(it) },
                        "message" to OpenUIValue.Str(error.message),
                        "hint" to error.hint?.let { OpenUIValue.Str(it) },
                    )
                },
            )
            assertNull(firstDifference(errors, case.runtimeErrors), "${case.name} runtime errors")
        }
    }
}
