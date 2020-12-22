package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.adapter.MessageListItem.Position.BOTTOM
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.utils.extensions.isEphemeral

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
        fun hideIndicator() {
            imageView.isVisible = false
        }

        fun showIndicator(@DrawableRes drawableRes: Int) {
            imageView.isVisible = true
            imageView.setImageResource(drawableRes)
        }

        // TODO review this logic for the BOTTOM check
        if (BOTTOM !in data.positions) {
            hideIndicator()
            return
        }

        if (!data.isMine) {
            hideIndicator()
            return
        }

        when (data.message.syncStatus) {
            SyncStatus.IN_PROGRESS, SyncStatus.SYNC_NEEDED -> {
                showIndicator(R.drawable.stream_ui_ic_clock)
            }
            SyncStatus.COMPLETED -> {
                if (data.messageReadBy.isNotEmpty()) {
                    showIndicator(R.drawable.stream_ui_ic_check_all)
                } else {
                    showIndicator(R.drawable.stream_ui_ic_check_gray)
                }
            }
            SyncStatus.FAILED_PERMANENTLY -> {
                // This case is covered by a separate Decorator
                hideIndicator()
            }
        }.exhaustive

        if (data.message.isEphemeral()){
            hideIndicator()
        }
    }
}
