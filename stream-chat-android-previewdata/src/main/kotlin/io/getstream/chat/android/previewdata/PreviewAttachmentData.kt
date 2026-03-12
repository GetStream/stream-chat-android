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

package io.getstream.chat.android.previewdata

import io.getstream.chat.android.client.extensions.EXTRA_DURATION
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType

@Suppress("MagicNumber")
@InternalStreamChatApi
public object PreviewAttachmentData {

    public val attachmentImage1: Attachment = Attachment(
        name = "image1.jpg",
        fileSize = 2000000,
        type = AttachmentType.IMAGE,
        mimeType = "image/jpeg",
        imageUrl = "https://example.com/image1.jpg",
    )

    public val attachmentImage2: Attachment = Attachment(
        name = "image2.jpg",
        fileSize = 3000000,
        type = AttachmentType.IMAGE,
        mimeType = "image/jpeg",
        imageUrl = "https://example.com/image2.jpg",
    )

    public val attachmentImage3: Attachment = Attachment(
        name = "image3.png",
        fileSize = 4000000,
        type = AttachmentType.IMAGE,
        mimeType = "image/png",
        imageUrl = "https://example.com/image3.png",
        extraData = mapOf(EXTRA_DURATION to 8),
    )

    public val attachmentVideo1: Attachment = Attachment(
        name = "video1.mp4",
        fileSize = 10000000,
        type = AttachmentType.VIDEO,
        mimeType = "video/mp4",
        thumbUrl = "https://example.com/thumb1.jpg",
        extraData = mapOf(EXTRA_DURATION to 8),
    )

    public val attachmentVideo2: Attachment = Attachment(
        name = "video2.mp4",
        fileSize = 20000000,
        type = AttachmentType.VIDEO,
        mimeType = "video/mp4",
        thumbUrl = "https://example.com/thumb2.jpg",
    )

    public val attachmentFile1: Attachment = Attachment(
        name = "document.pdf",
        fileSize = 1500000,
        type = AttachmentType.FILE,
        mimeType = "application/pdf",
    )

    public val attachmentAudioRecording1: Attachment = Attachment(
        type = AttachmentType.AUDIO_RECORDING,
        extraData = mutableMapOf(
            "waveform" to listOf(
                0.1f, 0.3f, 0.6f, 0.8f, 1.0f, 0.9f, 0.7f, 0.5f, 0.4f, 0.6f,
                0.8f, 0.7f, 0.5f, 0.3f, 0.4f, 0.6f, 0.9f, 1.0f, 0.8f, 0.6f,
                0.4f, 0.3f, 0.5f, 0.7f, 0.9f, 0.8f, 0.6f, 0.4f, 0.2f, 0.3f,
            ),
            "duration" to 15000,
        ),
    )
}
