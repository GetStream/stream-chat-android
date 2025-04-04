/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

@file:OptIn(ExperimentalMaterial3Api::class)

package io.getstream.chat.android.compose.ui.mentions

import androidx.compose.material3.ExperimentalMaterial3Api
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.SnapshotTest
import org.junit.Rule
import org.junit.Test

internal class MentionListTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_4A)

    @Test
    fun `loading mention list`() {
        snapshotWithDarkMode { darkMode ->
            MentionListLoading(darkMode = darkMode)
        }
    }

    @Test
    fun `empty mention list`() {
        snapshotWithDarkMode { darkMode ->
            MentionListEmpty(darkMode = darkMode)
        }
    }

    @Test
    fun `loaded mention list`() {
        snapshotWithDarkMode { darkMode ->
            MentionListLoaded(darkMode = darkMode)
        }
    }

    @Test
    fun `loading more mention list`() {
        snapshotWithDarkMode { darkMode ->
            MentionListLoadingMore(darkMode = darkMode)
        }
    }
}
