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

package io.getstream.chat.android.guides.catalog.uicomponents.customattachments.input.factory

import android.view.LayoutInflater
import android.view.ViewGroup
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.guides.databinding.ItemDateAttachmentPreviewBinding
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.message.input.attachment.selected.internal.BaseSelectedCustomAttachmentViewHolder
import io.getstream.chat.android.ui.message.input.attachment.selected.internal.SelectedCustomAttachmentViewHolderFactory

/**
 * A custom [SelectedCustomAttachmentViewHolderFactory] that adds support for date attachments
 * in [MessageInputView].
 */
class DateAttachmentPreviewFactory : SelectedCustomAttachmentViewHolderFactory {

    override fun createAttachmentViewHolder(
        attachments: List<Attachment>,
        parent: ViewGroup,
    ): BaseSelectedCustomAttachmentViewHolder {
        return ItemDateAttachmentPreviewBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::DateAttachmentPreviewViewHolder)
    }

    class DateAttachmentPreviewViewHolder(
        private val binding: ItemDateAttachmentPreviewBinding,
    ) : BaseSelectedCustomAttachmentViewHolder(binding.root) {

        override fun bind(attachment: Attachment, onAttachmentCancelled: (Attachment) -> Unit) {
            binding.dateTextView.text = attachment.extraData["payload"].toString()
            binding.deleteButton.setOnClickListener {
                onAttachmentCancelled(attachment)
            }
        }
    }
}
