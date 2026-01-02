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

package io.getstream.chat.ui.sample.feature.chat.messagelist.decorator

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import io.getstream.chat.android.ui.common.utils.Utils
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.BaseDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.CustomAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.FileAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.LinkAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MediaAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MessageDeletedViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MessagePlainTextViewHolder
import io.getstream.chat.ui.sample.R

class DeletedForMeDecorator : BaseDecorator() {

    override val type: Decorator.Type = CustomDecoratorType.DELETED_FOR_ME

    private val deletedForMeViewId = View.generateViewId()

    override fun decorateDeletedMessage(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) {
        val container = (viewHolder.binding.footnote as View)
            .findViewById<ViewGroup>(R.id.messageFooterContainerInner)
        setupDeletedForMeView(container, data)
    }

    override fun decorateCustomAttachmentsMessage(
        viewHolder: CustomAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        /* no-op */
    }

    override fun decorateGiphyAttachmentMessage(
        viewHolder: GiphyAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        /* no-op */
    }

    override fun decorateFileAttachmentsMessage(
        viewHolder: FileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        /* no-op */
    }

    override fun decorateMediaAttachmentsMessage(
        viewHolder: MediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        /* no-op */
    }

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        /* no-op */
    }

    override fun decorateLinkAttachmentsMessage(
        viewHolder: LinkAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        /* no-op */
    }

    private fun setupDeletedForMeView(
        container: ViewGroup,
        data: MessageListItem.MessageItem,
    ) {
        var textView = container.findViewById<TextView>(deletedForMeViewId)
        if (textView == null && data.message.deletedForMe) {
            textView = createTextView(container)
            container.addView(textView, 0)
        }
    }

    private fun createTextView(container: ViewGroup) = TextView(container.context).apply {
        id = deletedForMeViewId
        layoutParams = LinearLayoutCompat.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
            marginStart = Utils.dpToPx(MARGIN_START_DP)
            marginEnd = Utils.dpToPx(MARGIN_END_DP)
        }
        TextViewCompat.setTextAppearance(this, R.style.StreamUiTextAppearance_Footnote)
        setTextColor(ContextCompat.getColor(container.context, R.color.stream_ui_text_color_primary))
        text = "Deleted only for me"
    }
}

private const val MARGIN_START_DP = 8
private const val MARGIN_END_DP = 8
