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

package io.getstream.chat.android.compose.ui.attachments.preview.handler

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.common.R as UiCommonR

/**
 * Shows a preview for an URL in the attachment using the [Intent.ACTION_VIEW] action.
 */
public class UrlAttachmentPreviewHandler(private val context: Context) : AttachmentPreviewHandler {

    override fun canHandle(attachment: Attachment): Boolean {
        return !getAttachmentUrl(attachment).isNullOrEmpty()
    }

    override fun handleAttachmentPreview(attachment: Attachment) {
        val url = getAttachmentUrl(attachment)
        val uri = url?.toUri()
        val intent = if (attachment.mimeType != null) {
            Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, attachment.mimeType) }
        } else {
            Intent(Intent.ACTION_VIEW, uri)
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(UiCommonR.string.stream_ui_message_list_error_cannot_open_link, url),
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    private fun getAttachmentUrl(attachment: Attachment): String? {
        with(attachment) {
            return when (type) {
                AttachmentType.IMAGE -> {
                    when {
                        titleLink != null -> titleLink
                        ogUrl != null -> ogUrl
                        assetUrl != null -> assetUrl
                        else -> imageUrl
                    }
                }

                else -> assetUrl
            }
        }
    }
}
