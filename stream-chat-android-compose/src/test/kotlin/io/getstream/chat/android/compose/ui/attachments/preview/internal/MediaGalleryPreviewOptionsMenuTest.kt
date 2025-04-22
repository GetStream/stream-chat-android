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

package io.getstream.chat.android.compose.ui.attachments.preview.internal

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.SnapshotTest
import io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryOptionsConfig
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewUserData
import org.junit.Rule
import org.junit.Test

internal class MediaGalleryPreviewOptionsMenuTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `media gallery options menu for own user`() = snapshotWithDarkMode {
        val message = PreviewMessageData.messageWithUserAndAttachment
        MediaGalleryOptionsMenu(
            attachment = message.attachments[0],
            options = defaultMediaOptions(
                currentUser = message.user,
                message = message,
                connectionState = ConnectionState.Connected,
                config = MediaGalleryOptionsConfig(),
            ),
            onOptionClick = { _, _ -> },
            onDismiss = {},
        )
    }

    @Test
    fun `media gallery options menu for other user`() = snapshotWithDarkMode {
        val message = PreviewMessageData.messageWithUserAndAttachment
        val user = PreviewUserData.user1
        MediaGalleryOptionsMenu(
            attachment = message.attachments[0],
            options = defaultMediaOptions(
                currentUser = user,
                message = message,
                connectionState = ConnectionState.Connected,
                config = MediaGalleryOptionsConfig(),
            ),
            onOptionClick = { _, _ -> },
            onDismiss = {},
        )
    }
}
