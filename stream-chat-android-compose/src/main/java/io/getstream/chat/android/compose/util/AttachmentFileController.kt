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

package io.getstream.chat.android.compose.util

import android.content.Context
import android.net.Uri
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.images.internal.StreamImageLoader
import io.getstream.chat.android.ui.common.utils.StreamFileUtil
import io.getstream.result.Error
import io.getstream.result.Result

internal class AttachmentFileController(
    private val context: Context,
) {

    suspend fun getFileFromCache(attachment: Attachment): Result<Uri> =
        StreamFileUtil.getFileFromCache(context, attachment)

    suspend fun downloadImage(attachment: Attachment): Result<Uri> =
        attachment.imageUrl?.let { imageUrl ->
            StreamImageLoader.instance().loadAsBitmap(
                context = context,
                url = imageUrl,
            )?.let { bitmap ->
                StreamFileUtil.writeImageToSharableFile(context, bitmap)
            } ?: Result.Failure(Error.GenericError("Unable to share image: $imageUrl"))
        } ?: Result.Failure(Error.GenericError("Unable to share image"))

    suspend fun downloadFile(attachment: Attachment): Result<Uri> =
        StreamFileUtil.writeFileToShareableFile(context, attachment)
}
