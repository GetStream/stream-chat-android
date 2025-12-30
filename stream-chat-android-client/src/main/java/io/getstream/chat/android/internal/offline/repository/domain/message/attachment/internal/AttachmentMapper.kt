/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.internal.offline.repository.domain.message.attachment.internal

import io.getstream.chat.android.internal.offline.repository.domain.message.attachment.internal.UploadStateEntity.Companion.UPLOAD_STATE_FAILED
import io.getstream.chat.android.internal.offline.repository.domain.message.attachment.internal.UploadStateEntity.Companion.UPLOAD_STATE_IN_PROGRESS
import io.getstream.chat.android.internal.offline.repository.domain.message.attachment.internal.UploadStateEntity.Companion.UPLOAD_STATE_SUCCESS
import io.getstream.chat.android.models.Attachment
import io.getstream.result.Error
import java.io.File

internal fun Attachment.toEntity(messageId: String, index: Int): AttachmentEntity {
    val mutableExtraData = extraData.toMutableMap()
    val generatedId = mutableExtraData.getOrPut(AttachmentEntity.EXTRA_DATA_ID_KEY) {
        AttachmentEntity.generateId(messageId, index)
    } as String
    return AttachmentEntity(
        id = generatedId,
        messageId = messageId,
        authorName = authorName,
        titleLink = titleLink,
        authorLink = authorLink,
        thumbUrl = thumbUrl,
        imageUrl = imageUrl,
        assetUrl = assetUrl,
        ogUrl = ogUrl,
        mimeType = mimeType,
        fileSize = fileSize,
        title = title,
        text = text,
        type = type,
        image = image,
        name = name,
        fallback = fallback,
        uploadFilePath = upload?.absolutePath,
        uploadState = uploadState?.toEntity(),
        originalHeight = originalHeight,
        originalWidth = originalWidth,
        extraData = mutableExtraData,
    )
}

internal fun Attachment.toReplyEntity(messageId: String, index: Int): ReplyAttachmentEntity {
    val mutableExtraData = extraData.toMutableMap()
    val generatedId = mutableExtraData.getOrPut(AttachmentEntity.EXTRA_DATA_ID_KEY) {
        AttachmentEntity.generateId(messageId, index)
    } as String
    return ReplyAttachmentEntity(
        id = generatedId,
        messageId = messageId,
        authorName = authorName,
        titleLink = titleLink,
        authorLink = authorLink,
        thumbUrl = thumbUrl,
        imageUrl = imageUrl,
        assetUrl = assetUrl,
        ogUrl = ogUrl,
        mimeType = mimeType,
        fileSize = fileSize,
        title = title,
        text = text,
        type = type,
        image = image,
        name = name,
        fallback = fallback,
        uploadFilePath = upload?.absolutePath,
        uploadState = uploadState?.toEntity(),
        originalHeight = originalHeight,
        originalWidth = originalWidth,
        extraData = mutableExtraData,
    )
}

internal fun AttachmentEntity.toModel(): Attachment = Attachment(
    authorName = authorName,
    titleLink = titleLink,
    authorLink = authorLink,
    thumbUrl = thumbUrl,
    imageUrl = imageUrl,
    assetUrl = assetUrl,
    ogUrl = ogUrl,
    mimeType = mimeType,
    fileSize = fileSize,
    title = title,
    text = text,
    type = type,
    image = image,
    name = name,
    fallback = fallback,
    upload = uploadFilePath?.let(::File),
    uploadState = uploadState?.toModel(uploadFilePath?.let(::File)),
    originalHeight = originalHeight,
    originalWidth = originalWidth,
    extraData = extraData,
)

internal fun ReplyAttachmentEntity.toModel(): Attachment = Attachment(
    authorName = authorName,
    titleLink = titleLink,
    authorLink = authorLink,
    thumbUrl = thumbUrl,
    imageUrl = imageUrl,
    assetUrl = assetUrl,
    ogUrl = ogUrl,
    mimeType = mimeType,
    fileSize = fileSize,
    title = title,
    text = text,
    type = type,
    image = image,
    name = name,
    fallback = fallback,
    upload = uploadFilePath?.let(::File),
    uploadState = uploadState?.toModel(uploadFilePath?.let(::File)),
    originalHeight = originalHeight,
    originalWidth = originalWidth,
    extraData = extraData,
)

private fun Attachment.UploadState.toEntity(): UploadStateEntity {
    val (statusCode, errorMessage) = when (this) {
        Attachment.UploadState.Success -> UPLOAD_STATE_SUCCESS to null
        Attachment.UploadState.Idle -> UPLOAD_STATE_IN_PROGRESS to null
        is Attachment.UploadState.InProgress -> UPLOAD_STATE_IN_PROGRESS to null
        is Attachment.UploadState.Failed -> UPLOAD_STATE_FAILED to (this.error.message)
    }
    return UploadStateEntity(statusCode, errorMessage)
}

private fun UploadStateEntity.toModel(uploadFile: File?): Attachment.UploadState = when (this.statusCode) {
    UPLOAD_STATE_SUCCESS -> Attachment.UploadState.Success
    UPLOAD_STATE_IN_PROGRESS -> Attachment.UploadState.InProgress(0, uploadFile?.length() ?: 0)
    UPLOAD_STATE_FAILED -> Attachment.UploadState.Failed(Error.GenericError(message = this.errorMessage ?: ""))
    else -> error("Integer value of $statusCode can't be mapped to UploadState")
}
