package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.content.res.Resources
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiMessageThreadsFootnoteBinding
import io.getstream.chat.android.ui.messages.adapter.viewholder.GiphyViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.BackgroundDecorator.Companion.DEFAULT_CORNER_RADIUS
import io.getstream.chat.android.ui.utils.extensions.isInThread

internal class ThreadRepliesDecorator : BaseDecorator() {
    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupThreadRepliesView(
            viewHolder.binding.threadRepliesFootnote,
            viewHolder.binding.footnote.root,
            viewHolder.binding.root,
            viewHolder.binding.fileAttachmentsView.id,
            data
        )
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupThreadRepliesView(
            viewHolder.binding.threadRepliesFootnote,
            viewHolder.binding.footnote.root,
            viewHolder.binding.root,
            viewHolder.binding.fileAttachmentsView.id,
            data
        )
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupThreadRepliesView(
            viewHolder.binding.threadRepliesFootnote,
            viewHolder.binding.footnote.root,
            viewHolder.binding.root,
            viewHolder.binding.mediaAttachmentsGroupView.id,
            data
        )
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        setupThreadRepliesView(
            viewHolder.binding.threadRepliesFootnote,
            viewHolder.binding.footnote.root,
            viewHolder.binding.root,
            viewHolder.binding.mediaAttachmentsGroupView.id,
            data
        )
    }

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        setupThreadRepliesView(
            viewHolder.binding.threadRepliesFootnote,
            viewHolder.binding.footnote.root,
            viewHolder.binding.root,
            viewHolder.binding.messageContainer.id,
            data
        )
    }

    override fun decorateGiphyMessage(viewHolder: GiphyViewHolder, data: MessageListItem.MessageItem) {
        setupThreadRepliesView(
            viewHolder.binding.threadRepliesFootnote,
            viewHolder.binding.footnote.root,
            viewHolder.binding.root,
            viewHolder.binding.cardView.id,
            data
        )
    }

    private fun setupThreadRepliesView(
        threadRepliesFootNote: StreamUiMessageThreadsFootnoteBinding,
        messageFootnote: View,
        rootView: ConstraintLayout,
        footnoteAnchorViewId: Int,
        data: MessageListItem.MessageItem
    ) {
        val replyCount = data.message.replyCount
        if (replyCount == 0 || data.message.isInThread()) {
            threadRepliesFootNote.root.isVisible = false
            return
        }

        threadRepliesFootNote.root.isVisible = true
        threadRepliesFootNote.threadsOrnamentLeft.isVisible = data.isTheirs
        threadRepliesFootNote.threadsOrnamentRight.isVisible = !data.isTheirs

        rootView.updateConstraints {
            val threadRepliesFootnoteId = threadRepliesFootNote.root.id
            val footnoteId = messageFootnote.id

            clearHorizontalConstraints(threadRepliesFootnoteId, footnoteId)

            if (data.isTheirs) {
                connect(threadRepliesFootnoteId, ConstraintSet.LEFT, footnoteAnchorViewId, ConstraintSet.LEFT)
                connect(footnoteId, ConstraintSet.LEFT, threadRepliesFootnoteId, ConstraintSet.RIGHT)
            } else {
                connect(
                    threadRepliesFootnoteId,
                    ConstraintSet.RIGHT,
                    footnoteAnchorViewId,
                    ConstraintSet.RIGHT
                )
                connect(footnoteId, ConstraintSet.RIGHT, threadRepliesFootnoteId, ConstraintSet.LEFT)
            }

            clearVerticalConstraints(footnoteId)
            connect(footnoteId, ConstraintSet.BOTTOM, threadRepliesFootnoteId, ConstraintSet.BOTTOM)
        }

        threadRepliesFootNote.root.translationY = -DEFAULT_CORNER_RADIUS
        messageFootnote.translationY = -DEFAULT_CORNER_RADIUS

        threadRepliesFootNote.threadRepliesButton.text =
            getRepliesQuantityString(rootView.resources, replyCount)
    }

    private fun ConstraintSet.clearHorizontalConstraints(vararg viewIds: Int) {
        viewIds.forEach {
            clear(it, ConstraintSet.START)
            clear(it, ConstraintSet.LEFT)
            clear(it, ConstraintSet.END)
            clear(it, ConstraintSet.RIGHT)
        }
    }

    private fun ConstraintSet.clearVerticalConstraints(vararg viewIds: Int) {
        viewIds.forEach {
            clear(it, ConstraintSet.BOTTOM)
            clear(it, ConstraintSet.TOP)
        }
    }

    private fun getRepliesQuantityString(
        res: Resources,
        replyCount: Int
    ) = res.getQuantityString(
        R.plurals.stream_ui_thread_messages_indicator,
        replyCount,
        replyCount
    )
}
