/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

internal class BaselineProfileGenerator {
    @get:Rule
    internal val baselineProfileRule = BaselineProfileRule()

    @Test
    fun startupComposeSample() =
        baselineProfileRule.collectBaselineProfile(
            packageName = COMPOSE_SAMPLE_PACKAGE_NAME,
        ) {
            pressHome()
            // This block defines the app's critical user journey. Here we are interested in
            // optimizing for app startup. But you can also navigate and scroll
            // through your most important UI.
            startActivityAndWait()
            device.waitForIdle()
        }

    @Test
    fun startupUiComponentsSample() =
        baselineProfileRule.collectBaselineProfile(
            packageName = UI_COMPONENTS_PACKAGE_NAME,
        ) {
            pressHome()
            // This block defines the app's critical user journey. Here we are interested in
            // optimizing for app startup. But you can also navigate and scroll
            // through your most important UI.
            startActivityAndWait()
            device.waitForIdle()
        }
}
