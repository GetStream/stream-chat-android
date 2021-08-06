package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getPinnedText
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.TextAndAttachmentsViewHolder

internal class PinnedIndicatorDecorator : BaseDecorator() {
    override fun decorateTextAndAttachmentsMessage(
        viewHolder: TextAndAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupPinnedIndicator(viewHolder.binding.root, viewHolder.binding.pinnedByTextView, data)
    }

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupPinnedIndicator(viewHolder.binding.root, viewHolder.binding.pinnedByTextView, data)
    }

    override fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupPinnedIndicator(viewHolder.binding.root, viewHolder.binding.pinnedByTextView, data)
    }

    private fun setupPinnedIndicator(
        rootView: View,
        pinnedByTextView: TextView,
        data: MessageListItem.MessageItem,
    ) {
        if (data.message.pinned) {
            pinnedByTextView.isVisible = true
            pinnedByTextView.text = data.message.getPinnedText(pinnedByTextView.context)
            rootView.setBackgroundColor(pinnedByTextView.context.getColorCompat(R.color.stream_ui_highlight))
        } else {
            pinnedByTextView.isVisible = false
            rootView.setBackgroundColor(Color.TRANSPARENT)
        }
    }
}
