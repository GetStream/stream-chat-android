package com.getstream.sdk.chat.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.database.getLongOrNull
import androidx.documentfile.provider.DocumentFile
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import java.io.File
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object StorageUtils {

    private val supportedFilesMimeTypes =
        listOf(
            ModelType.attach_mime_pdf,
            ModelType.attach_mime_ppt,
            ModelType.attach_mime_csv,
            ModelType.attach_mime_xlsx,
            ModelType.attach_mime_doc,
            ModelType.attach_mime_docx,
            ModelType.attach_mime_txt,
            ModelType.attach_mime_zip,
            ModelType.attach_mime_tar,
            ModelType.attach_mime_mov,
            ModelType.attach_mime_mp3
        )
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

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
        val cachedFile = File(
            context.cacheDir,
            "STREAM_${
            dateFormat.format(
                Date().time
            )
            }_${attachmentMetaData.title}"
        )
        context.contentResolver.openInputStream(attachmentMetaData.uri!!)?.use { inputStream ->
            cachedFile.outputStream().use {
                inputStream.copyTo(it)
            }
        }

        return cachedFile
    }

    internal fun getMediaAttachments(context: Context): List<AttachmentMetaData> {
        val columns = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DURATION
        )
        val selection = (
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE +
                " OR " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
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

    internal fun getFileAttachments(context: Context, treeUri: Uri?): List<AttachmentMetaData> {
        if (treeUri == null) {
            throw IllegalStateException("Cannot get file attachments because treeUri doesn't exist")
        }
        return DocumentFile.fromTreeUri(context, treeUri)?.let {
            getFilesFromDocumentFile(it)
        } ?: emptyList()
    }

    private fun getFilesFromDocumentFile(documentFile: DocumentFile): List<AttachmentMetaData> {
        val attachmentMetaData = mutableListOf<AttachmentMetaData>()
        for (file in documentFile.listFiles()) {
            if (file.isDirectory) {
                attachmentMetaData += getFilesFromDocumentFile(file)
            } else {
                val mimeType = Utils.getMimeType(file.uri.path)
                if (supportedFilesMimeTypes.contains(mimeType)) {
                    attachmentMetaData += AttachmentMetaData(
                        file.uri,
                        mimeType
                    ).apply {
                        this.type = ModelType.attach_file
                        this.size = file.length()
                        this.title = file.name
                    }
                }
            }
        }

        return attachmentMetaData
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
                val mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(context.contentResolver.getType(uri)) ?: ""

                attachments += AttachmentMetaData(uri, mimeType).apply {
                    this.type = mediaType.modelType
                    this.size = getLong(getColumnIndex(MediaStore.Files.FileColumns.SIZE))
                    this.title = displayName
                    this.videoLength = (videoLength / 1000)
                }
            }
        }

        return attachments
    }
}

internal sealed class MediaType(val contentUri: Uri, val modelType: String)

internal object ImageMediaType :
    MediaType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ModelType.attach_image)

internal object VideoMediaType :
    MediaType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, ModelType.attach_video)
