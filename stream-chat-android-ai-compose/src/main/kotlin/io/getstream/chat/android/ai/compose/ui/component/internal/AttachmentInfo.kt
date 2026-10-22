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
import java.util.Locale

/**
 * What an attachment is called: its name, else the file name its URI ends in, else what kind of
 * thing it is, such as "Image".
 */
internal fun AIMessageAttachment.displayName(context: Context): String {
    name?.takeIf { it.isNotEmpty() }?.let { return it }
    fileName()?.let { return it }
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
    mimeType?.takeIf { it.isNotEmpty() }
        ?: pathExtension()?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.lowercase(Locale.ROOT)) }

/** Whether an attachment is an image, by its kind or its media type, to pick its file icon. */
internal fun AIMessageAttachment.looksLikeImage(): Boolean =
    kind == AIMessageAttachment.Kind.Image || resolvedMimeType()?.startsWith("image/") == true

/** The extension of its name, else of the file its URI ends in. */
private fun AIMessageAttachment.pathExtension(): String? =
    listOfNotNull(name, fileName())
        .map { it.substringAfterLast('.', missingDelimiterValue = "") }
        .firstOrNull { it.isNotEmpty() && !it.contains('/') }

private fun AIMessageAttachment.fileName(): String? =
    uri?.lastPathSegment?.takeIf { it.isNotEmpty() && it != "/" }

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
