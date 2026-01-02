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

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType

/**
 * Shows a preview for an URL in the attachment using the [Intent.ACTION_VIEW] action.
 */
public class UrlAttachmentPreviewHandler(private val context: Context) : AttachmentPreviewHandler {

    override fun canHandle(attachment: Attachment): Boolean {
        return !getAttachmentUrl(attachment).isNullOrEmpty()
    }

    override fun handleAttachmentPreview(attachment: Attachment) {
        val url = getAttachmentUrl(attachment)
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
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
