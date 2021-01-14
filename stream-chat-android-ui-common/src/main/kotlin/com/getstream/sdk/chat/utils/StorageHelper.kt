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
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@InternalStreamChatApi
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
        attachmentMetaData: AttachmentMetaData
    ): File {
        if (attachmentMetaData.file == null && attachmentMetaData.uri == null) {
            throw IllegalStateException("Unable to create cache file for attachment: $attachmentMetaData. Either file or URI cannot be null.")
        }
        if (attachmentMetaData.file != null) {
            return attachmentMetaData.file!!
        }
        val cachedFile = File(context.cacheDir, getFileName(attachmentMetaData))
        context.contentResolver.openInputStream(attachmentMetaData.uri!!)?.use { inputStream ->
            cachedFile.outputStream().use {
                inputStream.copyTo(it)
            }
        }

        return cachedFile
    }

    public fun getFileAttachments(context: Context): List<AttachmentMetaData> {
        return getFilteredAttachments(context, null)
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
                this.videoLength = videoLength / 1000
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

    private fun getFileName(attachmentMetaData: AttachmentMetaData): String =
        "$FILE_NAME_PREFIX${dateFormat.format(Date().time)}_${attachmentMetaData.getTitleWithExtension()}"

    public companion object {
        public const val TIME_FORMAT: String = "HHmmssSSS"
        public const val FILE_NAME_PREFIX: String = "STREAM_"
    }
}

private fun AttachmentMetaData.getTitleWithExtension(): String {
    val extension = MimeTypeMap.getFileExtensionFromUrl(title)
    return if (extension.isNullOrEmpty() && !mimeType.isNullOrEmpty()) {
        "$title.${MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)}"
    } else {
        // TODO: Attachment's title should never be null. Review AttachmentMetaData class
        title ?: ""
    }
}
