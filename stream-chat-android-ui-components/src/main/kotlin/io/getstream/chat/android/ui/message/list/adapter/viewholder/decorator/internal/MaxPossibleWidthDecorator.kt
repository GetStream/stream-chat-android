package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import androidx.constraintlayout.widget.Guideline
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.ImageAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.TextAndAttachmentsViewHolder

internal class MaxPossibleWidthDecorator(private val style: MessageListItemStyle) : BaseDecorator() {
    override fun decorateTextAndAttachmentsMessage(
        viewHolder: TextAndAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)

    /**
     * Decorates the maximum width of the Giphy attachment, by changing the start and end margins of the container.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateGiphyAttachmentMessage(
        viewHolder: GiphyAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) = applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)

    /**
     * Decorates the maximum width of the image attachment message, by changing the start and end margins of the container.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateImageAttachmentMessage(
        viewHolder: ImageAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) = applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)
    }

    override fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)
    }

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
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
        private const val END_PERCENT = 0.97f
    }
}
