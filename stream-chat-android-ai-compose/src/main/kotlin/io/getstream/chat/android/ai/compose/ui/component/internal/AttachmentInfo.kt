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

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.text.format.Formatter
import android.webkit.MimeTypeMap
import android.widget.Toast
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.util.internal.fileName
import io.getstream.chat.android.ai.compose.util.internal.knownMimeType
import io.getstream.chat.android.ai.compose.util.internal.pathExtension
import java.util.Locale

/**
 * What an attachment is called: its name, else the file name its URI ends in, else what kind of
 * thing it is, such as "Image".
 */
internal fun AIMessageAttachment.displayName(context: Context): String {
    name?.takeIf { it.isNotEmpty() }?.let { return it }
    fileName(uri)?.let { return it }
    return context.getString(
        if (kind == AIMessageAttachment.Kind.Image) {
            R.string.stream_ai_compose_conversation_attachment_image
        } else {
            R.string.stream_ai_compose_conversation_attachment_file
        },
    )
}

/** An attachment's type and size, such as "PDF · 2.4 MB", or as much of them as it has. */
internal fun AIMessageAttachment.details(context: Context): String {
    val type = typeLabel()
    val formattedSize = size?.let { Formatter.formatShortFileSize(context, it) }
    return when {
        type != null && formattedSize != null ->
            context.getString(R.string.stream_ai_compose_conversation_attachment_details, type, formattedSize)
        else -> type ?: formattedSize.orEmpty()
    }
}

/** An attachment's type for people to read, such as "PDF": its extension, else its media type's. */
internal fun AIMessageAttachment.typeLabel(): String? =
    (pathExtension() ?: mimeType?.let(MimeTypeMap.getSingleton()::getExtensionFromMimeType))
        ?.uppercase(Locale.ROOT)

/** An attachment's media type: the one it has, else the one its extension says. */
internal fun AIMessageAttachment.resolvedMimeType(): String? =
    knownMimeType(mimeType)
        ?: pathExtension()?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.lowercase(Locale.ROOT)) }
        ?: mimeType?.takeIf { it.isNotEmpty() }

/** The types of file that have an icon of their own. */
internal enum class AttachmentFileType { File, Pdf, Image, Audio, Video, Archive }

/**
 * An attachment's type of file, to pick its icon: from its media type, else its extension, else
 * its kind. A type without an icon of its own is a plain [AttachmentFileType.File].
 */
internal fun AIMessageAttachment.fileType(): AttachmentFileType {
    val declared = knownMimeType(mimeType)
    if (declared != null) return fileTypeOfMimeType(declared)
    val extension = pathExtension()?.lowercase(Locale.ROOT)
    val fromExtension = extension?.let { ext ->
        ExtensionFileTypes[ext] ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)?.let(::fileTypeOfMimeType)
    }
    // A wildcard such as `image/*` says no more than the family, which still picks an icon.
    val fromFamily = mimeType?.let(::fileTypeOfMimeType)?.takeIf { it != AttachmentFileType.File }
    return when {
        fromExtension != null -> fromExtension
        fromFamily != null -> fromFamily
        kind == AIMessageAttachment.Kind.Image -> AttachmentFileType.Image
        else -> AttachmentFileType.File
    }
}

private fun fileTypeOfMimeType(mimeType: String): AttachmentFileType {
    val type = mimeType.substringBefore(';').trim().lowercase(Locale.ROOT)
    return when {
        type == "application/pdf" -> AttachmentFileType.Pdf
        type.startsWith("image/") -> AttachmentFileType.Image
        type.startsWith("audio/") -> AttachmentFileType.Audio
        type.startsWith("video/") -> AttachmentFileType.Video
        type in ArchiveMimeTypes -> AttachmentFileType.Archive
        else -> AttachmentFileType.File
    }
}

private val ArchiveMimeTypes = setOf(
    "application/zip",
    "application/x-zip-compressed",
    "application/vnd.rar",
    "application/x-rar-compressed",
    "application/x-7z-compressed",
    "application/x-tar",
    "application/gzip",
    "application/x-gzip",
    "application/x-bzip",
    "application/x-bzip2",
    "application/x-xz",
    "application/x-compress",
    "application/x-compressed",
    "application/zstd",
    "application/x-zstd",
)

/** Extensions the platform's map of media types may not know, by the type of file they are. */
private val ExtensionFileTypes: Map<String, AttachmentFileType> = buildMap {
    put("pdf", AttachmentFileType.Pdf)
    listOf("zip", "rar", "7z", "tar", "gz", "tgz", "bz2", "tbz2", "xz", "txz", "zst")
        .forEach { put(it, AttachmentFileType.Archive) }
}

/** The extension of its name, else of the file its URI ends in. */
internal fun AIMessageAttachment.pathExtension(): String? = pathExtension(name, uri)

/**
 * Opens an attachment's file in an app that shows it: a page on the web in the browser, and a
 * file on this device in an app for its type. Says so when no app can.
 */
// No app for it, or a `file://` URI, which other apps aren't let to read, each throw.
@Suppress("TooGenericExceptionCaught")
internal fun openAttachment(context: Context, attachment: AIMessageAttachment) {
    val uri = attachment.uri ?: return
    val intent = Intent(Intent.ACTION_VIEW).apply {
        if (uri.scheme == "http" || uri.scheme == "https") {
            data = uri
        } else {
            val type = attachment.resolvedMimeType()
                ?: runCatching { context.contentResolver.getType(uri) }.getOrNull()
                ?: "*/*"
            setDataAndType(uri, type)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (context.findActivity() == null) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: RuntimeException) {
        Toast.makeText(context, R.string.stream_ai_compose_conversation_attachment_cannot_open, Toast.LENGTH_SHORT)
            .show()
    }
}

/** The activity this context belongs to, or null when it belongs to none. */
internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
