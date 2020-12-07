package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder

internal class MaxPossibleWidthDecorator : BaseDecorator() {
    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setMaxWidthToView(viewHolder.binding.backgroundView)
    }

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        setMaxWidthToView(viewHolder.binding.messageText)
    }

    override fun decorateDeletedMessage(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) = Unit

    private fun setMaxWidthToView(view: View) {
        view.post {
            view.layoutParams = (view.layoutParams as ConstraintLayout.LayoutParams).apply {
                matchConstraintPercentWidth = MAX_POSSIBLE_WIDTH_FACTOR
            }
        }
    }

    companion object {
        internal const val MAX_POSSIBLE_WIDTH_FACTOR = 5f / 7
    }
}
