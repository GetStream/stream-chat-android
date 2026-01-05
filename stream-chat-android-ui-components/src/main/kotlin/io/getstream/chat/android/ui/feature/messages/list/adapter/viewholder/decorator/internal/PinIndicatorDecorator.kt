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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal

import android.graphics.Color
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.messages.list.MessageListItemStyle
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.BaseDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.CustomAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.FileAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.GiphyViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.LinkAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MediaAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MessageDeletedViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.PollViewHolder
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.getPinnedText
import io.getstream.chat.android.ui.utils.extensions.setStartDrawableWithSize
import io.getstream.chat.android.ui.utils.extensions.updateConstraints

/**
 * Decorator responsible for highlighting pinned messages in the message list. Apart from that,
 * shows a caption indicating that the message was pinned by a particular user.
 */
internal class PinIndicatorDecorator(private val style: MessageListItemStyle) : BaseDecorator() {

    /**
     * The type of the decorator. In this case [Decorator.Type.BuiltIn.PIN_INDICATOR].
     */
    override val type: Decorator.Type = Decorator.Type.BuiltIn.PIN_INDICATOR

    /**
     * Decorates the pin indicator of the message containing custom attachments.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateCustomAttachmentsMessage(
        viewHolder: CustomAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = with(viewHolder.binding) {
        setupPinIndicator(root, pinIndicatorTextView, data)
    }

    /**
     * Decorates the pin indicator of the Giphy attachment.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateGiphyAttachmentMessage(
        viewHolder: GiphyAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) = with(viewHolder.binding) {
        setupPinIndicator(root, pinIndicatorTextView, data)
    }

    /**
     * Decorates the pin indicator of the message containing file attachments.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateFileAttachmentsMessage(
        viewHolder: FileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = with(viewHolder.binding) {
        setupPinIndicator(root, pinIndicatorTextView, data)
    }

    /**
     * Decorates the pin indicator of messages containing image and/or video attachments.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateMediaAttachmentsMessage(
        viewHolder: MediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = with(viewHolder.binding) {
        setupPinIndicator(root, pinIndicatorTextView, data)
    }

    /**
     * Decorates the pin indicator of the plain text message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) = with(viewHolder.binding) {
        setupPinIndicator(root, pinIndicatorTextView, data)
    }

    /**
     * Does nothing for the deleted message as it can't be pinned.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    /**
     * Does nothing for the ephemeral Giphy message as it can't be pinned.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    /**
     * Decorates the pin indicator of the link attachments message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateLinkAttachmentsMessage(
        viewHolder: LinkAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = with(viewHolder.binding) {
        setupPinIndicator(root, pinIndicatorTextView, data)
    }

    /**
     * Decorates the pin indicator of the poll message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decoratePollMessage(
        viewHolder: PollViewHolder,
        data: MessageListItem.MessageItem,
    ) = with(viewHolder.binding) {
        setupPinIndicator(root, pinIndicatorTextView, data)
    }

    private fun setupPinIndicator(
        root: ConstraintLayout,
        pinIndicatorTextView: TextView,
        data: MessageListItem.MessageItem,
    ) {
        if (data.message.pinned) {
            pinIndicatorTextView.isVisible = true
            pinIndicatorTextView.text = data.message.getPinnedText(root.context)
            pinIndicatorTextView.setTextStyle(style.pinnedMessageIndicatorTextStyle)
            pinIndicatorTextView.setStartDrawableWithSize(
                style.pinnedMessageIndicatorIcon,
                R.dimen.stream_ui_message_pin_indicator_icon_size,
            )

            root.setBackgroundColor(style.pinnedMessageBackgroundColor)
            root.updateConstraints {
                val bias = if (data.isMine) 1f else 0f
                setHorizontalBias(pinIndicatorTextView.id, bias)
            }
        } else {
            pinIndicatorTextView.isVisible = false

            root.setBackgroundColor(Color.TRANSPARENT)
        }
    }
}
