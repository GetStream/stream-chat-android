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

package io.getstream.chat.docs.kotlin.ui.guides.realm.entities

import io.getstream.chat.android.models.Attachment
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.io.File

private const val DEFAULT_ORIGINAL_HEIGHT = 200
private const val DEFAULT_ORIGINAL_WIDTH = 200

@Suppress("VariableNaming")
internal class AttachmentEntityRealm : RealmObject {
    @PrimaryKey
    var id: String = ""
    var message_id: String? = null
    var author_name: String? = null
    var title_link: String? = null
    var author_link: String? = null
    var thumb_url: String? = null
    var image_url: String? = null
    var asset_url: String? = null
    var og_url: String? = null
    var mime_type: String? = null
    var file_size: Int = 0
    var title: String? = null
    var text: String? = null
    var type: String? = null
    var image: String? = null
    var url: String? = null
    var name: String? = null
    var fallback: String? = null
    var upload_file_path: String? = null
    var original_height: Int = 0
    var original_width: Int = 0
    var upload_state: UploadStateEntityRealm? = null
}

internal fun AttachmentEntityRealm.toDomain(): Attachment =
    Attachment(
        authorName = author_name,
        authorLink = author_link,
        titleLink = title_link,
        thumbUrl = thumb_url,
        imageUrl = image_url,
        assetUrl = asset_url,
        ogUrl = og_url,
        mimeType = mime_type,
        fileSize = file_size,
        title = title,
        text = text,
        type = type,
        image = image,
        url = url,
        name = name,
        fallback = fallback,
        originalHeight = original_height,
        originalWidth = original_width,
        upload = upload_file_path?.let(::File),
        uploadState = upload_state?.toDomain(upload_file_path?.let(::File)),
    )

internal fun Attachment.toRealm(messageId: String, index: Int): AttachmentEntityRealm {
    val thisAttachment = this

    return AttachmentEntityRealm().apply {
        id = generateId(messageId, index)
        message_id = messageId
        author_name = thisAttachment.authorName
        title_link = thisAttachment.titleLink
        author_link = thisAttachment.authorLink
        thumb_url = thisAttachment.thumbUrl
        image_url = thisAttachment.imageUrl
        asset_url = thisAttachment.assetUrl
        og_url = thisAttachment.ogUrl
        mime_type = thisAttachment.mimeType
        file_size = thisAttachment.fileSize
        title = thisAttachment.title
        text = thisAttachment.text
        type = thisAttachment.type
        image = thisAttachment.image
        url = thisAttachment.url
        name = thisAttachment.name
        fallback = thisAttachment.fallback
        upload_file_path = thisAttachment.fallback
        original_height = thisAttachment.originalHeight ?: DEFAULT_ORIGINAL_HEIGHT
        original_width = thisAttachment.originalWidth ?: DEFAULT_ORIGINAL_WIDTH
        upload_state = thisAttachment.uploadState?.toRealm()
    }
}

private fun generateId(messageId: String, index: Int): String {
    return messageId + "_$index"
}
