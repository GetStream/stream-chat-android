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

package io.getstream.chat.android.ui.common.state.messages.composer

import android.content.Context
import android.net.Uri
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.common.internal.file.ShareableUriProvider
import io.getstream.chat.android.ui.common.utils.Utils
import java.io.File

/**
 * A model for the currently selected attachment item.
 */
public data class AttachmentMetaData(
    var uri: Uri? = null,
    var type: String? = null,
    var mimeType: String? = null,
    var title: String? = null,
    var file: File? = null,
    var extraData: Map<String, Any> = mapOf(),
) {
    var size: Long = 0
    var isSelected: Boolean = false
    var selectedPosition: Int = 0
    var videoLength: Long = 0

    public constructor(attachment: Attachment) : this(
        type = attachment.type,
        mimeType = attachment.mimeType,
        title = attachment.title,
    )

    public constructor(
        context: Context,
        file: File,
    ) : this(file = file, uri = ShareableUriProvider().getUriForFile(context, file)) {
        mimeType = Utils.getMimeType(file)
        type = getTypeFromMimeType(mimeType)
        size = file.length()
        title = file.name
    }

    private fun getTypeFromMimeType(mimeType: String?): String = mimeType?.let { type ->
        when {
            type.contains("image") -> {
                AttachmentType.IMAGE
            }
            type.contains("video") -> {
                AttachmentType.VIDEO
            }
            else -> {
                AttachmentType.FILE
            }
        }
    } ?: AttachmentType.FILE
}
