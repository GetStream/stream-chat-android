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
import io.getstream.chat.android.ui.feature.messages.list.background.MessageBackgroundFactory
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise

internal class BackgroundDecorator(
    private val messageBackgroundFactory: MessageBackgroundFactory,
) : BaseDecorator() {

    /**
     * The type of the decorator. In this case [Decorator.Type.BuiltIn.BACKGROUND].
     */
    override val type: Decorator.Type = Decorator.Type.BuiltIn.BACKGROUND

    /**
     * Decorates the background of the custom attachments message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateCustomAttachmentsMessage(
        viewHolder: CustomAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.textAndAttachmentMessageBackground(
                viewHolder.binding.messageContainer.context,
                data,
            )
    }

    /**
     * Decorates the background of the Giphy attachment.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateGiphyAttachmentMessage(
        viewHolder: GiphyAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.textAndAttachmentMessageBackground(
                viewHolder.binding.messageContainer.context,
                data,
            )
    }

    /**
     * Decorates the background of the file attachments message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateFileAttachmentsMessage(
        viewHolder: FileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.fileAttachmentsMessageBackground(
                viewHolder.binding.messageContainer.context,
                data,
            )
    }

    /**
     * Decorates the backgrounds of messages containing image and/or video attachments.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateMediaAttachmentsMessage(
        viewHolder: MediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.imageAttachmentMessageBackground(
                viewHolder.binding.messageContainer.context,
                data,
            )
    }

    /**
     * Decorates the background of the deleted message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.deletedMessageBackground(
                viewHolder.binding.messageContainer.context,
                data,
            )
    }

    /**
     * Decorates the background of the plain text message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.plainTextMessageBackground(
                viewHolder.binding.messageContainer.context,
                data,
            )
    }

    /**
     * Decorates the background of the poll message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decoratePollMessage(viewHolder: PollViewHolder, data: MessageListItem.MessageItem) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.pollMessageBackground(
                viewHolder.binding.messageContainer.context,
                data,
            )
    }

    /**
     * Decorates the background of the ephemeral Giphy message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.cardView.background =
            messageBackgroundFactory.giphyAppearanceModel(viewHolder.binding.cardView.context)
    }

    /**
     * Decorates the background of the message container.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateLinkAttachmentsMessage(
        viewHolder: LinkAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        viewHolder.binding.messageContainer.background =
            messageBackgroundFactory.linkAttachmentMessageBackground(
                viewHolder.binding.messageContainer.context,
                data,
            )
    }

    companion object {
        internal val DEFAULT_CORNER_RADIUS = 14.dpToPxPrecise()
    }
}
