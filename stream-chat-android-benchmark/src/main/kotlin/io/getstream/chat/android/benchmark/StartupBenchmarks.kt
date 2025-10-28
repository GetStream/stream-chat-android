/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.benchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import io.getstream.chat.android.benchmark.scenario.composeSampleScenarios
import org.junit.Rule
import org.junit.Test

class StartupBenchmarks {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun startupCompilationNone() = benchmark(CompilationMode.None())

    @Test
    fun startupCompilationBaselineProfiles() = benchmark(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun benchmark(compilationMode: CompilationMode) {
        rule.measureRepeated(
            packageName = COMPOSE_SAMPLE_PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            startupMode = null,
            iterations = 10,
            setupBlock = {
                pressHome()
            },
            measureBlock = {
                composeSampleScenarios()
            },
        )
    }
}
