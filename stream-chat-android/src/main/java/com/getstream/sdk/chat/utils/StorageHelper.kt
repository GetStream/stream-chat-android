package com.getstream.sdk.chat.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.database.getLongOrNull
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.core.internal.InternalStreamChatApi
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@InternalStreamChatApi
public class StorageHelper {
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US)

    internal fun getCachedFileFromUri(
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
        val columns = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DURATION
        )
        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            columns,
            selection,
            null,
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        )?.use {
            return getAttachmentsFromCursor(context, it)
        }
        return emptyList()
    }

    public fun getAttachmentsFromUriList(
        context: Context,
        uriList: List<Uri>
    ): List<AttachmentMetaData> {
        return uriList.mapNotNull { uri ->
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
                AttachmentMetaData(
                    uri = uri,
                    type = ModelType.attach_file,
                    mimeType = context.contentResolver.getType(uri),
                    title = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                ).apply {
                    this.size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE))
                }
            }
        }
    }

    private fun getAttachmentsFromCursor(
        context: Context,
        cursor: Cursor
    ): List<AttachmentMetaData> {
        val attachments = mutableListOf<AttachmentMetaData>()
        with(cursor) {
            for (i in 0 until count) {
                moveToPosition(i)
                val type = getInt(getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE))
                val mediaType = if (type == Constant.MEDIA_TYPE_IMAGE) {
                    ImageMediaType
                } else if (type == Constant.MEDIA_TYPE_VIDEO) {
                    VideoMediaType
                } else {
                    continue
                }

                val uri = ContentUris.withAppendedId(
                    mediaType.contentUri,
                    getLong(getColumnIndex(MediaStore.Files.FileColumns._ID))
                )
                val displayName =
                    getString(getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME))
                val videoLength =
                    getLongOrNull(getColumnIndex(MediaStore.Files.FileColumns.DURATION)) ?: 0
                val mimeType = context.contentResolver.getType(uri)

                attachments += AttachmentMetaData(uri = uri, mimeType = mimeType).apply {
                    this.type = mediaType.modelType
                    this.size = getLong(getColumnIndex(MediaStore.Files.FileColumns.SIZE))
                    this.title = displayName
                    this.videoLength = (videoLength / 1000)
                }
            }
        }

        return attachments
    }

    private fun getFileName(attachmentMetaData: AttachmentMetaData): String =
        "STREAM_${dateFormat.format(Date().time)}_${attachmentMetaData.getTitleWithExtension()}"
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

internal sealed class MediaType(val contentUri: Uri, val modelType: String)

internal object ImageMediaType :
    MediaType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ModelType.attach_image)

internal object VideoMediaType :
    MediaType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, ModelType.attach_video)
