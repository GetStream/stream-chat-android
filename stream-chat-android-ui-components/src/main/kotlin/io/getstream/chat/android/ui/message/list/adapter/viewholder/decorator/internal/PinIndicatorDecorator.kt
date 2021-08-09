package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import android.graphics.Color
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getPinnedText
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.TextAndAttachmentsViewHolder

/**
 * Decorator responsible for highlighting pinned messages in the message list. Apart from that,
 * shows a caption indicating that the message was pinned by a particular user.
 */
internal class PinIndicatorDecorator : BaseDecorator() {
    override fun decorateTextAndAttachmentsMessage(
        viewHolder: TextAndAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = with(viewHolder.binding) {
        setupPinIndicator(root, pinIndicatorTextView, data)
    }

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) = with(viewHolder.binding) {
        setupPinIndicator(root, pinIndicatorTextView, data)
    }

    override fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    private fun setupPinIndicator(
        root: ConstraintLayout,
        pinIndicatorTextView: TextView,
        data: MessageListItem.MessageItem,
    ) {
        val context = root.context
        if (data.message.pinned) {
            pinIndicatorTextView.isVisible = true
            pinIndicatorTextView.text = data.message.getPinnedText(context)

            root.setBackgroundColor(context.getColorCompat(R.color.stream_ui_highlight))
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
