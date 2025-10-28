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

package io.getstream.chat.android.ui.utils

import android.webkit.MimeTypeMap
import android.widget.ImageView
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.disposable.Disposable
import io.getstream.chat.android.ui.common.images.internal.StreamImageLoader.ImageTransformation.RoundedCorners
import io.getstream.chat.android.ui.common.images.resizing.applyStreamCdnImageResizingIfEnabled
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.common.utils.StreamFileUtil
import io.getstream.chat.android.ui.common.utils.extensions.getDisplayableName
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise

private val FILE_THUMB_TRANSFORMATION = RoundedCorners(3.dpToPxPrecise())

internal fun ImageView.loadAttachmentThumb(attachment: Attachment): Disposable = with(attachment) {
    when {
        isVideo() && ChatUI.videoThumbnailsEnabled && !thumbUrl.isNullOrBlank() ->
            load(
                data = thumbUrl?.applyStreamCdnImageResizingIfEnabled(ChatUI.streamCdnImageResizing),
                transformation = FILE_THUMB_TRANSFORMATION,
            )
        isImage() && !imageUrl.isNullOrBlank() ->
            load(
                data = imageUrl?.applyStreamCdnImageResizingIfEnabled(ChatUI.streamCdnImageResizing),
                transformation = FILE_THUMB_TRANSFORMATION,
            )
        else -> {
            // The mime type, or a best guess based on the extension
            val actualMimeType = mimeType ?: MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(attachment.getDisplayableName()?.substringAfterLast('.'))

            // We don't have icons for image types, but we can load the actual image in this case
            if (actualMimeType?.startsWith("image") == true && attachment.upload != null) {
                load(
                    data = StreamFileUtil.getUriForFile(context, attachment.upload!!),
                    transformation = FILE_THUMB_TRANSFORMATION,
                )
            } else {
                load(data = ChatUI.mimeTypeIconProvider.getIconRes(actualMimeType))
            }
        }
    }
}

internal fun ImageView.loadAttachmentThumb(attachment: AttachmentMetaData): Disposable = with(attachment) {
    when (type) {
        AttachmentType.VIDEO -> loadVideoThumbnail(
            uri = uri,
            transformation = FILE_THUMB_TRANSFORMATION,
        )
        AttachmentType.IMAGE -> load(data = uri, transformation = FILE_THUMB_TRANSFORMATION)
        else -> load(data = ChatUI.mimeTypeIconProvider.getIconRes(mimeType))
    }
}
