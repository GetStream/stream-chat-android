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

package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.randomAttachment
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class AttachmentExtensionsTests {

    @Test
    fun `isImage should return true for image mime type`() {
        val attachment = randomAttachment(mimeType = "image/jpeg")
        attachment.isImage shouldBeEqualTo true
    }

    @Test
    fun `isImage should return false for non-image mime type`() {
        val attachment = randomAttachment(mimeType = "video/mp4")
        attachment.isImage shouldBeEqualTo false
    }

    @Test
    fun `isImage should return false for null mime type`() {
        val attachment = randomAttachment(mimeType = null)
        attachment.isImage shouldBeEqualTo false
    }

    @Test
    fun `uploadId should return correct uploadId from extraData`() {
        val attachment = randomAttachment(extraData = mapOf(EXTRA_UPLOAD_ID to "12345"))
        attachment.uploadId shouldBeEqualTo "12345"
    }

    @Test
    fun `uploadId should return null if uploadId is not in extraData`() {
        val attachment = randomAttachment(extraData = mapOf())
        attachment.uploadId shouldBeEqualTo null
    }

    @Test
    fun `duration should return correct duration from extraData`() {
        val attachment = randomAttachment(extraData = mapOf(EXTRA_DURATION to 10.5))
        attachment.duration shouldBeEqualTo 10.5f
    }

    @Test
    fun `duration should return null if duration is not in extraData`() {
        val attachment = randomAttachment(extraData = mapOf())
        attachment.duration shouldBeEqualTo null
    }

    @Test
    fun `durationInMs should return correct duration in milliseconds`() {
        val attachment = randomAttachment(extraData = mapOf(EXTRA_DURATION to 10.5))
        attachment.durationInMs shouldBeEqualTo 10500
    }

    @Test
    fun `durationInMs should return null if duration is not in extraData`() {
        val attachment = randomAttachment(extraData = mapOf())
        attachment.durationInMs shouldBeEqualTo null
    }

    @Test
    fun `waveformData should return correct waveform data from extraData`() {
        val waveformData = listOf(0.1f, 0.2f, 0.3f)
        val attachment = randomAttachment(extraData = mapOf(EXTRA_WAVEFORM_DATA to waveformData))
        attachment.waveformData shouldBeEqualTo waveformData
    }

    @Test
    fun `waveformData should return null if waveform data is not in extraData`() {
        val attachment = randomAttachment(extraData = mapOf())
        attachment.waveformData shouldBeEqualTo null
    }
}
