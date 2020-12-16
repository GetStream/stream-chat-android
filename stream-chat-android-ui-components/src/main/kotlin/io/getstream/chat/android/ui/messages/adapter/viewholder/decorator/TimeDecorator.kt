package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.widget.TextView
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.formatTime
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.utils.extensions.getCreatedAtOrThrow

internal class TimeDecorator(private val dateFormatter: DateFormatter) : BaseDecorator() {

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        setupTime(viewHolder.binding.tvTime, data)
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupTime(viewHolder.binding.tvTime, data)
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupTime(viewHolder.binding.tvTime, data)
    }

    private fun setupTime(textView: TextView, data: MessageListItem.MessageItem) {
        textView.text = dateFormatter.formatTime(data.message.getCreatedAtOrThrow())
    }
}
