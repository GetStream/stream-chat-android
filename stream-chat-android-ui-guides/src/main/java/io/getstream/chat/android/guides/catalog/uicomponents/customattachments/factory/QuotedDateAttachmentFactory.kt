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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import io.getstream.chat.android.guides.databinding.ViewQuotedDateAttachmentBinding
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.QuotedAttachmentFactory

/**
 * A custom [QuotedAttachmentFactory] that adds support for quoted date attachments.
 */
class QuotedDateAttachmentFactory : QuotedAttachmentFactory {
    override fun canHandle(message: Message): Boolean = message.attachments.any { it.type == "date" }

    override fun generateQuotedAttachmentView(message: Message, parent: ViewGroup): View = QuotedDateAttachmentView(parent.context).apply {
        showDate(message.attachments.first())
    }

    class QuotedDateAttachmentView(context: Context) : FrameLayout(context) {

        private val binding = ViewQuotedDateAttachmentBinding.inflate(LayoutInflater.from(context), this)

        fun showDate(attachment: Attachment) {
            binding.dateTextView.text = attachment.extraData["payload"]
                .toString()
                .replace(",", "\n")
        }
    }
}
