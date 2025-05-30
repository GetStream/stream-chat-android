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

import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.SnapshotTest
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.previewdata.PreviewMessageData
import org.junit.Rule
import org.junit.Test

internal class UploadAttachmentFactoryTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    private val uploadingAttachment = Attachment(
        type = "file",
        name = "test_document.pdf",
        fileSize = 1024 * 1024, // 1MB
        mimeType = "application/pdf",
        uploadState = Attachment.UploadState.InProgress(
            bytesUploaded = 512 * 1024, // 512KB uploaded
            totalBytes = 1024 * 1024, // 1MB total
        ),
    )

    private val secondUploadingAttachment = Attachment(
        type = "image",
        name = "test_image.jpg",
        fileSize = 2 * 1024 * 1024, // 2MB
        mimeType = "image/jpeg",
        uploadState = Attachment.UploadState.InProgress(
            bytesUploaded = 1024 * 1024, // 1MB uploaded
            totalBytes = 2 * 1024 * 1024, // 2MB total
        ),
    )

    @Test
    fun `single uploading attachment content`() {
        snapshotWithDarkMode {
            val attachmentState = AttachmentState(
                message = PreviewMessageData.message1.copy(
                    attachments = mutableListOf(uploadingAttachment),
                ),
            )
            val factory: AttachmentFactory = UploadAttachmentFactory()
            factory.content(Modifier.wrapContentHeight(), attachmentState)
        }
    }

    @Test
    fun `multiple uploading attachments content`() {
        snapshotWithDarkMode {
            val attachmentState = AttachmentState(
                message = PreviewMessageData.message1.copy(
                    attachments = mutableListOf(uploadingAttachment, secondUploadingAttachment),
                ),
            )
            val factory: AttachmentFactory = UploadAttachmentFactory()
            factory.content(Modifier.wrapContentHeight(), attachmentState)
        }
    }
}
