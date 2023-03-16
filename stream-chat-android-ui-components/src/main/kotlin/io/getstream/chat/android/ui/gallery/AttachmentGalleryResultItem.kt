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

package io.getstream.chat.android.ui.gallery

import android.os.Parcelable
import io.getstream.chat.android.client.models.Attachment
import kotlinx.parcelize.Parcelize

/**
 * Parcelable data class that represents [Attachment] in [AttachmentGalleryActivity] as result some operation. See click
 * listeners of [AttachmentGalleryActivity].
 */
@Parcelize
public data class AttachmentGalleryResultItem(
    val messageId: String,
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
    val url: String? = null,
    val name: String? = null,
    val parentMessageId: String? = null,
) : Parcelable

/**
 * Extension to convert instance of [AttachmentGalleryResultItem] to [Attachment] type.
 */
public fun AttachmentGalleryResultItem.toAttachment(): Attachment {
    return Attachment(
        authorName = authorName,
        authorLink = authorLink,
        imageUrl = imageUrl,
        assetUrl = assetUrl,
        url = url,
        name = name,
        image = image,
        type = type,
        text = text,
        title = title,
        fileSize = fileSize,
        mimeType = mimeType,
    )
}

/**
 * Extension to convert instance of [Attachment] to [AttachmentGalleryResultItem] type.
 *
 * @param messageId The ID of the message containing the attachment.
 * @param cid the ID of the channel containing the message which contains the attachment.
 * @param userName The name of the user who sent the message containing the attachment.
 * @param isMine If the message containing the attachment was sent by the currently logged in user or not.
 * @param parentMessageId Indicates if the message is a thread message and has a parent. If it is not a thread message,
 *  a null value should be passed in.
 */
public fun Attachment.toAttachmentGalleryResultItem(
    messageId: String,
    cid: String,
    userName: String,
    isMine: Boolean,
    parentMessageId: String?,
): AttachmentGalleryResultItem {
    return AttachmentGalleryResultItem(
        messageId = messageId,
        cid = cid,
        userName = userName,
        isMine = isMine,
        imageUrl = this.imageUrl,
        assetUrl = this.assetUrl,
        name = this.name,
        authorLink = authorLink,
        parentMessageId = parentMessageId,
    )
}
