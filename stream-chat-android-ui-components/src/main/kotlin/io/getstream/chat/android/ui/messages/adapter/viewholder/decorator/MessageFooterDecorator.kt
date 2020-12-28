package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.widget.TextView
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.formatTime
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.messages.adapter.viewholder.GiphyViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.utils.extensions.getCreatedAtOrNull
import io.getstream.chat.android.ui.utils.extensions.isEphemeral
import io.getstream.chat.android.ui.utils.extensions.leftDrawable

internal class MessageFooterDecorator(private val dateFormatter: DateFormatter) : BaseDecorator() {

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        setupMessageEphemeralFooterLabel(viewHolder.binding.footnote.messageFooter, data)
        setupMessageFooterTime(viewHolder.binding.footnote.timeView, data)
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupMessageEphemeralFooterLabel(viewHolder.binding.footnote.messageFooter, data)
        setupMessageFooterTime(viewHolder.binding.footnote.timeView, data)
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupMessageEphemeralFooterLabel(viewHolder.binding.footnote.messageFooter, data)
        setupMessageFooterTime(viewHolder.binding.footnote.timeView, data)
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupMessageEphemeralFooterLabel(viewHolder.binding.footnote.messageFooter, data)
        setupMessageFooterTime(viewHolder.binding.footnote.timeView, data)
    }

    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupMessageEphemeralFooterLabel(viewHolder.binding.footnote.messageFooter, data)
        setupMessageFooterTime(viewHolder.binding.footnote.timeView, data)
    }

    override fun decorateDeletedMessage(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) = Unit

    override fun decorateGiphyMessage(viewHolder: GiphyViewHolder, data: MessageListItem.MessageItem) {
        with(viewHolder.binding) {
            setupMessageEphemeralFooterLabel(footnote.messageFooter, data)
            setupMessageFooterTime(footnote.timeView, data)
        }
    }

    private fun setupMessageEphemeralFooterLabel(textView: TextView, data: MessageListItem.MessageItem) {
        when {
            data.positions.contains(MessageListItem.Position.BOTTOM).not() || !data.message.isEphemeral() -> {
                textView.isVisible = false
            }
            else -> {
                textView.apply {
                    isVisible = true
                    text = context.getString(R.string.stream_ui_ephemeral_msg_footer)
                    leftDrawable(R.drawable.stream_ui_ic_icon_eye_off)
                    compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.stream_ui_spacing_small)
                }
            }
        }
    }

    private fun setupMessageFooterTime(textView: TextView, data: MessageListItem.MessageItem) {
        val createdAt = data.message.getCreatedAtOrNull()
        when {
            data.positions.contains(MessageListItem.Position.BOTTOM).not() || createdAt == null -> textView.isVisible = false
            else -> {
                textView.apply {
                    isVisible = true
                    text = dateFormatter.formatTime(createdAt)
                }
            }
        }
    }
}
