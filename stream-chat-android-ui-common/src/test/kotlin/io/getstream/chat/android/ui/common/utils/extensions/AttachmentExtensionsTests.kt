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

package io.getstream.chat.android.ui.common.utils.extensions

import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomString
import io.getstream.result.Error
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

internal class AttachmentExtensionsTests {

    @Test
    fun `getDisplayableName should return title when present`() {
        // Given
        val title = randomString()
        val name = randomString()
        val attachment = randomAttachment(
            title = title,
            name = name,
            upload = File(randomString()),
        )

        // When
        val displayableName = attachment.getDisplayableName()

        // Then
        Assertions.assertEquals(title, displayableName)
    }

    @Test
    fun `getDisplayableName should return name when title is null`() {
        // Given
        val name = randomString()
        val attachment = randomAttachment(
            title = null,
            name = name,
            upload = File(randomString()),
        )

        // When
        val displayableName = attachment.getDisplayableName()

        // Then
        Assertions.assertEquals(name, displayableName)
    }

    @Test
    fun `getDisplayableName should return upload name when title and name are null`() {
        // Given
        val uploadName = randomString()
        val attachment = randomAttachment(
            title = null,
            name = null,
            upload = File(uploadName),
        )

        // When
        val displayableName = attachment.getDisplayableName()

        // Then
        Assertions.assertEquals(uploadName, displayableName)
    }

    @Test
    fun `getDisplayableName should return null when title, name and upload are null`() {
        // Given
        val attachment = randomAttachment(
            title = null,
            name = null,
            upload = null,
        )

        // When
        val displayableName = attachment.getDisplayableName()

        // Then
        Assertions.assertNull(displayableName)
    }

    @Test
    fun `getDisplayableName should remove time prefix from title`() {
        // Given
        val title = "STREAM_123456789_document.pdf"
        val attachment = randomAttachment(
            title = title,
            name = null,
            upload = null,
        )

        // When
        val displayableName = attachment.getDisplayableName()

        // Then
        Assertions.assertEquals("document.pdf", displayableName)
    }

    @Test
    fun `imagePreviewUrl should return thumbUrl when present`() {
        // Given
        val thumbUrl = randomString()
        val imageUrl = randomString()
        val attachment = randomAttachment(
            thumbUrl = thumbUrl,
            imageUrl = imageUrl,
        )

        // When
        val previewUrl = attachment.imagePreviewUrl

        // Then
        Assertions.assertEquals(thumbUrl, previewUrl)
    }

    @Test
    fun `imagePreviewUrl should return imageUrl when thumbUrl is null`() {
        // Given
        val imageUrl = randomString()
        val attachment = randomAttachment(
            thumbUrl = null,
            imageUrl = imageUrl,
        )

        // When
        val previewUrl = attachment.imagePreviewUrl

        // Then
        Assertions.assertEquals(imageUrl, previewUrl)
    }

    @Test
    fun `imagePreviewUrl should return null when both thumbUrl and imageUrl are null`() {
        // Given
        val attachment = randomAttachment(
            thumbUrl = null,
            imageUrl = null,
        )

        // When
        val previewUrl = attachment.imagePreviewUrl

        // Then
        Assertions.assertNull(previewUrl)
    }

    @Test
    fun `isAnyFileType should return true when uploadId is present`() {
        // Given
        val attachment = randomAttachment(
            type = AttachmentType.IMAGE,
            extraData = mapOf("uploadId" to randomString()),
        )

        // When
        val result = attachment.isAnyFileType()

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    fun `isAnyFileType should return true when upload is present`() {
        // Given
        val attachment = randomAttachment(
            type = AttachmentType.IMAGE,
            upload = File(randomString()),
        )

        // When
        val result = attachment.isAnyFileType()

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    fun `isAnyFileType should return true for file type`() {
        // Given
        val attachment = randomAttachment(
            type = AttachmentType.FILE,
            upload = null,
            extraData = emptyMap(),
        )

        // When
        val result = attachment.isAnyFileType()

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    fun `isAnyFileType should return true for video type`() {
        // Given
        val attachment = randomAttachment(
            type = AttachmentType.VIDEO,
            upload = null,
            extraData = emptyMap(),
        )

        // When
        val result = attachment.isAnyFileType()

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    fun `isAnyFileType should return true for audio type`() {
        // Given
        val attachment = randomAttachment(
            type = AttachmentType.AUDIO,
            upload = null,
            extraData = emptyMap(),
        )

        // When
        val result = attachment.isAnyFileType()

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    fun `isAnyFileType should return true for audio recording type`() {
        // Given
        val attachment = randomAttachment(
            type = AttachmentType.AUDIO_RECORDING,
            upload = null,
            extraData = emptyMap(),
        )

        // When
        val result = attachment.isAnyFileType()

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    fun `isAnyFileType should return false for image without uploadId or upload`() {
        // Given
        val attachment = randomAttachment(
            type = AttachmentType.IMAGE,
            upload = null,
            extraData = emptyMap(),
        )

        // When
        val result = attachment.isAnyFileType()

        // Then
        Assertions.assertFalse(result)
    }

    @Test
    fun `isUploading should return true when uploadState is InProgress with upload and uploadId`() {
        // Given
        val attachment = randomAttachment(
            uploadState = Attachment.UploadState.InProgress(bytesUploaded = 50, totalBytes = 100),
            upload = File(randomString()),
            extraData = mapOf("uploadId" to randomString()),
        )

        // When
        val result = attachment.isUploading()

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    fun `isUploading should return true when uploadState is Idle with upload and uploadId`() {
        // Given
        val attachment = randomAttachment(
            uploadState = Attachment.UploadState.Idle,
            upload = File(randomString()),
            extraData = mapOf("uploadId" to randomString()),
        )

        // When
        val result = attachment.isUploading()

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    fun `isUploading should return false when uploadState is Success`() {
        // Given
        val attachment = randomAttachment(
            uploadState = Attachment.UploadState.Success,
            upload = File(randomString()),
            extraData = mapOf("uploadId" to randomString()),
        )

        // When
        val result = attachment.isUploading()

        // Then
        Assertions.assertFalse(result)
    }

    @Test
    fun `isUploading should return false when upload is null`() {
        // Given
        val attachment = randomAttachment(
            uploadState = Attachment.UploadState.InProgress(bytesUploaded = 50, totalBytes = 100),
            upload = null,
            extraData = mapOf("uploadId" to randomString()),
        )

        // When
        val result = attachment.isUploading()

        // Then
        Assertions.assertFalse(result)
    }

    @Test
    fun `isUploading should return false when uploadId is null`() {
        // Given
        val attachment = randomAttachment(
            uploadState = Attachment.UploadState.InProgress(bytesUploaded = 50, totalBytes = 100),
            upload = File(randomString()),
            extraData = emptyMap(),
        )

        // When
        val result = attachment.isUploading()

        // Then
        Assertions.assertFalse(result)
    }

    @Test
    fun `isFailed should return true when uploadState is Failed with upload and uploadId`() {
        // Given
        val attachment = randomAttachment(
            uploadState = Attachment.UploadState.Failed(
                error = Error.GenericError(message = randomString()),
            ),
            upload = File(randomString()),
            extraData = mapOf("uploadId" to randomString()),
        )

        // When
        val result = attachment.isFailed()

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    fun `isFailed should return false when uploadState is Success`() {
        // Given
        val attachment = randomAttachment(
            uploadState = Attachment.UploadState.Success,
            upload = File(randomString()),
            extraData = mapOf("uploadId" to randomString()),
        )

        // When
        val result = attachment.isFailed()

        // Then
        Assertions.assertFalse(result)
    }

    @Test
    fun `isFailed should return false when upload is null`() {
        // Given
        val attachment = randomAttachment(
            uploadState = Attachment.UploadState.Failed(
                error = Error.GenericError(message = randomString()),
            ),
            upload = null,
            extraData = mapOf("uploadId" to randomString()),
        )

        // When
        val result = attachment.isFailed()

        // Then
        Assertions.assertFalse(result)
    }

    @Test
    fun `isFailed should return false when uploadId is null`() {
        // Given
        val attachment = randomAttachment(
            uploadState = Attachment.UploadState.Failed(
                error = Error.GenericError(message = randomString()),
            ),
            upload = File(randomString()),
            extraData = emptyMap(),
        )

        // When
        val result = attachment.isFailed()

        // Then
        Assertions.assertFalse(result)
    }

    @Test
    fun `isFailed should return false when uploadState is InProgress`() {
        // Given
        val attachment = randomAttachment(
            uploadState = Attachment.UploadState.InProgress(bytesUploaded = 50, totalBytes = 100),
            upload = File(randomString()),
            extraData = mapOf("uploadId" to randomString()),
        )

        // When
        val result = attachment.isFailed()

        // Then
        Assertions.assertFalse(result)
    }

    @Test
    fun `hasLink should return true when titleLink is present`() {
        // Given
        val attachment = randomAttachment(
            titleLink = randomString(),
            ogUrl = null,
        )

        // When
        val result = attachment.hasLink()

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    fun `hasLink should return true when ogUrl is present`() {
        // Given
        val attachment = randomAttachment(
            titleLink = null,
            ogUrl = randomString(),
        )

        // When
        val result = attachment.hasLink()

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    fun `hasLink should return true when both titleLink and ogUrl are present`() {
        // Given
        val attachment = randomAttachment(
            titleLink = randomString(),
            ogUrl = randomString(),
        )

        // When
        val result = attachment.hasLink()

        // Then
        Assertions.assertTrue(result)
    }

    @Test
    fun `hasLink should return false when both titleLink and ogUrl are null`() {
        // Given
        val attachment = randomAttachment(
            titleLink = null,
            ogUrl = null,
        )

        // When
        val result = attachment.hasLink()

        // Then
        Assertions.assertFalse(result)
    }
}
