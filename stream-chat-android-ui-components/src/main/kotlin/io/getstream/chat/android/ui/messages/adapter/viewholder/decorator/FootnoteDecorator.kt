package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.extensions.isBottomPosition
import com.getstream.sdk.chat.utils.extensions.isNotBottomPosition
import com.getstream.sdk.chat.utils.formatTime
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiMessageThreadsFootnoteBinding
import io.getstream.chat.android.ui.messages.adapter.view.FootnoteView
import io.getstream.chat.android.ui.messages.adapter.viewholder.GiphyViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise
import io.getstream.chat.android.ui.utils.extensions.getCreatedAtOrNull
import io.getstream.chat.android.ui.utils.extensions.getUpdatedAtOrNull
import io.getstream.chat.android.ui.utils.extensions.isEphemeral
import io.getstream.chat.android.ui.utils.extensions.isGiphyNotEphemeral
import io.getstream.chat.android.ui.utils.extensions.isInThread
import io.getstream.chat.android.ui.utils.extensions.leftDrawable
import java.util.Date

internal class FootnoteDecorator(
    private val dateFormatter: DateFormatter,
    private val isDirectMessage: Boolean,
) : BaseDecorator() {

    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupFootnote(viewHolder.binding.footnote, data)

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        setupFootnote(viewHolder.binding.footnote, data)
    }

    override fun decorateGiphyMessage(viewHolder: GiphyViewHolder, data: MessageListItem.MessageItem) = Unit

    private fun setupFootnote(footnoteView: FootnoteView, data: MessageListItem.MessageItem) {
        with(footnoteView) {
            setupEphemeralMessageFooterLabel(footnote.messageFooter, data)
            setupMessageFooterTime(footnote.timeView, data)
            setupThreadRepliesView(threadsFootnote, footnote.root, data)
            applyGravity(data.isMine)
        }
    }

    private fun setupEphemeralMessageFooterLabel(textView: TextView, data: MessageListItem.MessageItem) {
        when {
            data.isBottomPosition() && !isDirectMessage && data.isTheirs -> {
                textView.text = data.message.user.name
                textView.isVisible = true
            }
            data.isNotBottomPosition() || !data.message.isEphemeral() -> {
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
        fun TextView.showTime(time: Date) {
            isVisible = true
            text = dateFormatter.formatTime(time)
        }

        val createdAt = data.message.getCreatedAtOrNull()
        val updatedAt = data.message.getUpdatedAtOrNull()

        when {
            data.isNotBottomPosition() || createdAt == null -> textView.isVisible = false
            data.message.isGiphyNotEphemeral() && updatedAt != null -> textView.showTime(updatedAt)
            else -> textView.showTime(createdAt)
        }
    }

    private fun setupThreadRepliesView(
        threadRepliesFootNote: StreamUiMessageThreadsFootnoteBinding,
        messageFootnote: View,
        data: MessageListItem.MessageItem
    ) {
        val replyCount = data.message.replyCount
        if (replyCount == 0 || data.message.isInThread()) {
            threadRepliesFootNote.root.isVisible = false
            revertFootnoteTranslation(threadRepliesFootNote.root, messageFootnote)
            messageFootnote.isVisible = true
            return
        }

        messageFootnote.isVisible = false
        threadRepliesFootNote.root.isVisible = true
        threadRepliesFootNote.threadsOrnamentLeft.isVisible = data.isTheirs
        threadRepliesFootNote.threadsOrnamentRight.isVisible = !data.isTheirs

        threadRepliesFootNote.root.translationY = -DEFAULT_FOOTNOTE_TOP_MARGIN
        messageFootnote.translationY = -DEFAULT_FOOTNOTE_TOP_MARGIN

        threadRepliesFootNote.threadRepliesButton.text =
            getRepliesQuantityString(messageFootnote.resources, replyCount)
    }

    private fun revertFootnoteTranslation(threadRepliesFootNote: View, messageFootnote: View) {
        threadRepliesFootNote.translationY = 0.dpToPxPrecise()
        messageFootnote.translationY = 0.dpToPxPrecise()
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
