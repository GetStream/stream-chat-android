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

package io.getstream.chat.android.compose

import android.content.Context
import io.getstream.chat.android.compose.ui.attachments.preview.handler.MediaAttachmentPreviewHandler
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.randomString
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.mock

internal class MediaAttachmentPreviewHandlerTest {

    private val mediaAttachmentPreviewHandler = MediaAttachmentPreviewHandler(mock<Context>())

    /**
     * [generateAttachmentsInput]
     */
    @ParameterizedTest
    @MethodSource("generateAttachmentsInput")
    fun test(attachment: Attachment, expectedResult: Boolean) {
        mediaAttachmentPreviewHandler.canHandle(attachment) `should be equal to` expectedResult
    }

    companion object {

        @JvmStatic
        fun generateAttachmentsInput() = listOf(
            Arguments.of(Attachment(), false),
            Arguments.of(Attachment(assetUrl = randomString()), false),
            Arguments.of(Attachment(assetUrl = randomString(), type = randomString()), false),
            Arguments.of(
                Attachment(assetUrl = randomString(), type = randomString(), mimeType = randomString()),
                false,
            ),
            Arguments.of(
                Attachment(assetUrl = randomString(), type = AttachmentType.AUDIO),
                true,
            ),
            Arguments.of(
                Attachment(assetUrl = randomString(), type = AttachmentType.VIDEO),
                true,
            ),
        ) +
            listOf(
                AttachmentType.AUDIO,
                randomString() + AttachmentType.AUDIO,
                randomString() + AttachmentType.AUDIO + randomString(),
                AttachmentType.AUDIO + randomString(),
                AttachmentType.VIDEO,
                randomString() + AttachmentType.VIDEO,
                randomString() + AttachmentType.VIDEO + randomString(),
                AttachmentType.VIDEO + randomString(),
                "mpeg-3",
                "x-mpeg3",
                "mp3", "mpeg",
                "x-mpeg",
                "aac",
                "webm",
                "wav",
                "x-wav",
                "flac",
                "x-flac",
                "ac3",
                "ogg", "x-ogg",
                "mp4",
                "x-m4a",
                "x-matroska",
                "vorbis",
                "quicktime",
            ).map {
                Arguments.of(Attachment(assetUrl = randomString(), type = randomString(), mimeType = it), true)
            }
    }
}
