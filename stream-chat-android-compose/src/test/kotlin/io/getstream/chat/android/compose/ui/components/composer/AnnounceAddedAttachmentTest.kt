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

package io.getstream.chat.android.compose.ui.components.composer

import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomString
import org.junit.Assert.assertEquals
import org.junit.Test

internal class AnnounceAddedAttachmentTest {

    private val photoAttached = randomString()
    private val videoAttached = randomString()
    private val audioAttached = randomString()
    private val fileAttached = randomString()

    @Test
    fun `returns empty string when attachment is null`() {
        val result = announceAddedAttachment(
            added = null,
            photoAttached = photoAttached,
            videoAttached = videoAttached,
            audioAttached = audioAttached,
            fileAttached = fileAttached,
        )

        assertEquals("", result)
    }

    @Test
    fun `returns photoAttached label for an image attachment`() {
        val result = announceAddedAttachment(
            added = randomAttachment(type = AttachmentType.IMAGE),
            photoAttached = photoAttached,
            videoAttached = videoAttached,
            audioAttached = audioAttached,
            fileAttached = fileAttached,
        )

        assertEquals(photoAttached, result)
    }

    @Test
    fun `returns videoAttached label for a video attachment`() {
        val result = announceAddedAttachment(
            added = randomAttachment(type = AttachmentType.VIDEO),
            photoAttached = photoAttached,
            videoAttached = videoAttached,
            audioAttached = audioAttached,
            fileAttached = fileAttached,
        )

        assertEquals(videoAttached, result)
    }

    @Test
    fun `returns audioAttached label for an audio recording attachment`() {
        val result = announceAddedAttachment(
            added = randomAttachment(type = AttachmentType.AUDIO_RECORDING),
            photoAttached = photoAttached,
            videoAttached = videoAttached,
            audioAttached = audioAttached,
            fileAttached = fileAttached,
        )

        assertEquals(audioAttached, result)
    }

    @Test
    fun `returns fileAttached label for any other attachment type`() {
        val result = announceAddedAttachment(
            added = randomAttachment(type = AttachmentType.FILE),
            photoAttached = photoAttached,
            videoAttached = videoAttached,
            audioAttached = audioAttached,
            fileAttached = fileAttached,
        )

        assertEquals(fileAttached, result)
    }
}
