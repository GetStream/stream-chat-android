package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.widget.ImageView
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.ui.messages.adapter.viewholder.GiphyViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder

internal class FailedIndicatorDecorator : BaseDecorator() {

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupFailedIndicator(viewHolder.binding.deliveryFailedIcon, data)
    }

    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupFailedIndicator(viewHolder.binding.deliveryFailedIcon, data)
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupFailedIndicator(viewHolder.binding.deliveryFailedIcon, data)
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupFailedIndicator(viewHolder.binding.deliveryFailedIcon, data)
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupFailedIndicator(viewHolder.binding.deliveryFailedIcon, data)
    }

    override fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    private fun setupFailedIndicator(
        deliveryFailedIcon: ImageView,
        data: MessageListItem.MessageItem,
    ) {
        val isFailed = data.isMine && data.message.syncStatus == SyncStatus.FAILED_PERMANENTLY
        deliveryFailedIcon.isVisible = isFailed
    }
}
