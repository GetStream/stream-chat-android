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

package io.getstream.chat.android.ui.common.internal.file

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.internal.file.StreamFileManager
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.flatMap
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Class handling operations related to sharing files with external apps.
 */
@InternalStreamChatApi
public class StreamShareFileManager(
    private val fileManager: StreamFileManager = StreamFileManager(),
    private val uriProvider: ShareableUriProvider = ShareableUriProvider(),
) {

    /**
     * Writes a bitmap to a shareable file in the cache directory and returns a shareable URI.
     *
     * @param context The Android context.
     * @param bitmap The bitmap to write.
     * @return A [Result] containing the [Uri] of the shareable file, or an error if the operation fails.
     */
    @Suppress("TooGenericExceptionCaught")
    public suspend fun writeBitmapToShareableFile(
        context: Context,
        bitmap: Bitmap,
    ): Result<Uri> = withContext(DispatcherProvider.IO) {
        val fileName = "shared_image_${System.currentTimeMillis()}.png"
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, BITMAP_QUALITY, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val inputStream = ByteArrayInputStream(byteArray)
            fileManager
                .writeFileInCache(context, fileName, inputStream)
                .map { file -> getUriForFile(context, file) }
        } catch (e: Exception) {
            Result.Failure(Error.ThrowableError("Could not write bitmap.", e))
        }
    }

    /**
     * Writes an attachment to a shareable file in the cache directory and returns a shareable URI.
     * If the attachment is already cached, returns the cached file URI immediately.
     * Otherwise, downloads the attachment from the server and caches it.
     *
     * @param context The Android context.
     * @param attachment The attachment to write.
     * @param chatClient Lambda providing the [ChatClient] instance for downloading. Defaults to [ChatClient.instance].
     * @return A [Result] containing the [Uri] of the shareable file, or an error if the operation fails.
     */
    public suspend fun writeAttachmentToShareableFile(
        context: Context,
        attachment: Attachment,
        chatClient: () -> ChatClient = { ChatClient.instance() },
    ): Result<Uri> = withContext(DispatcherProvider.IO) {
        // Check if already cached
        val cachedFile = getCachedFileForAttachment(context, attachment)
        if (cachedFile is Result.Success) {
            return@withContext Result.Success(getUriForFile(context, cachedFile.value))
        }
        // Not cached -> download and cache
        val url = attachment.assetUrl ?: attachment.imageUrl
            ?: return@withContext Result.Failure(Error.GenericError(message = "File URL cannot be null."))

        return@withContext chatClient()
            .downloadFile(url)
            .await()
            .flatMap { response ->
                val fileName = getCacheFileName(attachment)
                val source = response.byteStream()
                fileManager.writeFileInCache(context, fileName, source)
            }
            .map { file -> getUriForFile(context, file) }
    }

    /**
     * Gets a shareable URI for an attachment that is already cached.
     * This method does not download the attachment if it's not cached.
     *
     * @param context The Android context.
     * @param attachment The attachment to get the URI for.
     * @return A [Result] containing the [Uri] of the cached file, or an error if the file is not cached.
     */
    public suspend fun getShareableUriForAttachment(
        context: Context,
        attachment: Attachment,
    ): Result<Uri> = withContext(DispatcherProvider.IO) {
        return@withContext getCachedFileForAttachment(context, attachment).map { file ->
            getUriForFile(context, file)
        }
    }

    private suspend fun getCachedFileForAttachment(context: Context, attachment: Attachment): Result<File> {
        val fileName = getCacheFileName(attachment)
        return fileManager.getFileFromCache(context, fileName).flatMap { file ->
            // Ensure attachment was really cached
            if (file.exists() && file.length() == attachment.fileSize.toLong()) {
                Result.Success(file)
            } else {
                Result.Failure(Error.GenericError("Cached file is invalid or incomplete."))
            }
        }
    }

    private fun getCacheFileName(attachment: Attachment): String {
        val url = attachment.assetUrl ?: attachment.imageUrl
        val hashCode = url?.hashCode() ?: 0
        return "${CACHE_FILE_PREFIX}${hashCode}${attachment.name}"
    }

    private fun getUriForFile(context: Context, file: File): Uri =
        uriProvider.getUriForFile(context, file)

    private companion object {
        private const val CACHE_FILE_PREFIX = "TMP"
        private const val BITMAP_QUALITY = 90
    }
}
