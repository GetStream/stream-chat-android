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

package io.getstream.chat.android.guides.catalog.uicomponents.customattachments.factory

import android.view.LayoutInflater
import android.view.ViewGroup
import io.getstream.chat.android.guides.databinding.ItemDateAttachmentPreviewBinding
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.AttachmentPreviewViewHolder
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.factory.AttachmentPreviewFactory

/**
 * A custom [AttachmentPreviewFactory] that adds support for date attachments in [MessageComposerView].
 */
class DateAttachmentPreviewFactory : AttachmentPreviewFactory {

    override fun canHandle(attachment: Attachment): Boolean = attachment.type == "date"

    override fun onCreateViewHolder(
        parentView: ViewGroup,
        attachmentRemovalListener: (Attachment) -> Unit,
        style: MessageComposerViewStyle?,
    ): AttachmentPreviewViewHolder = ItemDateAttachmentPreviewBinding
        .inflate(LayoutInflater.from(parentView.context), parentView, false)
        .let { DateAttachmentPreviewViewHolder(it, attachmentRemovalListener) }

    class DateAttachmentPreviewViewHolder(
        private val binding: ItemDateAttachmentPreviewBinding,
        private val attachmentRemovalListener: (Attachment) -> Unit,
    ) : AttachmentPreviewViewHolder(binding.root) {

        private lateinit var attachment: Attachment

        init {
            binding.deleteButton.setOnClickListener {
                attachmentRemovalListener(attachment)
            }
        }

        override fun bind(attachment: Attachment) {
            this.attachment = attachment

            binding.dateTextView.text = attachment.extraData["payload"].toString()
        }
    }
}
