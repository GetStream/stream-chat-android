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

package io.getstream.chat.android.ui.common.utils

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.StreamFileProvider
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.Result.Failure
import io.getstream.result.Result.Success
import io.getstream.result.flatMap
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

private const val DEFAULT_BITMAP_QUALITY = 90

@InternalStreamChatApi
public object StreamFileUtil {

    private fun getFileProviderAuthority(context: Context): String {
        val compName = ComponentName(context, StreamFileProvider::class.java.name)
        val providerInfo = context.packageManager.getProviderInfo(compName, 0)
        return providerInfo.authority
    }

    public fun getUriForFile(context: Context, file: File): Uri = FileProvider.getUriForFile(context, getFileProviderAuthority(context), file)

    public suspend fun writeImageToSharableFile(
        context: Context,
        bitmap: Bitmap,
        getUri: (File) -> Uri = { getUriForFile(context, it) },
    ): Result<Uri> = withContext(DispatcherProvider.IO) {
        when (val getOrCreateCacheDirResult = getOrCreateStreamCacheDir(context)) {
            is Success -> {
                try {
                    val streamCacheDir = getOrCreateCacheDirResult.value
                    val file = File(streamCacheDir, "shared_image_${System.currentTimeMillis()}.png")
                    file.outputStream().use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, DEFAULT_BITMAP_QUALITY, out)
                        out.flush()
                    }
                    Success(getUri(file))
                } catch (_: IOException) {
                    Failure(Error.GenericError("Could not write image to file."))
                }
            }

            is Failure -> getOrCreateCacheDirResult
        }
    }

    /**
     * Creates a Stream cache directory if one doesn't exist already.
     *
     * @param context The [Context] necessary to perform
     * file operations.
     *
     * @return Returns a [Result]. If the action was successful
     * [Result.Success] will contain a [File] pointing to the cache directory,
     * otherwise [Result.Failure] will contain a [Error].
     */
    @Suppress("TooGenericExceptionCaught")
    private fun getOrCreateStreamCacheDir(
        context: Context,
    ): Result<File> = try {
        val file = File(context.cacheDir, STREAM_CACHE_DIR_NAME).also { streamCacheDir ->
            streamCacheDir.mkdirs()
        }

        Success(file)
    } catch (e: Exception) {
        Failure(
            Error.ThrowableError(
                message = "Could not get or create the Stream cache directory",
                cause = e,
            ),
        )
    }

    /**
     * Creates a file with the given [fileName] inside the Stream cache directory.
     *
     * @param context The [Context] necessary to perform file operations.
     * @param fileName The name of the file to be created.
     *
     * @return The newly [File] wrapped inside [Result] if the operation was successful, otherwise returns a
     * [ChatError] wrapped inside [Result].
     */
    internal fun createFileInCacheDir(context: Context, fileName: String): Result<File> = try {
        getOrCreateStreamCacheDir(context)
            .flatMap { Success(File(it, fileName)) }
    } catch (e: Exception) {
        Failure(
            Error.ThrowableError(
                message = "Could not get or create the file.",
                cause = e,
            ),
        )
    }

    /**
     * Deletes all the content contained within the
     * Stream cache directory.
     *
     * @param context The [Context] necessary to perform
     * file operations.
     *
     * @return Returns a [Result]. If the action was successful
     * [Result.Success] will contain [Unit], otherwise [Result.Failure]
     * will contain a [Error].
     */
    @Suppress("TooGenericExceptionCaught")
    public fun clearStreamCache(
        context: Context,
    ): Result<Unit> = try {
        val directory = File(context.cacheDir, STREAM_CACHE_DIR_NAME)
        directory.deleteRecursively()

        Success(Unit)
    } catch (e: Exception) {
        Failure(
            Error.ThrowableError(
                message = "Could clear the Stream cache directory",
                cause = e,
            ),
        )
    }

    /**
     * Fetches the given attachment from cache if it has been previously cached.
     * Returns an error otherwise.
     *
     * @param context The Android [Context] used for path resolving and [Uri] fetching.
     * @param attachment the attachment to be downloaded.
     *
     * @return A [Uri] to the file is returned in the form of [Result.Success]
     * if the file was successfully fetched from the cache. Returns a [Error]
     * accessible via [Result.Failure] otherwise.
     */
    @Suppress("TooGenericExceptionCaught")
    public suspend fun getFileFromCache(
        context: Context,
        attachment: Attachment,
        getUri: (File) -> Uri = { getUriForFile(context, it) },
    ): Result<Uri> = withContext(DispatcherProvider.IO) {
        try {
            when (val getOrCreateCacheDirResult = getOrCreateStreamCacheDir(context)) {
                is Failure -> getOrCreateCacheDirResult
                is Success -> {
                    val streamCacheDir = getOrCreateCacheDirResult.value

                    val attachmentHashCode = (attachment.assetUrl)?.hashCode()
                    val fileName = CACHED_FILE_PREFIX + attachmentHashCode.toString() + attachment.name

                    val file = File(streamCacheDir, fileName)

                    // First we check if the file exists.
                    // We then check the hash code is valid and check file size
                    // equality to make sure we've completed the download successfully.
                    val isFileCached = file.exists() &&
                        attachmentHashCode != null &&
                        file.length() == attachment.fileSize.toLong()

                    if (isFileCached) {
                        Success(getUri(file))
                    } else {
                        Failure(Error.GenericError(message = "No such file in cache."))
                    }
                }
            }
        } catch (e: Exception) {
            Failure(
                Error.ThrowableError(
                    message = "Cannot determine if the file has been cached.",
                    cause = e,
                ),
            )
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
     * [Result.Success] will contain a [Uri] pointing to the file, otherwise [Result.Failure]
     * will contain a [Error].
     */
    @Suppress("ReturnCount")
    public suspend fun writeFileToShareableFile(
        context: Context,
        attachment: Attachment,
        chatClient: () -> ChatClient = { ChatClient.instance() },
        getUri: (File) -> Uri = { getUriForFile(context, it) },
    ): Result<Uri> = withContext(DispatcherProvider.IO) {
        val runCatching = runCatching {
            when (val getOrCreateCacheDirResult = getOrCreateStreamCacheDir(context)) {
                is Failure -> getOrCreateCacheDirResult
                is Success -> {
                    val streamCacheDir = getOrCreateCacheDirResult.value

                    val url = attachment.assetUrl ?: attachment.imageUrl // Supports both images and files
                    val attachmentHashCode = url?.hashCode()
                    val fileName = CACHED_FILE_PREFIX + attachmentHashCode.toString() + attachment.name

                    val file = File(streamCacheDir, fileName)

                    if (file.exists() &&
                        attachmentHashCode != null &&
                        file.length() == attachment.fileSize.toLong()
                    ) {
                        Success(getUri(file))
                    } else {
                        val fileUrl = url ?: return@withContext Failure(
                            Error.GenericError(message = "File URL cannot be null."),
                        )

                        when (val response = chatClient().downloadFile(fileUrl).await()) {
                            is Success -> {
                                // write the response to a file
                                response.value.byteStream().use { inputStream ->
                                    file.outputStream().use { outputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }

                                Success(getUri(file))
                            }

                            is Failure -> response
                        }
                    }
                }
            }
        }

        runCatching.getOrNull() ?: createFailureResultFromException(runCatching.exceptionOrNull())
    }

    private fun createFailureResultFromException(throwable: Throwable?): Failure = Failure(
        throwable?.let { exception ->
            Error.ThrowableError(message = "Could not write to file.", cause = exception)
        } ?: Error.GenericError(message = "Could not write to file."),
    )

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
