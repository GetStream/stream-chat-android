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

internal class AddMembersBottomSheetTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun loading() {
        snapshot { AddMembersBottomSheetLoading() }
    }

    @Test
    fun `loading in dark mode`() {
        snapshot(isInDarkMode = true) { AddMembersBottomSheetLoading() }
    }

    @Test
    fun empty() {
        snapshot { AddMembersBottomSheetEmpty() }
    }

    @Test
    fun `empty in dark mode`() {
        snapshot(isInDarkMode = true) { AddMembersBottomSheetEmpty() }
    }

    @Test
    fun `results with query`() {
        snapshot { AddMembersBottomSheetResultsWithQuery() }
    }

    @Test
    fun `results with query in dark mode`() {
        snapshot(isInDarkMode = true) { AddMembersBottomSheetResultsWithQuery() }
    }

    @Test
    fun results() {
        snapshot { AddMembersBottomSheetResults() }
    }

    @Test
    fun `results in dark mode`() {
        snapshot(isInDarkMode = true) { AddMembersBottomSheetResults() }
    }

    @Test
    fun `results with selection`() {
        snapshot { AddMembersBottomSheetResultsWithSelection() }
    }

    @Test
    fun `results with selection in dark mode`() {
        snapshot(isInDarkMode = true) { AddMembersBottomSheetResultsWithSelection() }
    }

    @Test
    fun `results with existing member`() {
        snapshot { AddMembersBottomSheetResultsWithMember() }
    }

    @Test
    fun `results with existing member in dark mode`() {
        snapshot(isInDarkMode = true) { AddMembersBottomSheetResultsWithMember() }
    }

    @Test
    fun `loading more`() {
        snapshot { AddMembersBottomSheetLoadingMore() }
    }

    @Test
    fun `loading more in dark mode`() {
        snapshot(isInDarkMode = true) { AddMembersBottomSheetLoadingMore() }
    }
}
