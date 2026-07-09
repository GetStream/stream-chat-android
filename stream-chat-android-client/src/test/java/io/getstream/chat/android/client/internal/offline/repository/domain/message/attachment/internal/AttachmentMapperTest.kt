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

package io.getstream.chat.android.client.internal.offline.repository.domain.message.attachment.internal

import io.getstream.chat.android.client.internal.offline.randomAttachmentEntity
import io.getstream.chat.android.client.internal.offline.randomReplyAttachmentEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.attachment.internal.UploadStateEntity.Companion.UPLOAD_STATE_FAILED
import io.getstream.chat.android.client.internal.offline.repository.domain.message.attachment.internal.UploadStateEntity.Companion.UPLOAD_STATE_IN_PROGRESS
import io.getstream.chat.android.client.internal.offline.repository.domain.message.attachment.internal.UploadStateEntity.Companion.UPLOAD_STATE_SUCCESS
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.positiveRandomLong
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomString
import io.getstream.result.Error
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class AttachmentMapperTest {

    @Test
    fun `Should map Attachment to AttachmentEntity correctly`() {
        val messageId = randomString()
        val index = randomInt()
        val attachment = randomAttachment(uploadState = Attachment.UploadState.Success)
        val generatedId = AttachmentEntity.generateId(messageId, index)

        val expectedEntity = AttachmentEntity(
            id = generatedId,
            messageId = messageId,
            authorName = attachment.authorName,
            titleLink = attachment.titleLink,
            authorLink = attachment.authorLink,
            thumbUrl = attachment.thumbUrl,
            imageUrl = attachment.imageUrl,
            assetUrl = attachment.assetUrl,
            ogUrl = attachment.ogUrl,
            mimeType = attachment.mimeType,
            fileSize = attachment.fileSize,
            title = attachment.title,
            text = attachment.text,
            type = attachment.type,
            image = attachment.image,
            name = attachment.name,
            fallback = attachment.fallback,
            uploadFilePath = attachment.upload?.absolutePath,
            uploadState = UploadStateEntity(UPLOAD_STATE_SUCCESS, null),
            originalHeight = attachment.originalHeight,
            originalWidth = attachment.originalWidth,
            extraData = attachment.extraData + (AttachmentEntity.EXTRA_DATA_ID_KEY to generatedId),
        )

        val result = attachment.toEntity(messageId, index)

        assertEquals(expectedEntity, result)
    }

    @Test
    fun `Should keep attachment id from extra data when mapping to AttachmentEntity`() {
        val existingId = randomString()
        val attachment = randomAttachment(
            extraData = mapOf(AttachmentEntity.EXTRA_DATA_ID_KEY to existingId),
        )

        val result = attachment.toEntity(randomString(), randomInt())

        assertEquals(existingId, result.id)
        assertEquals(attachment.extraData, result.extraData)
    }

    @Test
    fun `Should map Attachment to ReplyAttachmentEntity correctly`() {
        val messageId = randomString()
        val index = randomInt()
        val attachment = randomAttachment(uploadState = Attachment.UploadState.Success)
        val generatedId = AttachmentEntity.generateId(messageId, index)

        val expectedEntity = ReplyAttachmentEntity(
            id = generatedId,
            messageId = messageId,
            authorName = attachment.authorName,
            titleLink = attachment.titleLink,
            authorLink = attachment.authorLink,
            thumbUrl = attachment.thumbUrl,
            imageUrl = attachment.imageUrl,
            assetUrl = attachment.assetUrl,
            ogUrl = attachment.ogUrl,
            mimeType = attachment.mimeType,
            fileSize = attachment.fileSize,
            title = attachment.title,
            text = attachment.text,
            type = attachment.type,
            image = attachment.image,
            name = attachment.name,
            fallback = attachment.fallback,
            uploadFilePath = attachment.upload?.absolutePath,
            uploadState = UploadStateEntity(UPLOAD_STATE_SUCCESS, null),
            originalHeight = attachment.originalHeight,
            originalWidth = attachment.originalWidth,
            extraData = attachment.extraData + (AttachmentEntity.EXTRA_DATA_ID_KEY to generatedId),
        )

        val result = attachment.toReplyEntity(messageId, index)

        assertEquals(expectedEntity, result)
    }

    @Test
    fun `Should map AttachmentEntity to Attachment correctly`() {
        val entity = randomAttachmentEntity(
            uploadFilePath = randomString(),
            uploadState = UploadStateEntity(UPLOAD_STATE_SUCCESS, null),
        )

        val expectedAttachment = Attachment(
            authorName = entity.authorName,
            titleLink = entity.titleLink,
            authorLink = entity.authorLink,
            thumbUrl = entity.thumbUrl,
            imageUrl = entity.imageUrl,
            assetUrl = entity.assetUrl,
            ogUrl = entity.ogUrl,
            mimeType = entity.mimeType,
            fileSize = entity.fileSize,
            title = entity.title,
            text = entity.text,
            type = entity.type,
            image = entity.image,
            name = entity.name,
            fallback = entity.fallback,
            upload = entity.uploadFilePath?.let(::File),
            uploadState = Attachment.UploadState.Success,
            originalHeight = entity.originalHeight,
            originalWidth = entity.originalWidth,
            extraData = entity.extraData,
        )

        val result = entity.toModel()

        assertEquals(expectedAttachment, result)
    }

    @Test
    fun `Should map ReplyAttachmentEntity to Attachment correctly`() {
        val entity = randomReplyAttachmentEntity(
            uploadFilePath = randomString(),
            uploadState = UploadStateEntity(UPLOAD_STATE_SUCCESS, null),
        )

        val expectedAttachment = Attachment(
            authorName = entity.authorName,
            titleLink = entity.titleLink,
            authorLink = entity.authorLink,
            thumbUrl = entity.thumbUrl,
            imageUrl = entity.imageUrl,
            assetUrl = entity.assetUrl,
            ogUrl = entity.ogUrl,
            mimeType = entity.mimeType,
            fileSize = entity.fileSize,
            title = entity.title,
            text = entity.text,
            type = entity.type,
            image = entity.image,
            name = entity.name,
            fallback = entity.fallback,
            upload = entity.uploadFilePath?.let(::File),
            uploadState = Attachment.UploadState.Success,
            originalHeight = entity.originalHeight,
            originalWidth = entity.originalWidth,
            extraData = entity.extraData,
        )

        val result = entity.toModel()

        assertEquals(expectedAttachment, result)
    }

    @Test
    fun `Should map Idle upload state to in progress entity state`() {
        val attachment = randomAttachment(uploadState = Attachment.UploadState.Idle)

        val result = attachment.toEntity(randomString(), randomInt())

        assertEquals(UploadStateEntity(UPLOAD_STATE_IN_PROGRESS, null), result.uploadState)
    }

    @Test
    fun `Should map InProgress upload state to in progress entity state`() {
        val attachment = randomAttachment(
            uploadState = Attachment.UploadState.InProgress(
                bytesUploaded = positiveRandomLong(),
                totalBytes = positiveRandomLong(),
            ),
        )

        val result = attachment.toEntity(randomString(), randomInt())

        assertEquals(UploadStateEntity(UPLOAD_STATE_IN_PROGRESS, null), result.uploadState)
    }

    @Test
    fun `Should map Failed upload state to failed entity state`() {
        val errorMessage = randomString()
        val attachment = randomAttachment(
            uploadState = Attachment.UploadState.Failed(Error.GenericError(message = errorMessage)),
        )

        val result = attachment.toEntity(randomString(), randomInt())

        assertEquals(UploadStateEntity(UPLOAD_STATE_FAILED, errorMessage), result.uploadState)
    }

    @Test
    fun `Should map null upload state to null entity state`() {
        val attachment = randomAttachment(uploadState = null)

        val result = attachment.toEntity(randomString(), randomInt())

        assertEquals(null, result.uploadState)
    }

    @Test
    fun `Should map in progress entity state to InProgress upload state`() {
        val entity = randomAttachmentEntity(
            uploadFilePath = null,
            uploadState = UploadStateEntity(UPLOAD_STATE_IN_PROGRESS, null),
        )

        val result = entity.toModel()

        assertEquals(Attachment.UploadState.InProgress(bytesUploaded = 0L, totalBytes = 0L), result.uploadState)
    }

    @Test
    fun `Should map failed entity state to Failed upload state`() {
        val errorMessage = randomString()
        val entity = randomAttachmentEntity(
            uploadState = UploadStateEntity(UPLOAD_STATE_FAILED, errorMessage),
        )

        val result = entity.toModel()

        assertEquals(Attachment.UploadState.Failed(Error.GenericError(message = errorMessage)), result.uploadState)
    }

    @Test
    fun `Should map failed entity state without message to Failed upload state with empty message`() {
        val entity = randomAttachmentEntity(
            uploadState = UploadStateEntity(UPLOAD_STATE_FAILED, null),
        )

        val result = entity.toModel()

        assertEquals(Attachment.UploadState.Failed(Error.GenericError(message = "")), result.uploadState)
    }

    @Test
    fun `Should throw when mapping entity with unknown upload state code`() {
        val entity = randomAttachmentEntity(
            uploadState = UploadStateEntity(UPLOAD_STATE_FAILED + positiveRandomInt(), null),
        )

        assertThrows<IllegalStateException> { entity.toModel() }
    }
}
