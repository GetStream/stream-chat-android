package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.widget.TextView
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.formatTime
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.utils.extensions.getCreatedAtOrNull
import io.getstream.chat.android.ui.utils.extensions.isEphemeral
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.leftDrawable

internal class TimeDecorator(private val dateFormatter: DateFormatter) : BaseDecorator() {

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        setupMessageFooter(viewHolder.binding.messageFooter, data)
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupMessageFooter(viewHolder.binding.messageFooter, data)
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupMessageFooter(viewHolder.binding.messageFooter, data)
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupMessageFooter(viewHolder.binding.messageFooter, data)
    }

    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupMessageFooter(viewHolder.binding.messageFooter, data)
    }

    override fun decorateDeletedMessage(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) = Unit

    private fun setupMessageFooter(textView: TextView, data: MessageListItem.MessageItem) {
        val createdAt = data.message.getCreatedAtOrNull()
        when {
            data.positions.contains(MessageListItem.Position.BOTTOM).not() || createdAt == null -> textView.isVisible = false
            else -> {
                textView.apply {
                    isVisible = true
                    val createdAtTime = dateFormatter.formatTime(createdAt)
                    if (data.message.isEphemeral()) {
                        val footerTemplate = context.getString(R.string.stream_ui_ephemeral_msg_footer_template)
                        text = String.format(footerTemplate, createdAtTime)
                        leftDrawable(R.drawable.stream_ui_ic_icon_eye_off)
                        compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.stream_ui_spacing_small)
                    } else {
                        text = createdAtTime
                        leftDrawable(0)
                    }
                }
            }
        }
    }
}
