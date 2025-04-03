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

package io.getstream.chat.android.client.utils.attachment

import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.randomAttachment
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class AttachmentUtilsTests {

    @Test
    fun `isImage should return true for image attachment`() {
        val attachment = randomAttachment(type = AttachmentType.IMAGE)
        attachment.isImage() shouldBeEqualTo true
    }

    @Test
    fun `isImage should return false for non-image attachment`() {
        val attachment = randomAttachment(type = AttachmentType.FILE)
        attachment.isImage() shouldBeEqualTo false
    }

    @Test
    fun `isVideo should return true for video attachment`() {
        val attachment = randomAttachment(type = AttachmentType.VIDEO)
        attachment.isVideo() shouldBeEqualTo true
    }

    @Test
    fun `isVideo should return false for non-video attachment`() {
        val attachment = randomAttachment(type = AttachmentType.IMAGE)
        attachment.isVideo() shouldBeEqualTo false
    }

    @Test
    fun `isAudio should return true for audio attachment`() {
        val attachment = randomAttachment(type = AttachmentType.AUDIO)
        attachment.isAudio() shouldBeEqualTo true
    }

    @Test
    fun `isAudio should return false for non-audio attachment`() {
        val attachment = randomAttachment(type = AttachmentType.FILE)
        attachment.isAudio() shouldBeEqualTo false
    }

    @Test
    fun `isFile should return true for file attachment`() {
        val attachment = randomAttachment(type = AttachmentType.FILE)
        attachment.isFile() shouldBeEqualTo true
    }

    @Test
    fun `isFile should return false for non-file attachment`() {
        val attachment = randomAttachment(type = AttachmentType.IMAGE)
        attachment.isFile() shouldBeEqualTo false
    }

    @Test
    fun `isGiphy should return true for giphy attachment`() {
        val attachment = randomAttachment(type = AttachmentType.GIPHY)
        attachment.isGiphy() shouldBeEqualTo true
    }

    @Test
    fun `isGiphy should return false for non-giphy attachment`() {
        val attachment = randomAttachment(type = AttachmentType.IMAGE)
        attachment.isGiphy() shouldBeEqualTo false
    }

    @Test
    fun `isImgur should return true for imgur attachment`() {
        val attachment = randomAttachment(type = AttachmentType.IMGUR)
        attachment.isImgur() shouldBeEqualTo true
    }

    @Test
    fun `isImgur should return false for non-imgur attachment`() {
        val attachment = randomAttachment(type = AttachmentType.IMAGE)
        attachment.isImgur() shouldBeEqualTo false
    }

    @Test
    fun `isLink should return true for link attachment`() {
        val attachment = randomAttachment(type = AttachmentType.LINK)
        attachment.isLink() shouldBeEqualTo true
    }

    @Test
    fun `isLink should return false for non-link attachment`() {
        val attachment = randomAttachment(type = AttachmentType.IMAGE)
        attachment.isLink() shouldBeEqualTo false
    }

    @Test
    fun `isAudioRecording should return true for audio recording attachment`() {
        val attachment = randomAttachment(type = AttachmentType.AUDIO_RECORDING)
        attachment.isAudioRecording() shouldBeEqualTo true
    }

    @Test
    fun `isAudioRecording should return false for non-audio recording attachment`() {
        val attachment = randomAttachment(type = AttachmentType.IMAGE)
        attachment.isAudioRecording() shouldBeEqualTo false
    }
}
