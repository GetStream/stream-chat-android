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
import android.os.Environment
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
 * retrieving, and managing files in various cache directories and external storage.
 *
 * This class handles:
 * - Image caching in a dedicated directory
 * - General file caching in the Stream cache directory
 * - Timestamped cache folders for isolated file storage
 * - Cache cleanup operations
 * - External storage management for photos and videos captured using the SDK
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
            if (!directory.exists()) {
                Result.Success(Unit)
            } else if (directory.deleteRecursively()) {
                Result.Success(Unit)
            } else {
                Result.Failure(Error.GenericError("Could not clear Stream cache directory."))
            }
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

    /**
     * Creates a file for photo capture in the external pictures directory.
     *
     * The file is created in the app-specific external pictures directory:
     * - Primary: `{externalFilesDir}/Pictures/STREAM_IMG_{timestamp}.jpg`
     * - Fallback: `{cacheDir}/stream_cache/STREAM_IMG_{timestamp}.jpg` (if external storage unavailable)
     *
     * The file name includes a timestamp to ensure uniqueness.
     *
     * @param context Android context for accessing storage directories
     * @return [Result.Success] with the created File, or [Result.Failure] with an error
     */
    @Suppress("TooGenericExceptionCaught")
    public fun createPhotoInExternalDir(context: Context): Result<File> {
        return try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: getStreamCacheDir(context)
            val filename = createMediaFilename(PHOTO_PREFIX, PHOTO_EXTENSION)
            Result.Success(File(dir, filename))
        } catch (e: Exception) {
            Result.Failure(Error.ThrowableError("Could not create photo file.", e))
        }
    }

    /**
     * Creates a file for video capture in the external movies directory.
     *
     * The file is created in the app-specific external movies directory:
     * - Primary: `{externalFilesDir}/Movies/STREAM_VID_{timestamp}.mp4`
     * - Fallback: `{cacheDir}/stream_cache/STREAM_VID_{timestamp}.mp4` (if external storage unavailable)
     *
     * The file name includes a timestamp to ensure uniqueness.
     *
     * @param context Android context for accessing storage directories
     * @return [Result.Success] with the created File, or [Result.Failure] with an error
     */
    @Suppress("TooGenericExceptionCaught")
    public fun createVideoInExternalDir(context: Context): Result<File> {
        return try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                ?: getStreamCacheDir(context)
            val filename = createMediaFilename(VIDEO_PREFIX, VIDEO_EXTENSION)
            Result.Success(File(dir, filename))
        } catch (e: Exception) {
            Result.Failure(Error.ThrowableError("Could not create video file.", e))
        }
    }

    /**
     * Clears Stream files from the external storage directories.
     *
     * This method removes all Stream-generated photos (STREAM_IMG_*) from the Pictures directory
     * and Stream-generated videos (STREAM_VID_*) from the Movies directory.
     *
     * Only files matching the Stream naming pattern are deleted:
     * - Photos: `STREAM_IMG_{timestamp}.jpg`
     * - Videos: `STREAM_VID_{timestamp}.mp4`
     *
     * @param context Android context for accessing external storage directories
     * @return [Result.Success] if cleanup succeeded, or [Result.Failure] with an error
     */
    @Suppress("TooGenericExceptionCaught")
    public fun clearExternalStorage(context: Context): Result<Unit> {
        return try {
            val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val moviesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)

            var allDeleted = true

            // Clear Stream photos from Pictures directory
            picturesDir?.listFiles { file ->
                file.isFile && file.name.startsWith(PHOTO_PREFIX)
            }?.forEach { file ->
                if (!file.delete()) {
                    allDeleted = false
                }
            }

            // Clear Stream videos from Movies directory
            moviesDir?.listFiles { file ->
                file.isFile && file.name.startsWith(VIDEO_PREFIX)
            }?.forEach { file ->
                if (!file.delete()) {
                    allDeleted = false
                }
            }

            if (allDeleted) {
                Result.Success(Unit)
            } else {
                Result.Failure(Error.GenericError("Could not delete all external storage files."))
            }
        } catch (e: Exception) {
            Result.Failure(Error.ThrowableError("Could not clear external storage.", e))
        }
    }

    private fun createMediaFilename(prefix: String, extension: String): String {
        val dateFormat = SimpleDateFormat(EXTERNAL_DIR_TIMESTAMP_FORMAT, Locale.US)
        return "${prefix}_${dateFormat.format(Date().time)}.$extension"
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
            val dateFormat = SimpleDateFormat(TIMESTAMPED_DIR_TIMESTAMP_FORMAT, Locale.US)
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
            if (!directory.exists()) {
                Result.Success(Unit)
            } else if (directory.deleteRecursively()) {
                Result.Success(Unit)
            } else {
                Result.Failure(Error.GenericError("Could not clear image cache directory."))
            }
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

            val allDeleted = timestampedFolders.all { folder ->
                !folder.exists() || folder.deleteRecursively()
            }

            if (allDeleted) {
                Result.Success(Unit)
            } else {
                Result.Failure(Error.GenericError("Could not clear all timestamped cache folders."))
            }
        } catch (e: Exception) {
            Result.Failure(Error.ThrowableError("Could not clear timestamped cache folders.", e))
        }
    }

    private companion object {
        private const val CACHE_DIR = "stream_cache"
        private const val IMAGE_CACHE_DIR = "stream_image_cache"
        private const val TIMESTAMPED_DIR_TIMESTAMP_FORMAT = "HHmmssSSS"
        private const val TIMESTAMPED_DIR_PREFIX = "STREAM_"
        private const val EXTERNAL_DIR_TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss"
        private const val PHOTO_PREFIX = "STREAM_IMG"
        private const val PHOTO_EXTENSION = "jpg"
        private const val VIDEO_PREFIX = "STREAM_VID"
        private const val VIDEO_EXTENSION = "mp4"
    }
}
