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

package io.getstream.chat.android.client.internal.file

import android.content.Context
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.flatMap
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages file operations for the Stream Chat SDK, providing utilities for caching,
 * retrieving, and managing files in various cache directories.
 *
 * This class handles:
 * - Image caching in a dedicated directory
 * - General file caching in the Stream cache directory
 * - Timestamped cache folders for isolated file storage
 * - Cache cleanup operations
 *
 * All file operations use the application's cache directory as the root location.
 */
@InternalStreamChatApi
public class StreamFileManager {

    /**
     * Returns the directory used for caching images.
     *
     * Path: `{cacheDir}/stream_image_cache/`
     *
     * @param context Android context for accessing the cache directory.
     * @return File pointing to the image cache directory.
     */
    public fun getImageCache(context: Context): File {
        return context.cacheDir.resolve(IMAGE_CACHE_DIR)
    }

    /**
     * Creates a file reference in cache without writing content.
     *
     * This is useful for operations like MediaRecorder which need a file path
     * before actually writing any content.
     *
     * @param context Android context for cache directory access
     * @param fileName Name of the file to create
     * @return [Result.Success] with the File reference, or [Result.Failure] with an error
     */
    public fun createFileInCache(context: Context, fileName: String): Result<File> {
        return getOrCreateCacheDir(context).flatMap { cacheDir ->
            val file = File(cacheDir, fileName)
            Result.Success(file)
        }
    }

    /**
     * Writes data from an InputStream to a cache file.
     *
     * The file is created in the Stream cache directory. If a file with the same name
     * already exists, it will be overwritten.
     *
     * @param context Android context for cache directory access
     * @param fileName Name of the file to create in cache
     * @param source InputStream containing the data to cache
     * @return [Result.Success] with the cached File, or [Result.Failure] with an error
     */
    @Suppress("TooGenericExceptionCaught")
    public suspend fun writeFileInCache(
        context: Context,
        fileName: String,
        source: InputStream,
    ): Result<File> = withContext(DispatcherProvider.IO) {
        try {
            getOrCreateCacheDir(context).flatMap { cacheDir ->
                val file = File(cacheDir, fileName)
                source.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Result.Success(file)
            }
        } catch (e: Exception) {
            Result.Failure(Error.ThrowableError("Failed to write file to cache.", e))
        }
    }

    /**
     * Writes data from an InputStream to a file in a timestamped cache folder.
     *
     * This method creates a unique timestamped folder (format: `STREAM_HHmmssSSS`) and writes
     * the file there. This is useful for caching files from URIs where unique folder isolation
     * is needed to prevent naming conflicts.
     *
     * @param context Android context for cache directory access
     * @param fileName Name of the file to create in the timestamped folder
     * @param source InputStream containing the data to cache
     * @return [Result.Success] with the cached File, or [Result.Failure] with an error
     */
    @Suppress("TooGenericExceptionCaught")
    public fun writeFileInTimestampedCache(
        context: Context,
        fileName: String,
        source: InputStream,
    ): Result<File> {
        return try {
            createTimestampedCacheDir(context).flatMap { cacheDir ->
                val file = File(cacheDir, fileName)
                source.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Result.Success(file)
            }
        } catch (e: Exception) {
            Result.Failure(Error.ThrowableError("Failed to write file to timestamped cache.", e))
        }
    }

    /**
     * Retrieves a file from cache by name.
     *
     * @param context Android context for cache directory access
     * @param fileName Name of the file to retrieve
     * @return [Result.Success] with the File if it exists, or [Result.Failure] if not found
     */
    @Suppress("TooGenericExceptionCaught")
    public suspend fun getFileFromCache(
        context: Context,
        fileName: String,
    ): Result<File> = withContext(DispatcherProvider.IO) {
        try {
            getOrCreateCacheDir(context).flatMap { cacheDir ->
                val file = File(cacheDir, fileName)
                if (file.exists()) {
                    Result.Success(file)
                } else {
                    Result.Failure(Error.GenericError("File not found in cache."))
                }
            }
        } catch (e: Exception) {
            Result.Failure(Error.ThrowableError("Failed to get file from cache.", e))
        }
    }

    /**
     * Clears the Stream cache directory.
     *
     * @param context Android context for cache directory access
     * @return [Result.Success] if cache cleared successfully, or [Result.Failure] with an error
     */
    @Suppress("TooGenericExceptionCaught")
    public fun clearCache(context: Context): Result<Unit> {
        return try {
            val directory = getStreamCacheDir(context)
            directory.deleteRecursively()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(Error.ThrowableError("Could not clear Stream cache directory.", e))
        }
    }

    /**
     * Clears all cached data including Stream cache, image cache,
     * and timestamped cache folders.
     *
     * @param context Android context for cache directory access
     * @return [Result.Success] if all caches cleared successfully, or [Result.Failure] with an error
     */
    public fun clearAllCache(context: Context): Result<Unit> {
        val streamCacheResult = clearCache(context)
        val imageCacheResult = clearImageCache(context)
        val timestampedCacheResult = clearTimestampedCacheFolders(context)
        return streamCacheResult
            .flatMap { imageCacheResult }
            .flatMap { timestampedCacheResult }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun getOrCreateCacheDir(context: Context): Result<File> {
        return try {
            val cacheDir = getStreamCacheDir(context)
            cacheDir.mkdirs()
            Result.Success(cacheDir)
        } catch (e: Exception) {
            Result.Failure(Error.ThrowableError("Could not get or create cache directory.", e))
        }
    }

    private fun getStreamCacheDir(context: Context): File {
        return File(context.cacheDir, CACHE_DIR)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun createTimestampedCacheDir(context: Context): Result<File> {
        return try {
            val dateFormat = SimpleDateFormat(TIMESTAMP_FORMAT, Locale.US)
            val timestamp = dateFormat.format(Date().time)
            val dirName = "$TIMESTAMPED_DIR_PREFIX$timestamp"
            val cacheDir = File(context.cacheDir, dirName)
            cacheDir.mkdirs()
            Result.Success(cacheDir)
        } catch (e: Exception) {
            Result.Failure(Error.ThrowableError("Could not get or create cache directory.", e))
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun clearImageCache(context: Context): Result<Unit> {
        return try {
            val directory = getImageCache(context)
            directory.deleteRecursively()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(Error.ThrowableError("Could not clear image cache directory.", e))
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun clearTimestampedCacheFolders(context: Context): Result<Unit> {
        return try {
            val cacheDir = context.cacheDir
            val timestampedFolders = cacheDir.listFiles { file ->
                file.isDirectory && file.name.startsWith(TIMESTAMPED_DIR_PREFIX)
            } ?: emptyArray()

            timestampedFolders.forEach { folder ->
                folder.deleteRecursively()
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(Error.ThrowableError("Could not clear timestamped cache folders.", e))
        }
    }

    private companion object {
        private const val CACHE_DIR = "stream_cache"
        private const val IMAGE_CACHE_DIR = "stream_image_cache"
        private const val TIMESTAMP_FORMAT = "HHmmssSSS"
        private const val TIMESTAMPED_DIR_PREFIX = "STREAM_"
    }
}
