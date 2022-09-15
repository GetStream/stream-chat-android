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

package com.getstream.sdk.chat

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

private const val DEFAULT_BITMAP_QUALITY = 90

@InternalStreamChatApi
public object StreamFileUtil {

    private fun getFileProviderAuthority(context: Context): String {
        val compName = ComponentName(context, StreamFileProvider::class.java.name)
        val providerInfo = context.packageManager.getProviderInfo(compName, 0)
        return providerInfo.authority
    }

    public fun getUriForFile(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, getFileProviderAuthority(context), file)

    public fun writeImageToSharableFile(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.cacheDir,
                "share_image_${System.currentTimeMillis()}.png"
            )
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, DEFAULT_BITMAP_QUALITY, out)
                out.flush()
            }
            getUriForFile(context, file)
        } catch (_: IOException) {
            null
        }
    }

    /**
     * Creates a Stream cache directory if one doesn't exist already.
     *
     * @param context The [Context] necessary to perform
     * file operations.
     *
     * @return Returns a [Result]. If the action was successful
     * [Result.data] will contain a [File] pointing to the cache directory,
     * otherwise [Result.error] will contain a [ChatError].
     */
    @Suppress("TooGenericExceptionCaught")
    private fun getOrCreateStreamCacheDir(
        context: Context,
    ): Result<File> {
        return try {
            val file = File(context.cacheDir, STREAM_CACHE_DIR_NAME).also { streamCacheDir ->
                streamCacheDir.mkdirs()
            }

            Result(data = file)
        } catch (e: Exception) {
            val chatError = ChatError(
                message = "Could not get or create the Stream cache directory",
                cause = e
            )

            Result(error = chatError)
        }
    }

    /**
     * Deletes all the content contained within the
     * Stream cache directory.
     *
     * @param context The [Context] necessary to perform
     * file operations.
     *
     * @return Returns a [Result]. If the action was successful
     * [Result.data] will contain [Unit], otherwise [Result.error]
     * will contain a [ChatError].
     */
    @Suppress("TooGenericExceptionCaught")
    public fun clearStreamCache(
        context: Context,
    ): Result<Unit> {
        return try {
            val directory = File(context.cacheDir, STREAM_CACHE_DIR_NAME)
            directory.deleteRecursively()

            Result(data = Unit)
        } catch (e: Exception) {
            val chatError = ChatError(
                message = "Could clear the Stream cache directory",
                cause = e
            )

            Result(error = chatError)
        }
    }

    /**
     * Hashes the links of given attachments and then tries to create a new file
     * under that hash. If the file already exists checks that the full file
     * has been written and shares it if it has, in other cases downloads the file
     * and writes it.
     *
     * @param context The Android [Context] used for path resolving and [Uri] fetching.
     * @param attachment the attachment to be downloaded.
     *
     * @return Returns a [Result]. If the action was successful
     * [Result.data] will contain a [Uri] pointing to the file, otherwise [Result.error]
     * will contain a [ChatError].
     */
    public suspend fun writeFileToShareableFile(
        context: Context,
        attachment: Attachment,
    ): Result<Uri> {
        val runCatching = kotlin.runCatching {
            val getOrCreateCacheDirResult = getOrCreateStreamCacheDir(context)
            if (getOrCreateCacheDirResult.isError) return Result(error = getOrCreateCacheDirResult.error())

            val streamCacheDir = getOrCreateCacheDirResult.data()

            val attachmentName = (attachment.url ?: attachment.assetUrl)?.hashCode()
            val fileName = CACHED_FILE_PREFIX + attachmentName.toString() + attachment.name

            val file = File(streamCacheDir, fileName)

            // When File.createNewFile returns false it means that the file already exists.
            // We then check the hash name equality to confirm it's the same file and check file size
            // equality to make sure we've completed the download successfully.
            return if (!file.createNewFile() &&
                attachmentName != null &&
                // once this is functional
                file.length() == attachment.fileSize.toLong()
            ) {
                Result(data = getUriForFile(context, file))
            } else {
                val fileUrl = attachment.assetUrl ?: attachment.url ?: return Result(
                    error = ChatError(message = "File URL cannot be null.")
                )

                val response = ChatClient.instance().downloadFile(fileUrl).await()

                if (response.isSuccess) {
                    // write the response to a file
                    val byteArray = response.data().byteStream().readBytes()
                    val fileOutputStream = FileOutputStream(file)
                    fileOutputStream.write(byteArray)
                    fileOutputStream.close()

                    Result(data = getUriForFile(context, file))
                } else {
                    Result(error = response.error())
                }
            }
        }

        return runCatching.getOrNull() ?: Result(
            error = ChatError(
                message = "Could not write to file.",
                cause = (runCatching.exceptionOrNull())
            )
        )
    }

    /**
     * The name of the Stream cache directory.
     *
     * This does not include file separators so do not forget to include them
     * when using this to access the directory or files contained within.
     */
    private const val STREAM_CACHE_DIR_NAME = "stream_cache"

    /**
     * The prefix to all cached file names.
     */
    private const val CACHED_FILE_PREFIX = "TMP"
}
