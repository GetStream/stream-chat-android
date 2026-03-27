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

package io.getstream.chat.android.compose.ui.channel.info

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import org.junit.Rule
import org.junit.Test

internal class AddMembersScreenTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun loading() {
        snapshot { AddMembersScreenLoading() }
    }

    @Test
    fun `loading in dark mode`() {
        snapshot(isInDarkMode = true) { AddMembersScreenLoading() }
    }

    @Test
    fun empty() {
        snapshot { AddMembersScreenEmpty() }
    }

    @Test
    fun `empty in dark mode`() {
        snapshot(isInDarkMode = true) { AddMembersScreenEmpty() }
    }

    @Test
    fun `results with query`() {
        snapshot { AddMembersScreenResultsWithQuery() }
    }

    @Test
    fun `results with query in dark mode`() {
        snapshot(isInDarkMode = true) { AddMembersScreenResultsWithQuery() }
    }

    @Test
    fun results() {
        snapshot { AddMembersScreenResults() }
    }

    @Test
    fun `results in dark mode`() {
        snapshot(isInDarkMode = true) { AddMembersScreenResults() }
    }

    @Test
    fun `results with selection`() {
        snapshot { AddMembersScreenResultsWithSelection() }
    }

    @Test
    fun `results with selection in dark mode`() {
        snapshot(isInDarkMode = true) { AddMembersScreenResultsWithSelection() }
    }

    @Test
    fun `results with existing member`() {
        snapshot { AddMembersScreenResultsWithMember() }
    }

    @Test
    fun `results with existing member in dark mode`() {
        snapshot(isInDarkMode = true) { AddMembersScreenResultsWithMember() }
    }

    @Test
    fun `loading more`() {
        snapshot { AddMembersScreenLoadingMore() }
    }

    @Test
    fun `loading more in dark mode`() {
        snapshot(isInDarkMode = true) { AddMembersScreenLoadingMore() }
    }
}
