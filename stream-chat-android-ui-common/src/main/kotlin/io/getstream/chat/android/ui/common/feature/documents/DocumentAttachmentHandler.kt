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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.R
import io.getstream.chat.android.ui.common.feature.documents.DocumentAttachmentHandler.SNACKBAR_DELAY_MS
import io.getstream.chat.android.ui.common.internal.file.StreamShareFileManager
import io.getstream.chat.android.ui.common.utils.MediaStringUtil
import io.getstream.log.taggedLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Shared handler for opening document attachments.
 *
 * Documents are downloaded via [StreamShareFileManager] and opened with an external application.
 * If the download takes longer than [SNACKBAR_DELAY_MS], a Snackbar with progress is shown.
 */
@InternalStreamChatApi
public object DocumentAttachmentHandler {

    private val logger by taggedLogger("Chat:DocumentAttachmentHandler")
    private val shareFileManager = StreamShareFileManager()

    private const val SNACKBAR_DELAY_MS = 500L

    /**
     * Opens the given document [attachment].
     *
     * @param context Must be a [LifecycleOwner] (e.g. an Activity) for external-app downloads.
     * @param attachment The document attachment to open.
     */
    public fun openAttachment(context: Context, attachment: Attachment) {
        openWithExternalApp(context, attachment)
    }

    private fun openWithExternalApp(context: Context, attachment: Attachment) {
        val lifecycleOwner = context.findLifecycleOwner() ?: run {
            logger.e { "[openWithExternalApp] Could not find a LifecycleOwner from Context. Cannot download file." }
            return
        }

        val rootView = context.findActivity()?.findViewById<android.view.View>(android.R.id.content)

        lifecycleOwner.lifecycleScope.launch {
            var snackbar: Snackbar? = null
            val snackbarJob = rootView?.let {
                launch {
                    delay(SNACKBAR_DELAY_MS)
                    snackbar = Snackbar.make(
                        it,
                        context.getString(
                            R.string.stream_ui_message_list_attachment_downloading,
                            MediaStringUtil.convertFileSizeByteCount(0L),
                            MediaStringUtil.convertFileSizeByteCount(attachment.fileSize.toLong()),
                        ),
                        Snackbar.LENGTH_INDEFINITE,
                    ).also { sb -> sb.show() }
                }
            }

            shareFileManager.writeAttachmentToShareableFile(
                context = context,
                attachment = attachment,
                onProgress = { bytesDownloaded, totalBytes ->
                    snackbar?.let { sb ->
                        val downloaded = MediaStringUtil.convertFileSizeByteCount(bytesDownloaded)
                        val total = MediaStringUtil.convertFileSizeByteCount(totalBytes)
                        val text = context.getString(
                            R.string.stream_ui_message_list_attachment_downloading,
                            downloaded,
                            total,
                        )
                        sb.view.post { sb.setText(text) }
                    }
                },
            )
                .onSuccess { uri ->
                    snackbarJob?.cancel()
                    snackbar?.dismiss()
                    openFileUri(context, uri, attachment)
                }
                .onError { error ->
                    snackbarJob?.cancel()
                    snackbar?.dismiss()
                    logger.e { "[openWithExternalApp] Failed to download file: ${error.message}" }
                    val msg = context.getString(
                        R.string.stream_ui_message_list_attachment_download_failed,
                        attachment.name ?: "",
                    )
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openFileUri(context: Context, uri: android.net.Uri, attachment: Attachment) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, attachment.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, attachment.name))
        } catch (e: ActivityNotFoundException) {
            logger.e(e) { "[openFileUri] No app available to open file." }
            Toast.makeText(context, R.string.stream_ui_message_list_attachment_no_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun Context.findLifecycleOwner(): LifecycleOwner? {
        var ctx: Context? = this
        while (ctx != null) {
            if (ctx is LifecycleOwner) return ctx
            ctx = (ctx as? ContextWrapper)?.baseContext
        }
        return null
    }

    private fun Context.findActivity(): Activity? {
        var ctx: Context? = this
        while (ctx != null) {
            if (ctx is Activity) return ctx
            ctx = (ctx as? ContextWrapper)?.baseContext
        }
        return null
    }
}
