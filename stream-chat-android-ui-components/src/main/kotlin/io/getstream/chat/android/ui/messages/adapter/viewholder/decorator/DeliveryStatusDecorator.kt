package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.widget.ImageView
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder

internal class DeliveryStatusDecorator : BaseDecorator() {

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        setupDeliveryStateIndicator(viewHolder.binding.deliveryStatusIcon, data)
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupDeliveryStateIndicator(viewHolder.binding.deliveryStatusIcon, data)
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupDeliveryStateIndicator(viewHolder.binding.deliveryStatusIcon, data)
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupDeliveryStateIndicator(viewHolder.binding.deliveryStatusIcon, data)
    }

    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupDeliveryStateIndicator(viewHolder.binding.deliveryStatusIcon, data)
    }

    override fun decorateDeletedMessage(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) = Unit

    private fun setupDeliveryStateIndicator(imageView: ImageView, data: MessageListItem.MessageItem) {
        // TODO review this logic for the BOTTOM check
        if (data.positions.contains(MessageListItem.Position.BOTTOM).not()) {
            imageView.isVisible = false
            return
        }

        if (!data.isMine) {
            imageView.isVisible = false
            return
        }

        imageView.isVisible = true

        when (data.message.syncStatus) {
            SyncStatus.IN_PROGRESS, SyncStatus.SYNC_NEEDED -> {
                imageView.setImageResource(R.drawable.stream_ui_ic_clock)
            }
            SyncStatus.COMPLETED -> {
                if (data.messageReadBy.isNotEmpty()) {
                    imageView.setImageResource(R.drawable.stream_ui_ic_check_all)
                } else if (data.messageReadBy.isEmpty()) {
                    imageView.setImageResource(R.drawable.stream_ui_ic_check_gray)
                }
            }
            SyncStatus.FAILED_PERMANENTLY -> {
                // no direction on this yet
            }
        }
    }
}
