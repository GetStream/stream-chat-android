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

package io.getstream.chat.android.ui.common.helper.internal

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper class for managing file and media attachments from device storage.
 *
 * This class provides utilities to:
 * - Query files and media from the device's MediaStore
 * - Cache remote or content URI files to local storage
 * - Parse attachment metadata from content URIs
 *
 * The class uses Android's MediaStore API to retrieve file information and should be called
 * from a background thread when querying large datasets to avoid blocking the main thread.
 *
 * @see AttachmentMetaData
 */
@Suppress("TooManyFunctions")
public class StorageHelper {
    private val dateFormat = SimpleDateFormat(TIME_FORMAT, Locale.US)

    /**
     * Retrieves or creates a cached copy of a file from the given attachment metadata.
     *
     * This method handles two scenarios:
     * 1. If [AttachmentMetaData.file] is already set, it returns the file directly
     * 2. If only [AttachmentMetaData.uri] is available, it copies the content to a cache file
     *
     * The cached file is stored in a unique timestamped folder within the app's cache directory
     * to prevent naming conflicts. The file name is derived from the attachment's title with
     * proper extension handling.
     *
     * @param context The Android context used to access the content resolver and cache directory.
     * @param attachmentMetaData The attachment metadata containing either a file or URI reference.
     * @return A [File] object pointing to the cached file, or `null` if both file and URI are null.
     *
     * @throws java.io.IOException If there's an error reading from the URI or writing to cache.
     */
    public fun getCachedFileFromUri(
        context: Context,
        attachmentMetaData: AttachmentMetaData,
    ): File? {
        if (attachmentMetaData.file == null && attachmentMetaData.uri == null) {
            return null
        }
        if (attachmentMetaData.file != null) {
            return attachmentMetaData.file!!
        }
        val cachedFile = File(getUniqueCacheFolder(context), attachmentMetaData.getTitleWithExtension())
        context.contentResolver.openInputStream(attachmentMetaData.uri!!)?.use { inputStream ->
            cachedFile.outputStream().use {
                inputStream.copyTo(it)
            }
        }

        return cachedFile
    }

    /**
     * Retrieves all file attachments from the device's external storage.
     *
     * This method queries the MediaStore for all files that have a valid MIME type, excluding
     * folders and files with unknown types. The results are sorted by date added in descending
     * order (most recent first).
     *
     * Note: This method performs a potentially expensive query operation and should be called
     * from a background thread to avoid blocking the UI.
     *
     * @param context The Android context used to access the content resolver.
     * @return A list of [AttachmentMetaData] objects representing all files on the device,
     *         or an empty list if the query fails or returns no results.
     *
     * @see getMediaAttachments For retrieving only images and videos.
     */
    public fun getFileAttachments(context: Context): List<AttachmentMetaData> {
        // Excluding files with empty mime type just to be sure that we won't include folder and unknown files
        return getFilteredAttachments(
            context,
            selection = "${MediaStore.Files.FileColumns.MIME_TYPE} IS NOT NULL " +
                "AND ${MediaStore.Files.FileColumns.MIME_TYPE} != ''",
        )
    }

    /**
     * Retrieves all media attachments (images and videos) from the device's external storage.
     *
     * This method queries the MediaStore specifically for files with media type IMAGE or VIDEO,
     * filtering out all other file types. The results are sorted by date added in descending
     * order (most recent first).
     *
     * The returned metadata includes video duration for video files, which is useful for
     * displaying in the UI or for validation purposes.
     *
     * Note: This method performs a potentially expensive query operation and should be called
     * from a background thread to avoid blocking the UI.
     *
     * @param context The Android context used to access the content resolver.
     * @return A list of [AttachmentMetaData] objects representing all images and videos on the device,
     *         or an empty list if the query fails or returns no results.
     *
     * @see getFileAttachments For retrieving all file types.
     */
    public fun getMediaAttachments(context: Context): List<AttachmentMetaData> {
        val selection = (
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE +
                " OR " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
            )
        return getFilteredAttachments(context, selection)
    }

    /**
     * Queries a list of content URIs and returns parsed attachment metadata for each.
     *
     * This method is useful when you have specific URIs (e.g., from an intent or file picker)
     * and need to extract their metadata for attachment processing. Each URI is queried
     * individually using the content resolver to retrieve:
     * - Display name
     * - MIME type (with fallback to content resolver type)
     * - File size
     *
     * URIs that cannot be queried or return no data are filtered out from the result.
     * The attachment type (image, video, or file) is automatically determined based on
     * the MIME type.
     *
     * @param context The Android context used to access the content resolver.
     * @param uriList The list of content URIs (using the `content://` scheme) to query.
     * @return A list of [AttachmentMetaData] objects with parsed metadata. URIs that fail
     *         to resolve are omitted from the result.
     *
     * @see getFileAttachments For querying all files from device storage.
     * @see getMediaAttachments For querying all media from device storage.
     */
    public fun getAttachmentsFromUriList(context: Context, uriList: List<Uri>): List<AttachmentMetaData> {
        return uriList.mapNotNull { uri ->
            val columns = arrayOf(
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.SIZE,
            )
            context.contentResolver.query(uri, columns, null, null, null)
                ?.use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val displayNameIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                        val fileSizeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                        val mimeTypeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)

                        val displayName = if (displayNameIndex != -1 && !cursor.isNull(displayNameIndex)) {
                            cursor.getString(displayNameIndex)
                        } else {
                            null
                        }

                        val fileSize = if (fileSizeIndex != -1 && !cursor.isNull(fileSizeIndex)) {
                            cursor.getLong(fileSizeIndex)
                        } else {
                            0L
                        }

                        val mimeType = if (mimeTypeIndex != -1 && !cursor.isNull(mimeTypeIndex)) {
                            cursor.getString(mimeTypeIndex)
                        } else {
                            context.contentResolver.getType(uri)
                        }

                        AttachmentMetaData(
                            uri = uri,
                            type = getModelType(mimeType),
                            mimeType = mimeType,
                            title = displayName,
                        ).apply {
                            size = fileSize
                        }
                    } else {
                        null
                    }
                }
        }
    }

    private fun getFilteredAttachments(context: Context, selection: String?): List<AttachmentMetaData> {
        val columns = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DURATION,
        )
        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            columns,
            selection,
            null,
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC",
        )?.use { cursor ->
            return mutableListOf<AttachmentMetaData>().apply {
                while (cursor.moveToNext()) {
                    add(getAttachmentFromCursor(cursor))
                }
            }
        }
        return emptyList()
    }

    private fun getAttachmentFromCursor(cursor: Cursor): AttachmentMetaData {
        val displayNameIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val fileSizeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
        val mimeTypeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
        val durationIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DURATION)

        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))

        val displayName = if (displayNameIndex != -1 && !cursor.isNull(displayNameIndex)) {
            cursor.getString(displayNameIndex)
        } else {
            null
        }

        val fileSize = if (fileSizeIndex != -1 && !cursor.isNull(fileSizeIndex)) {
            cursor.getLong(fileSizeIndex)
        } else {
            0L
        }

        val mimeType = if (mimeTypeIndex != -1 && !cursor.isNull(mimeTypeIndex)) {
            cursor.getString(mimeTypeIndex)
        } else {
            null
        }

        val duration = if (durationIndex != -1 && !cursor.isNull(fileSizeIndex)) {
            cursor.getLong(durationIndex)
        } else {
            0L
        }

        return AttachmentMetaData(
            uri = getContentUri(mimeType, id),
            mimeType = mimeType,
        ).apply {
            this.type = getModelType(mimeType)
            this.size = fileSize
            this.title = displayName
            this.videoLength = duration / MILISECOND_IN_A_SECOND
        }
    }

    private fun getContentUri(mimeType: String?, id: Long): Uri {
        val contentUri: Uri = when {
            isImage(mimeType) -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            isVideo(mimeType) -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Files.getContentUri("external")
        }
        return ContentUris.withAppendedId(contentUri, id)
    }

    private fun getModelType(mimeType: String?): String {
        return when {
            isImage(mimeType) -> AttachmentType.IMAGE
            isVideo(mimeType) -> AttachmentType.VIDEO
            else -> AttachmentType.FILE
        }
    }

    private fun isImage(mimeType: String?): Boolean {
        return mimeType?.startsWith("image") ?: false
    }

    private fun isVideo(mimeType: String?): Boolean {
        return mimeType?.startsWith("video") ?: false
    }

    private fun getUniqueCacheFolder(context: Context): File =
        File(context.cacheDir, "$FILE_NAME_PREFIX${dateFormat.format(Date().time)}").also {
            it.mkdirs()
        }

    public companion object {
        public const val TIME_FORMAT: String = "HHmmssSSS"
        public const val FILE_NAME_PREFIX: String = "STREAM_"
        private const val MILISECOND_IN_A_SECOND = 1000
    }
}

private fun AttachmentMetaData.getTitleWithExtension(): String {
    val extension = title?.substringAfterLast('.')
    val newTitle = title
        ?.replace(" ", "_")
        ?.replace("(", "_")
        ?.replace(")", "_")
    return if (extension.isNullOrEmpty() && !mimeType.isNullOrEmpty()) {
        "$newTitle.${MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)}"
    } else {
        newTitle ?: ""
    }
}
