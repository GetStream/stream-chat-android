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

package io.getstream.chat.android.benchmark

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import io.getstream.chat.android.benchmark.scenario.composeSampleScenarios
import org.junit.Rule
import org.junit.Test

@RequiresApi(Build.VERSION_CODES.P)
internal class BaselineProfileGenerator {
    @get:Rule
    internal val baselineProfileRule = BaselineProfileRule()

    @Test
    fun startupComposeSample() =
        baselineProfileRule.collect(
            packageName = COMPOSE_SAMPLE_PACKAGE_NAME,
            stableIterations = 2,
            maxIterations = 8,
            includeInStartupProfile = true,
        ) {
            composeSampleScenarios()
        }
}
