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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal

import androidx.constraintlayout.widget.Guideline
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

internal class MaxPossibleWidthDecorator(private val style: MessageListItemStyle) : BaseDecorator() {

    /**
     * The type of the decorator. In this case [Decorator.Type.BuiltIn.MAX_POSSIBLE_WIDTH].
     */
    override val type: Decorator.Type = Decorator.Type.BuiltIn.MAX_POSSIBLE_WIDTH

    /**
     * Decorates the maximum width of the custom attachments message, by changing
     * the start and end margins of the container.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateCustomAttachmentsMessage(
        viewHolder: CustomAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)

    /**
     * Decorates the maximum width of the Giphy attachment message, by changing
     * the start and end margins of the container.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateGiphyAttachmentMessage(
        viewHolder: GiphyAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) = applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)

    /**
     * Decorates the maximum width of the file attachments message, by changing
     * the start and end margins of the container.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateFileAttachmentsMessage(
        viewHolder: FileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)

    /**
     * Decorates the maximum width of messages containing image and/or video attachments, by changing
     * the start and end margins of the container.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateMediaAttachmentsMessage(
        viewHolder: MediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)

    /**
     * Decorates the maximum width of the plain text message, by changing
     * the start and end margins of the container.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)
    }

    /**
     * Decorates the maximum width of the deleted message, by changing
     * the start and end margins of the container.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)
    }

    /**
     * Decorates the maximum width of the ephemeral Giphy message, by changing
     * the start and end margins of the container.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)
    }

    /**
     * Decorates the maximum width of the link attachments message, by changing
     * the start and end margins of the container.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateLinkAttachmentsMessage(
        viewHolder: LinkAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)
    }

    /**
     * Decorates the maximum width of the poll message, by changing
     * the start and end margins of the container.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decoratePollMessage(
        viewHolder: PollViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)
    }

    private fun applyMaxPossibleWidth(marginStart: Guideline, marginEnd: Guideline, data: MessageListItem.MessageItem) {
        val marginStartPercent = if (data.isTheirs) {
            START_PERCENT
        } else {
            START_PERCENT + getMaxWidthFactor(data.isTheirs)
        }
        val marginEndPercent = if (data.isTheirs) {
            END_PERCENT - getMaxWidthFactor(data.isTheirs)
        } else {
            END_PERCENT
        }
        marginStart.setGuidelinePercent(marginStartPercent)
        marginEnd.setGuidelinePercent(marginEndPercent)
    }

    /**
     * Gets message's max width factor from [style] based on [isTheirs]
     */
    private fun getMaxWidthFactor(isTheirs: Boolean): Float {
        val maxPossibleWidthFactor = if (isTheirs) {
            style.messageMaxWidthFactorTheirs
        } else {
            style.messageMaxWidthFactorMine
        }

        return 1 - maxPossibleWidthFactor
    }

    companion object {
        private const val START_PERCENT = 0f
        private const val END_PERCENT = 1f
    }
}
