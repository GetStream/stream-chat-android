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

package io.getstream.chat.android.ui.common.feature.documents

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.R
import io.getstream.chat.android.ui.common.internal.file.StreamShareFileManager
import io.getstream.chat.android.uiutils.model.MimeType
import io.getstream.log.taggedLogger
import kotlinx.coroutines.launch

/**
 * Shared handler for opening document attachments.
 *
 * Text-based files (TXT, HTML) are displayed in-app using [TextFilePreviewActivity].
 * All other document types (PDF, Office formats, etc.) are downloaded via [StreamShareFileManager]
 * and opened with an external application.
 */
@InternalStreamChatApi
public object DocumentAttachmentHandler {

    private val logger by taggedLogger("Chat:DocumentAttachmentHandler")
    private val shareFileManager = StreamShareFileManager()

    /**
     * Opens the given document [attachment].
     *
     * @param context Must be a [LifecycleOwner] (e.g. an Activity) for external-app downloads.
     * @param attachment The document attachment to open.
     */
    public fun openAttachment(context: Context, attachment: Attachment) {
        val mimeType = attachment.mimeType
        val url = attachment.assetUrl ?: return

        if (mimeType == MimeType.MIME_TYPE_TXT || mimeType == MimeType.MIME_TYPE_HTML) {
            context.startActivity(
                TextFilePreviewActivity.getIntent(
                    context = context,
                    url = url,
                    mimeType = mimeType,
                    fileName = attachment.name,
                ),
            )
        } else {
            openWithExternalApp(context, attachment)
        }
    }

    private fun openWithExternalApp(context: Context, attachment: Attachment) {
        val lifecycleOwner = context.findLifecycleOwner() ?: run {
            logger.e { "[openWithExternalApp] Could not find a LifecycleOwner from Context. Cannot download file." }
            return
        }

        lifecycleOwner.lifecycleScope.launch {
            Toast.makeText(context, R.string.stream_ui_message_list_attachment_opening, Toast.LENGTH_SHORT).show()
            shareFileManager.writeAttachmentToShareableFile(context, attachment)
                .onSuccess { uri ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, attachment.mimeType)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, attachment.name))
                    } catch (e: ActivityNotFoundException) {
                        logger.e(e) { "[openWithExternalApp] No app available to open file." }
                        Toast.makeText(context, R.string.stream_ui_message_list_attachment_no_app, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .onError {
                    logger.e { "[openWithExternalApp] Failed to download file: ${it.message}" }
                    val msg = context.getString(
                        R.string.stream_ui_message_list_attachment_download_failed,
                        attachment.name ?: "",
                    )
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
        }
    }

    /**
     * Walks the [Context] wrapper chain to find the underlying [LifecycleOwner].
     * Handles [ContextThemeWrapper] and other [ContextWrapper] layers that may wrap an Activity.
     */
    private fun Context.findLifecycleOwner(): LifecycleOwner? {
        var ctx: Context? = this
        while (ctx != null) {
            if (ctx is LifecycleOwner) return ctx
            ctx = (ctx as? ContextWrapper)?.baseContext
        }
        return null
    }
}
