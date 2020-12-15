package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder

internal class MaxPossibleWidthDecorator : BaseDecorator() {
    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        viewHolder.binding.mediaAttachmentsGroupView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            matchConstraintPercentWidth = MAX_POSSIBLE_WIDTH_FACTOR
        }
    }

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        viewHolder.binding.messageText.post {
            viewHolder.binding.messageText.updateLayoutParams<ConstraintLayout.LayoutParams> {
                matchConstraintMaxWidth = maxWidth(viewHolder.binding.root)
            }
        }
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        viewHolder.binding.backgroundView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            matchConstraintPercentWidth = MAX_POSSIBLE_WIDTH_FACTOR
        }
    }

    private fun maxWidth(parent: ViewGroup) =
        ((parent.measuredWidth - parent.paddingLeft - parent.paddingRight) * MAX_POSSIBLE_WIDTH_FACTOR).toInt()

    companion object {
        private const val MAX_POSSIBLE_WIDTH_FACTOR = 5f / 7
    }
}
