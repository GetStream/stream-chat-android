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
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.feature.documents.AttachmentDocumentActivity
import io.getstream.chat.android.ui.common.feature.documents.DocumentAttachmentHandler
import io.getstream.chat.android.uiutils.model.MimeType

/**
 * Shows a preview for document attachments.
 *
 * Behavior depends on [useDocumentGView]:
 * - `true` (default): documents are rendered via Google Docs Viewer.
 * - `false`: text-based files (TXT, HTML) are rendered in-app, others open with an external app.
 *
 * Set via `ChatTheme(useDocumentGView = false)`.
 */
public class DocumentAttachmentPreviewHandler(
    private val context: Context,
    private val useDocumentGView: Boolean = true,
) : AttachmentPreviewHandler {

    override fun canHandle(attachment: Attachment): Boolean {
        val assetUrl = attachment.assetUrl
        val mimeType = attachment.mimeType

        if (assetUrl.isNullOrEmpty() || mimeType.isNullOrEmpty()) return false

        val supportedMimeTypes = listOf(
            MimeType.MIME_TYPE_DOC,
            MimeType.MIME_TYPE_TXT,
            MimeType.MIME_TYPE_PDF,
            MimeType.MIME_TYPE_HTML,
        )

        return mimeType in supportedMimeTypes ||
            // For compatibility with other client SDKs
            mimeType.contains(MimeType.MIME_TYPE_VND)
    }

    override fun handleAttachmentPreview(attachment: Attachment) {
        @Suppress("DEPRECATION")
        if (useDocumentGView) {
            context.startActivity(AttachmentDocumentActivity.getIntent(context, attachment.assetUrl))
        } else {
            DocumentAttachmentHandler.openAttachment(context, attachment)
        }
    }
}
