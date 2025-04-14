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

package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.layout.Column
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.ui.SnapshotTest
import org.junit.Rule
import org.junit.Test

internal class AttachmentsContentTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_2,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `file attachment content`() {
        snapshotWithDarkMode { darkMode ->
            Column {
                FileAttachmentContent(
                    darkMode = darkMode,
                    isMine = true,
                )
                FileAttachmentContent(
                    darkMode = darkMode,
                    isMine = false,
                )
            }
        }
    }

    @Test
    fun `link attachment content`() {
        snapshotWithDarkMode { darkMode ->
            LinkAttachmentContent(
                darkMode = darkMode,
            )
        }
    }

    @Test
    fun `image attachment preview content`() {
        snapshotWithDarkModeRow { darkMode ->
            ImageAttachmentPreviewContent(
                darkMode = darkMode,
            )
        }
    }

    @Test
    fun `media attachment preview items`() {
        snapshotWithDarkModeRow { darkMode ->
            MediaAttachmentPreviewItems(
                darkMode = darkMode,
            )
        }
    }

    @Test
    fun `file attachment quoted content`() {
        snapshotWithDarkMode { darkMode ->
            FileAttachmentQuotedContent(
                darkMode = darkMode,
            )
        }
    }
}
