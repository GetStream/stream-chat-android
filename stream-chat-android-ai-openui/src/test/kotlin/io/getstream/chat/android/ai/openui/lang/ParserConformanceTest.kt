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

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Differential tests against the reference TypeScript implementation (`@openuidev/lang-core`).
 *
 * `fixtures/parser-conformance.json` was produced by running the upstream parser over a corpus
 * of programs (the OpenUI docs and prompt examples, the benchmark samples, hand-written edge cases
 * and the OpenUI 1.0 cases). Each case records the one-shot `parse()` result, snapshots of the
 * streaming parser while the input is pushed in pseudo-random chunks, and the result once the
 * stream completes.
 *
 * Results are compared in upstream's shape ([upstreamShape]): production output refers to a
 * call's args from its mapped props instead of copying them.
 */
internal class ParserConformanceTest {

    class Case(
        val name: String,
        val library: String,
        val input: String,
        val result: OpenUIValue,
        val chunks: List<String>,
        val checkpoints: List<Int>,
        val streamResults: List<OpenUIValue>,
        val completeResult: OpenUIValue,
    )

    companion object {
        val cases: List<Case> by lazy {
            Fixtures.json("parser-conformance.json").arrayValue!!.map { item ->
                Case(
                    name = item["name"].stringValue!!,
                    library = item["library"].stringValue!!,
                    input = item["input"].stringValue!!,
                    result = item["result"]!!,
                    chunks = item["chunks"].arrayValue!!.map { it.stringValue!! },
                    checkpoints = item["checkpoints"].arrayValue!!.map { it.numberValue!!.toInt() },
                    streamResults = item["streamResults"].arrayValue!!,
                    completeResult = item["completeResult"]!!,
                )
            }
        }
    }

    @Test
    fun `fixtures are loaded`() {
        assertTrue(cases.size >= 190)
        assertEquals(4, Fixtures.libraries.size)
    }

    @Test
    fun `comparison is not vacuous`() {
        // Most cases render a root, and a result never matches another program's expectation.
        assertTrue(cases.count { it.result["root"] !is OpenUIValue.Null } >= 80)
        val (paramMap, rootName) = Fixtures.libraries.getValue(cases[0].library)
        val first = upstreamShape(Parser(paramMap, rootName).parse(cases[0].input).jsonValue)
        assertNotNull(
            firstDifference(
                first,
                cases
                    .first {
                        it.library == cases[0].library && it.input != cases[0].input
                    }.result,
            ),
        )
    }

    @TestFactory
    fun `completed stream matches reference`(): List<DynamicTest> = cases.map { case ->
        DynamicTest.dynamicTest(case.name) {
            val (paramMap, rootName) = Fixtures.libraries.getValue(case.library)
            val parser = StreamParser(paramMap, rootName)
            var result = parser.getResult(complete = true)
            case.chunks.forEachIndexed { step, chunk ->
                result = parser.push(chunk, complete = step == case.chunks.lastIndex)
            }
            assertNull(firstDifference(upstreamShape(result.jsonValue), case.completeResult), case.name)
        }
    }

    @TestFactory
    fun `one shot parse matches reference`(): List<DynamicTest> = cases.map { case ->
        DynamicTest.dynamicTest(case.name) {
            val (paramMap, rootName) = Fixtures.libraries.getValue(case.library)
            val actual = upstreamShape(Parser(paramMap, rootName).parse(case.input).jsonValue)
            assertNull(firstDifference(actual, case.result), case.name)
        }
    }

    @TestFactory
    fun `streaming parse matches reference`(): List<DynamicTest> = cases.map { case ->
        DynamicTest.dynamicTest(case.name) {
            val (paramMap, rootName) = Fixtures.libraries.getValue(case.library)
            val parser = StreamParser(paramMap, rootName)
            var checkpoint = 0
            case.chunks.forEachIndexed { step, chunk ->
                val result = parser.push(chunk)
                if (checkpoint < case.checkpoints.size && case.checkpoints[checkpoint] == step) {
                    assertNull(
                        firstDifference(upstreamShape(result.jsonValue), case.streamResults[checkpoint]),
                        "${case.name} @ step $step",
                    )
                    checkpoint++
                }
            }
            assertEquals(case.checkpoints.size, checkpoint)
        }
    }
}
