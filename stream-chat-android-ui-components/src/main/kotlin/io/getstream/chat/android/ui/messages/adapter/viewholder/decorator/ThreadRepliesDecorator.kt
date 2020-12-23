package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiMessageThreadsFootnoteBinding
import io.getstream.chat.android.ui.messages.adapter.viewholder.GiphyViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder

internal class ThreadRepliesDecorator : BaseDecorator() {
    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        // setupThreadRepliesView(viewHolder.binding.threadRepliesFootnote)
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        // setupThreadRepliesView(viewHolder.binding.threadRepliesFootnote)
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        // setupThreadRepliesView(viewHolder.binding.threadRepliesFootnote)
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        // setupThreadRepliesView(viewHolder.binding.threadRepliesFootnote)
    }

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        setupThreadRepliesView(viewHolder.binding.threadRepliesFootnote, data)
    }

    override fun decorateGiphyMessage(viewHolder: GiphyViewHolder, data: MessageListItem.MessageItem) {
        // setupThreadRepliesView(viewHolder.binding.threadRepliesFootnote)
    }

    private fun setupThreadRepliesView(
        binding: StreamUiMessageThreadsFootnoteBinding,
        data: MessageListItem.MessageItem
    ) {
        val replyCount = data.message.replyCount
        if (replyCount == 0) {
            binding.container.isVisible = false
            return
        }

        if (data.isTheirs) {
            binding.threadsOrnamentLeft.isVisible = true
            binding.threadsOrnamentRight.isVisible = false
        } else if (data.isMine) {
            binding.threadsOrnamentLeft.isVisible = false
            binding.threadsOrnamentRight.isVisible = true
        }

        binding.container.isVisible = true
        binding.threadRepliesButton.text = binding.threadRepliesButton.resources.getQuantityString(
            R.plurals.stream_ui_thread_messages_indicator,
            replyCount,
            replyCount
        )
    }
}
