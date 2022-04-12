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

package com.getstream.sdk.chat.model

import android.content.Context
import android.net.Uri
import com.getstream.sdk.chat.StreamFileUtil
import com.getstream.sdk.chat.utils.Utils
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import java.io.File

@ExperimentalStreamChatApi
public data class AttachmentMetaData(
    var uri: Uri? = null,
    var type: String? = null,
    var mimeType: String? = null,
    var title: String? = null,
    var file: File? = null,
) {
    var size: Long = 0
    var isSelected: Boolean = false
    var selectedPosition: Int = 0
    var videoLength: Long = 0

    public constructor(attachment: Attachment) : this(
        type = attachment.type,
        mimeType = attachment.mimeType,
        title = attachment.title
    )

    public constructor(
        context: Context,
        file: File
    ) : this(file = file, uri = StreamFileUtil.getUriForFile(context, file)) {
        mimeType = Utils.getMimeType(file)
        type = getTypeFromMimeType(mimeType)
        size = file.length()
    }

    private fun getTypeFromMimeType(mimeType: String?): String = mimeType?.let { type ->
        when {
            type.contains("image") -> {
                ModelType.attach_image
            }
            type.contains("video") -> {
                ModelType.attach_video
            }
            else -> {
                ModelType.attach_file
            }
        }
    } ?: ModelType.attach_file
}
