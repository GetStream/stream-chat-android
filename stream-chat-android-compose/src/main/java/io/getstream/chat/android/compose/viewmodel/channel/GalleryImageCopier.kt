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

package io.getstream.chat.android.compose.viewmodel.channel

import android.content.Context
import android.net.Uri
import io.getstream.chat.android.client.internal.file.StreamFileManager
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Copies a picked gallery or document [Uri] into app cache as a local image [File].
 */
internal fun interface GalleryImageCopier {
    /**
     * @param uri The content or file [Uri] to read.
     * @return A file in app cache, or `null` if the [Uri] cannot be read or written.
     */
    suspend fun copyToCache(uri: Uri): File?
}

/**
 * [GalleryImageCopier] that reads the [Uri] via [android.content.ContentResolver] and writes an image
 * into timestamped cache using [StreamFileManager].
 *
 * @param context Used for [android.content.ContentResolver] and cache directory access.
 * @param fileManager Writes the stream into cache.
 */
internal class ContentResolverImageCopier(
    private val context: Context,
    private val fileManager: StreamFileManager = StreamFileManager(),
) : GalleryImageCopier {

    override suspend fun copyToCache(uri: Uri): File? = withContext(DispatcherProvider.IO) {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
        fileManager.writeFileInTimestampedCache(
            context = context,
            fileName = "image_${UUID.randomUUID()}",
            source = inputStream,
        ).getOrNull()
    }
}
