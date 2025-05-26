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

package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.SnapshotTest
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.previewdata.PreviewMessageData
import org.junit.Rule
import org.junit.Test

internal class FileAttachmentFactoryTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    private val fileAttachment = Attachment(
        type = "file",
        name = "test_document.pdf",
        fileSize = 1024 * 1024, // 1MB
        mimeType = "application/pdf",
    )

    private val secondFileAttachment = Attachment(
        type = "file",
        name = "another_document.pdf",
        fileSize = 2 * 1024 * 1024, // 2MB
        mimeType = "application/pdf",
    )

    @Test
    fun `file attachment preview content`() {
        snapshotWithDarkMode {
            val attachments = listOf(fileAttachment, secondFileAttachment)
            val factory: AttachmentFactory = FileAttachmentFactory()
            factory.previewContent?.invoke(Modifier.fillMaxSize(), attachments, {})
        }
    }

    @Test
    fun `file attachment content`() {
        snapshotWithDarkMode {
            val attachmentState = AttachmentState(
                message = PreviewMessageData.message1.copy(
                    attachments = mutableListOf(fileAttachment),
                ),
            )
            val factory: AttachmentFactory = FileAttachmentFactory()
            factory.content(Modifier.width(ChatTheme.dimens.attachmentsContentFileWidth), attachmentState)
        }
    }

    @Test
    fun `multiple file attachments content`() {
        snapshotWithDarkMode {
            val attachmentState = AttachmentState(
                message = PreviewMessageData.message1.copy(
                    attachments = mutableListOf(fileAttachment, secondFileAttachment),
                ),
            )
            val factory: AttachmentFactory = FileAttachmentFactory()
            factory.content(Modifier.width(ChatTheme.dimens.attachmentsContentFileWidth), attachmentState)
        }
    }
}
