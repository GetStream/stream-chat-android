package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageGiphyBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageMediaAttachmentsBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePlainTextBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePlainTextWithMediaAttachmentsBinding
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
    ) = Unit

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) = Unit

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupThreadRepliesView(viewHolder.binding, data)
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupThreadRepliesView(viewHolder.binding, data)
    }

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        setupThreadRepliesView(viewHolder.binding, data)
    }

    override fun decorateGiphyMessage(viewHolder: GiphyViewHolder, data: MessageListItem.MessageItem) {
        setupThreadRepliesView(viewHolder.binding, data)
    }

    private fun setupThreadRepliesView(
        binding: StreamUiItemMessageMediaAttachmentsBinding,
        data: MessageListItem.MessageItem
    ) {
        val replyCount = data.message.replyCount
        if (replyCount == 0) {
            binding.threadRepliesFootnote.root.isVisible = false
            return
        }

        if (data.isTheirs) {
            binding.threadRepliesFootnote.threadsOrnamentLeft.isVisible = true
            binding.threadRepliesFootnote.threadsOrnamentRight.isVisible = false
        } else {
            binding.threadRepliesFootnote.threadsOrnamentLeft.isVisible = false
            binding.threadRepliesFootnote.threadsOrnamentRight.isVisible = true
        }

        binding.root.updateConstraints {
            val threadRepliesFootnoteId = binding.threadRepliesFootnote.root.id
            val footnoteId = binding.footnote.root.id

            clear(threadRepliesFootnoteId, ConstraintSet.START)
            clear(threadRepliesFootnoteId, ConstraintSet.LEFT)
            clear(threadRepliesFootnoteId, ConstraintSet.END)
            clear(threadRepliesFootnoteId, ConstraintSet.RIGHT)
            clear(footnoteId, ConstraintSet.END)
            clear(footnoteId, ConstraintSet.LEFT)
            clear(footnoteId, ConstraintSet.START)
            clear(footnoteId, ConstraintSet.RIGHT)

            if (data.isTheirs) {
                connect(threadRepliesFootnoteId, ConstraintSet.LEFT, binding.mediaAttachmentsGroupView.id, ConstraintSet.LEFT)
                connect(footnoteId, ConstraintSet.LEFT, threadRepliesFootnoteId, ConstraintSet.RIGHT)
            } else {
                connect(threadRepliesFootnoteId, ConstraintSet.RIGHT, binding.mediaAttachmentsGroupView.id, ConstraintSet.RIGHT)
                connect(footnoteId, ConstraintSet.RIGHT, threadRepliesFootnoteId, ConstraintSet.LEFT)
            }
        }

        binding.root.isVisible = true
        binding.threadRepliesFootnote.threadRepliesButton.text =
            binding.threadRepliesFootnote.threadRepliesButton.resources.getQuantityString(
                R.plurals.stream_ui_thread_messages_indicator,
                replyCount,
                replyCount
            )
    }

    private fun setupThreadRepliesView(binding: StreamUiItemMessageGiphyBinding, data: MessageListItem.MessageItem) {
        val replyCount = data.message.replyCount
        if (replyCount == 0) {
            binding.threadRepliesFootnote.root.isVisible = false
            return
        }

        if (data.isTheirs) {
            binding.threadRepliesFootnote.threadsOrnamentLeft.isVisible = true
            binding.threadRepliesFootnote.threadsOrnamentRight.isVisible = false
        } else {
            binding.threadRepliesFootnote.threadsOrnamentLeft.isVisible = false
            binding.threadRepliesFootnote.threadsOrnamentRight.isVisible = true
        }

        binding.root.updateConstraints {
            val threadRepliesFootnoteId = binding.threadRepliesFootnote.root.id
            val footnoteId = binding.footnote.root.id

            clear(threadRepliesFootnoteId, ConstraintSet.START)
            clear(threadRepliesFootnoteId, ConstraintSet.LEFT)
            clear(threadRepliesFootnoteId, ConstraintSet.END)
            clear(threadRepliesFootnoteId, ConstraintSet.RIGHT)
            clear(footnoteId, ConstraintSet.END)
            clear(footnoteId, ConstraintSet.LEFT)
            clear(footnoteId, ConstraintSet.START)
            clear(footnoteId, ConstraintSet.RIGHT)

            if (data.isTheirs) {
                connect(threadRepliesFootnoteId, ConstraintSet.LEFT, binding.cardView.id, ConstraintSet.LEFT)
                connect(footnoteId, ConstraintSet.LEFT, threadRepliesFootnoteId, ConstraintSet.RIGHT)
            } else {
                connect(threadRepliesFootnoteId, ConstraintSet.RIGHT, binding.cardView.id, ConstraintSet.RIGHT)
                connect(footnoteId, ConstraintSet.RIGHT, threadRepliesFootnoteId, ConstraintSet.LEFT)
            }
        }

        binding.root.isVisible = true
        binding.threadRepliesFootnote.threadRepliesButton.text =
            binding.threadRepliesFootnote.threadRepliesButton.resources.getQuantityString(
                R.plurals.stream_ui_thread_messages_indicator,
                replyCount,
                replyCount
            )
    }

    private fun setupThreadRepliesView(
        binding: StreamUiItemMessagePlainTextBinding,
        data: MessageListItem.MessageItem
    ) {
        val replyCount = data.message.replyCount
        if (replyCount == 0) {
            binding.threadRepliesFootnote.root.isVisible = false
            return
        }

        binding.threadRepliesFootnote.root.isVisible = true

        binding.threadRepliesFootnote.threadsOrnamentLeft.isVisible = data.isTheirs
        binding.threadRepliesFootnote.threadsOrnamentRight.isVisible = !data.isTheirs

        binding.root.updateConstraints {
            val threadRepliesFootnoteId = binding.threadRepliesFootnote.root.id
            val footnoteId = binding.footnote.root.id

            clear(threadRepliesFootnoteId, ConstraintSet.START)
            clear(threadRepliesFootnoteId, ConstraintSet.LEFT)
            clear(threadRepliesFootnoteId, ConstraintSet.END)
            clear(threadRepliesFootnoteId, ConstraintSet.RIGHT)
            clear(footnoteId, ConstraintSet.END)
            clear(footnoteId, ConstraintSet.LEFT)
            clear(footnoteId, ConstraintSet.START)
            clear(footnoteId, ConstraintSet.RIGHT)

            if (data.isTheirs) {
                connect(threadRepliesFootnoteId, ConstraintSet.LEFT, binding.messageContainer.id, ConstraintSet.LEFT)
                connect(footnoteId, ConstraintSet.LEFT, threadRepliesFootnoteId, ConstraintSet.RIGHT)
            } else {
                connect(threadRepliesFootnoteId, ConstraintSet.RIGHT, binding.messageContainer.id, ConstraintSet.RIGHT)
                connect(footnoteId, ConstraintSet.RIGHT, threadRepliesFootnoteId, ConstraintSet.LEFT)
            }
        }

        binding.threadRepliesFootnote.threadRepliesButton.text =
            binding.threadRepliesFootnote.threadRepliesButton.resources.getQuantityString(
                R.plurals.stream_ui_thread_messages_indicator,
                replyCount,
                replyCount
            )
    }


    private fun setupThreadRepliesView(
        binding: StreamUiItemMessagePlainTextWithMediaAttachmentsBinding,
        data: MessageListItem.MessageItem
    ) {
        val replyCount = data.message.replyCount
        if (replyCount == 0) {
            binding.threadRepliesFootnote.root.isVisible = false
            return
        }

        if (data.isTheirs) {
            binding.threadRepliesFootnote.threadsOrnamentLeft.isVisible = true
            binding.threadRepliesFootnote.threadsOrnamentRight.isVisible = false
        } else {
            binding.threadRepliesFootnote.threadsOrnamentLeft.isVisible = false
            binding.threadRepliesFootnote.threadsOrnamentRight.isVisible = true
        }

        binding.root.updateConstraints {
            val threadRepliesFootnoteId = binding.threadRepliesFootnote.root.id
            val footnoteId = binding.footnote.root.id

            clear(threadRepliesFootnoteId, ConstraintSet.START)
            clear(threadRepliesFootnoteId, ConstraintSet.LEFT)
            clear(threadRepliesFootnoteId, ConstraintSet.END)
            clear(threadRepliesFootnoteId, ConstraintSet.RIGHT)
            clear(footnoteId, ConstraintSet.END)
            clear(footnoteId, ConstraintSet.LEFT)
            clear(footnoteId, ConstraintSet.START)
            clear(footnoteId, ConstraintSet.RIGHT)

            if (data.isTheirs) {
                connect(threadRepliesFootnoteId, ConstraintSet.LEFT, binding.messageContainer.id, ConstraintSet.LEFT)
                connect(footnoteId, ConstraintSet.LEFT, threadRepliesFootnoteId, ConstraintSet.RIGHT)
            } else {
                connect(threadRepliesFootnoteId, ConstraintSet.RIGHT, binding.messageContainer.id, ConstraintSet.RIGHT)
                connect(footnoteId, ConstraintSet.RIGHT, threadRepliesFootnoteId, ConstraintSet.LEFT)
            }
        }

        binding.root.isVisible = true
        binding.threadRepliesFootnote.threadRepliesButton.text =
            binding.threadRepliesFootnote.threadRepliesButton.resources.getQuantityString(
                R.plurals.stream_ui_thread_messages_indicator,
                replyCount,
                replyCount
            )
    }
}
