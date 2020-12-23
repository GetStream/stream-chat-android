package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.constrainViewStartToStartOfView
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePlainTextBinding
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
        setupThreadRepliesView(viewHolder.binding, data)
    }

    override fun decorateGiphyMessage(viewHolder: GiphyViewHolder, data: MessageListItem.MessageItem) {
        // setupThreadRepliesView(viewHolder.binding.threadRepliesFootnote)
    }

    private fun setupThreadRepliesView(
        binding: StreamUiItemMessagePlainTextBinding,
        data: MessageListItem.MessageItem
    ) {
        val replyCount = data.message.replyCount
        if (replyCount == 0) {
            // binding.container.isVisible = false
            // return
        }

        if (data.isTheirs) {
            binding.threadRepliesFootnote.threadsOrnamentLeft.isVisible = true
            binding.threadRepliesFootnote.threadsOrnamentRight.isVisible = false
        } else if (data.isMine) {
            binding.threadRepliesFootnote.threadsOrnamentLeft.isVisible = false
            binding.threadRepliesFootnote.threadsOrnamentRight.isVisible = true
        }

        binding.root.updateConstraints {
            applyGravity(binding.threadRepliesFootnote.root, binding.messageText, binding.messageText, data)
        }

        binding.root.isVisible = true
        binding.threadRepliesFootnote.threadRepliesButton.text =
            binding.threadRepliesFootnote.threadRepliesButton.resources.getQuantityString(
                R.plurals.stream_ui_thread_messages_indicator,
                replyCount,
                replyCount
            )
    }

    private fun ConstraintSet.applyGravity(
        targetView: View,
        startView: View,
        endView: View,
        data: MessageListItem.MessageItem
    ) {
        clear(targetView.id, ConstraintSet.START)
        clear(targetView.id, ConstraintSet.END)
        if (data.isTheirs) constrainViewStartToStartOfView(targetView, startView)
    }
}
