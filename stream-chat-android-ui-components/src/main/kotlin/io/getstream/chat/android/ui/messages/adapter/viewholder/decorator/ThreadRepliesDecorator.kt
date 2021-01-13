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
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise
import io.getstream.chat.android.ui.utils.extensions.isInThread

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
    ) = Unit

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) = Unit

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) = Unit

    override fun decorateGiphyMessage(viewHolder: GiphyViewHolder, data: MessageListItem.MessageItem) = Unit

    private fun setupThreadRepliesView(
        threadRepliesFootNote: StreamUiMessageThreadsFootnoteBinding,
        messageFootnote: View,
        rootView: ConstraintLayout,
        footnoteAnchorViewId: Int,
        data: MessageListItem.MessageItem
    ) {
        val threadRepliesFootnoteId = threadRepliesFootNote.root.id
        val footnoteId = messageFootnote.id

        val replyCount = data.message.replyCount
        if (replyCount == 0 || data.message.isInThread()) {
            threadRepliesFootNote.root.isVisible = false
            revertFootnoteTranslation(threadRepliesFootNote.root, messageFootnote)
            revertDefaultFootnoteConstraints(rootView, footnoteId, threadRepliesFootnoteId, footnoteAnchorViewId, data)
            return
        }

        threadRepliesFootNote.root.isVisible = true
        threadRepliesFootNote.threadsOrnamentLeft.isVisible = data.isTheirs
        threadRepliesFootNote.threadsOrnamentRight.isVisible = !data.isTheirs

        rootView.updateConstraints {
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

            clearVerticalConstraints(footnoteId, threadRepliesFootnoteId)
            connect(threadRepliesFootnoteId, ConstraintSet.TOP, footnoteAnchorViewId, ConstraintSet.BOTTOM)
            connect(footnoteId, ConstraintSet.BOTTOM, threadRepliesFootnoteId, ConstraintSet.BOTTOM)
        }

        threadRepliesFootNote.root.translationY = -DEFAULT_FOOTNOTE_TOP_MARGIN
        messageFootnote.translationY = -DEFAULT_FOOTNOTE_TOP_MARGIN

        threadRepliesFootNote.threadRepliesButton.text =
            getRepliesQuantityString(rootView.resources, replyCount)
    }

    private fun revertDefaultFootnoteConstraints(
        rootView: ConstraintLayout,
        footnoteId: Int,
        threadRepliesFootnoteId: Int,
        footnoteAnchorViewId: Int,
        data: MessageListItem.MessageItem
    ) {
        rootView.updateConstraints {
            clearVerticalConstraints(footnoteId, threadRepliesFootnoteId)
            connect(footnoteId, ConstraintSet.TOP, footnoteAnchorViewId, ConstraintSet.BOTTOM)
            clearHorizontalConstraints(footnoteId, threadRepliesFootnoteId)
            if (data.isTheirs) {
                connect(footnoteId, ConstraintSet.LEFT, footnoteAnchorViewId, ConstraintSet.LEFT)
                connect(threadRepliesFootnoteId, ConstraintSet.LEFT, footnoteId, ConstraintSet.RIGHT)
            } else {
                connect(
                    footnoteId,
                    ConstraintSet.RIGHT,
                    footnoteAnchorViewId,
                    ConstraintSet.RIGHT
                )
                connect(threadRepliesFootnoteId, ConstraintSet.RIGHT, footnoteId, ConstraintSet.LEFT)
            }
        }
    }

    private fun revertFootnoteTranslation(threadRepliesFootNote: View, messageFootnote: View) {
        threadRepliesFootNote.translationY = 0.dpToPxPrecise()
        messageFootnote.translationY = 0.dpToPxPrecise()
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

    companion object {
        private val DEFAULT_FOOTNOTE_TOP_MARGIN = 6.dpToPxPrecise()
    }
}
