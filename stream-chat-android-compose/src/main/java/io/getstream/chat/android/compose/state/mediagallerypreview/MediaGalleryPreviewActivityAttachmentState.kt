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

package io.getstream.chat.android.compose.state.mediagallerypreview

import android.os.Parcelable
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import kotlinx.parcelize.Parcelize

/**
 * Class used to transform [Attachment] into a smaller and easily
 * parcelable version that contains the minimum necessary data
 * for the proper functioning of the Media Gallery Preview screen.
 *
 * @param name The name of the attachment.
 * @param thumbUrl The URL for the thumbnail version of the attachment,
 * given the attachment has a visual quality, e.g. is a video, an image,
 * a link to a website or similar.
 * @param imageUrl The URL for the raw version of the attachment.
 * Guaranteed to be non-null for images, optional for other types.
 * @param assetUrl The URL for the raw asset, used for various types of
 * attachments.
 * @param originalHeight The original height of the attachment.
 * Provided if the attachment is of type "image".
 * @param originalWidth The original width of the attachment.
 * Provided if the attachment is of type "image".
 * @param type The type of the attachment, e.g. "image" or "video".
 * @param fileSize The size of the file in bytes.
 * @see [AttachmentType]
 */
@Suppress("LongParameterList")
@Parcelize
internal class MediaGalleryPreviewActivityAttachmentState(
    val name: String?,
    val thumbUrl: String?,
    val imageUrl: String?,
    val assetUrl: String?,
    val originalWidth: Int?,
    val originalHeight: Int?,
    val type: String?,
    val fileSize: Int,
) : Parcelable

/**
 * Maps [Attachment] to [MediaGalleryPreviewActivityAttachmentState].
 */
internal fun Attachment.toMediaGalleryPreviewActivityAttachmentState(): MediaGalleryPreviewActivityAttachmentState =
    MediaGalleryPreviewActivityAttachmentState(
        name = this.name,
        thumbUrl = this.thumbUrl,
        imageUrl = this.imageUrl,
        assetUrl = this.assetUrl,
        originalWidth = this.originalWidth,
        originalHeight = this.originalHeight,
        type = this.type,
        fileSize = this.fileSize,
    )

/**
 * Maps [MediaGalleryPreviewActivityAttachmentState] to [Attachment].
 */
internal fun MediaGalleryPreviewActivityAttachmentState.toAttachment(): Attachment = Attachment(
    name = this.name,
    thumbUrl = this.thumbUrl,
    imageUrl = this.imageUrl,
    assetUrl = this.assetUrl,
    originalWidth = this.originalWidth,
    originalHeight = this.originalHeight,
    type = this.type,
    fileSize = this.fileSize,
)
