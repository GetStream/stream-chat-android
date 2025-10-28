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

package io.getstream.chat.android.ui.feature.gallery

import android.os.Parcelable
import io.getstream.chat.android.models.Attachment
import kotlinx.parcelize.Parcelize

/**
 * Parcelable data class that represents [Attachment] in [AttachmentGalleryActivity] as result some operation. See click
 * listeners of [AttachmentGalleryActivity].
 */
@Parcelize
public data class AttachmentGalleryResultItem(
    val messageId: String,
    val parentId: String?,
    val cid: String,
    val userName: String,
    val isMine: Boolean = false,
    val authorName: String? = null,
    val authorLink: String? = null,
    val imageUrl: String? = null,
    val assetUrl: String? = null,
    val mimeType: String? = null,
    val fileSize: Int = 0,
    val title: String? = null,
    val text: String? = null,
    val type: String? = null,
    val image: String? = null,
    val name: String? = null,
) : Parcelable

/**
 * Extension to convert instance of [AttachmentGalleryResultItem] to [Attachment] type.
 */
public fun AttachmentGalleryResultItem.toAttachment(): Attachment = Attachment(
    authorName = authorName,
    authorLink = authorLink,
    imageUrl = imageUrl,
    assetUrl = assetUrl,
    name = name,
    image = image,
    type = type,
    text = text,
    title = title,
    fileSize = fileSize,
    mimeType = mimeType,
)

/**
 * Extension to convert instance of [Attachment] to [AttachmentGalleryResultItem] type.
 */
public fun Attachment.toAttachmentGalleryResultItem(
    messageId: String,
    parentId: String?,
    cid: String,
    userName: String,
    isMine: Boolean,
): AttachmentGalleryResultItem = AttachmentGalleryResultItem(
    messageId = messageId,
    parentId = parentId,
    cid = cid,
    userName = userName,
    isMine = isMine,
    imageUrl = this.imageUrl,
    assetUrl = this.assetUrl,
    name = this.name,
    authorLink = authorLink,
)
