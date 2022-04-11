/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.view.activity.AttachmentDocumentActivity
import io.getstream.chat.android.client.models.Attachment

/**
 * Shows a preview for the document in the attachment using Google Docs.
 */
public class DocumentAttachmentPreviewHandler(private val context: Context) : AttachmentPreviewHandler {

    override fun canHandle(attachment: Attachment): Boolean {
        val assetUrl = attachment.assetUrl
        val mimeType = attachment.mimeType

        if (assetUrl.isNullOrEmpty()) return false
        if (mimeType.isNullOrEmpty()) return false

        val supportedMimeTypes = listOf(
            ModelType.attach_mime_doc,
            ModelType.attach_mime_txt,
            ModelType.attach_mime_pdf,
            ModelType.attach_mime_html,
        )

        return mimeType in supportedMimeTypes ||
            // For compatibility with other client SDKs
            mimeType.contains(ModelType.attach_mime_vnd)
    }

    override fun handleAttachmentPreview(attachment: Attachment) {
        context.startActivity(AttachmentDocumentActivity.getIntent(context, attachment.assetUrl))
    }
}
