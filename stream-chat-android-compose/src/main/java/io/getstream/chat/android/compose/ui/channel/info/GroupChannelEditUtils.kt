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

package io.getstream.chat.android.compose.ui.channel.info

import android.content.Context
import android.net.Uri
import io.getstream.chat.android.client.internal.file.StreamFileManager
import java.io.File
import java.util.UUID

private val fileManager = StreamFileManager()

/**
 * Copies the content at the given [Uri] into a temporary cache file using the SDK's [StreamFileManager].
 */
internal fun Uri.toCacheFile(context: Context): File? {
    val inputStream = context.contentResolver.openInputStream(this) ?: return null
    return fileManager.writeFileInTimestampedCache(
        context = context,
        fileName = "channel_image_${UUID.randomUUID()}.jpg",
        source = inputStream,
    ).getOrNull()
}
