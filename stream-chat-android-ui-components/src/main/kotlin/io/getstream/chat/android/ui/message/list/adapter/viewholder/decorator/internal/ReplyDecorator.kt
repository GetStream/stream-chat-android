package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.message.list.adapter.view.internal.MessageReplyView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.TextAndAttachmentsViewHolder

internal class ReplyDecorator(private val getCurrentUser: () -> User?) : BaseDecorator() {
    override fun decorateTextAndAttachmentsMessage(
        viewHolder: TextAndAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupReplyView(viewHolder.binding.replyView, data)

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupReplyView(viewHolder.binding.replyView, data)

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    private fun setupReplyView(replyView: MessageReplyView, data: MessageListItem.MessageItem) {
        val replyToMessage = data.message.replyTo
        if (replyToMessage != null) {
            replyView.isVisible = true
            replyView.setMessage(replyToMessage, if (data.isMine) false else getCurrentUser() == replyToMessage.user)
        } else {
            replyView.isVisible = false
        }
    }
}
