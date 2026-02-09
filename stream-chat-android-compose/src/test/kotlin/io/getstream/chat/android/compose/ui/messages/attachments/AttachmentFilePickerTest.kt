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

package io.getstream.chat.android.compose.ui.messages.attachments

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.compose.ui.util.ViewModelStore
import io.getstream.chat.android.test.TestCoroutineRule
import org.junit.Rule
import org.junit.Test

internal class AttachmentFilePickerTest : PaparazziComposeTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `single selection`() {
        snapshotWithDarkMode {
            ViewModelStore {
                AttachmentFilePickerSingleSelection()
            }
        }
    }

    @Test
    fun `multiple selection`() {
        snapshotWithDarkMode {
            ViewModelStore {
                AttachmentFilePickerMultipleSelection()
            }
        }
    }
}
