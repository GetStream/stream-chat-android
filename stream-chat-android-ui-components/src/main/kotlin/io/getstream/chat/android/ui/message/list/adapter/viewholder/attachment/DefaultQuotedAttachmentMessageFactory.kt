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

package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.view.View
import android.view.ViewGroup
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.message.list.adapter.view.internal.QuotedAttachmentView

/**
 * Factory for attachments we support by default.
 */
public class DefaultQuotedAttachmentMessageFactory : QuotedAttachmentFactory {

    /**
     * @param message The quoted message with the attachments we wish to render.
     *
     * @return If the factory can handle the given quoted message attachment or not.
     */
    override fun canHandle(message: Message): Boolean {
        val attachmentType = message.attachments.firstOrNull()?.type ?: return false

        return attachmentType == ModelType.attach_file || attachmentType == ModelType.attach_image ||
            attachmentType == ModelType.attach_giphy || attachmentType == ModelType.attach_video
    }

    /**
     * Generates a [QuotedAttachmentView] to render the attachment.
     *
     * @param message The quoted message holding the attachments.
     * @param parent The parent [ViewGroup] in which the attachment will be rendered.
     *
     * @return [QuotedAttachmentView] that will be rendered inside the quoted message.
     */
    override fun generateQuotedAttachmentView(message: Message, parent: ViewGroup): View {
        return QuotedAttachmentView(parent.context).apply {
            showAttachment(message.attachments.first())
        }
    }
}
