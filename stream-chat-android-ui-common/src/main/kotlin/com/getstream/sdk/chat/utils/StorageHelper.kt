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

package com.getstream.sdk.chat.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.database.getLongOrNull
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@InternalStreamChatApi
@OptIn(ExperimentalStreamChatApi::class)
@Suppress("TooManyFunctions")
public class StorageHelper {
    private val dateFormat = SimpleDateFormat(TIME_FORMAT, Locale.US)
    private val projection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.MIME_TYPE,
        MediaStore.Files.FileColumns.SIZE,
        MediaStore.Video.Media.DURATION
    )

    public fun getCachedFileFromUri(
        context: Context,
        attachmentMetaData: AttachmentMetaData,
    ): File {
        if (attachmentMetaData.file == null && attachmentMetaData.uri == null) {
            throw IllegalStateException(
                "Unable to create cache file for attachment: $attachmentMetaData. " +
                    "Either file or URI cannot be null."
            )
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

    public fun getFileAttachments(context: Context): List<AttachmentMetaData> {
        // Excluding files with empty mime type just to be sure that we won't include folder and unknown files
        return getFilteredAttachments(
            context,
            selection = "${MediaStore.Files.FileColumns.MIME_TYPE} IS NOT NULL " +
                "AND ${MediaStore.Files.FileColumns.MIME_TYPE} != ''",
        )
    }

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

    private fun getFilteredAttachments(context: Context, selection: String?): List<AttachmentMetaData> {
        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            null,
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            return mutableListOf<AttachmentMetaData>().apply {
                while (cursor.moveToNext()) {
                    add(getAttachmentFromCursor(cursor))
                }
            }
        }
        return emptyList()
    }

    public fun getAttachmentsFromUriList(context: Context, uriList: List<Uri>): List<AttachmentMetaData> {
        return uriList.mapNotNull { uri ->
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                cursor.moveToFirst()
                getAttachmentFromCursor(cursor, uri)
            }
        }
    }

    private fun getAttachmentFromCursor(cursor: Cursor, contentUri: Uri? = null): AttachmentMetaData {

        with(cursor) {
            val id = getLong(getColumnIndex(MediaStore.Files.FileColumns._ID))
            val mimeType = getString(getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE))
            val title = getString(getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME))
            val size = getLong(getColumnIndex(MediaStore.Files.FileColumns.SIZE))
            val videoLength = getLongOrNull(getColumnIndex(MediaStore.Video.Media.DURATION)) ?: 0

            val uri = contentUri ?: getContentUri(mimeType, id)
            return AttachmentMetaData(uri = uri, mimeType = mimeType).apply {
                this.type = getModelType(mimeType)
                this.size = size
                this.title = title
                this.videoLength = videoLength / MILISECOND_IN_A_SECOND
            }
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
            isImage(mimeType) -> ModelType.attach_image
            isVideo(mimeType) -> ModelType.attach_video
            else -> ModelType.attach_file
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
