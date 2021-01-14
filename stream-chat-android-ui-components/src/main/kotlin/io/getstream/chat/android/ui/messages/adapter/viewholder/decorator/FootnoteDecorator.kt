package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.widget.TextView
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.extensions.isBottomPosition
import com.getstream.sdk.chat.utils.extensions.isNotBottomPosition
import com.getstream.sdk.chat.utils.formatTime
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.messages.adapter.view.FootnoteView
import io.getstream.chat.android.ui.messages.adapter.viewholder.GiphyViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.utils.extensions.getCreatedAtOrNull
import io.getstream.chat.android.ui.utils.extensions.getUpdatedAtOrNull
import io.getstream.chat.android.ui.utils.extensions.isEphemeral
import io.getstream.chat.android.ui.utils.extensions.isGiphyNotEphemeral
import io.getstream.chat.android.ui.utils.extensions.isInThread
import io.getstream.chat.android.ui.utils.extensions.leftDrawable

internal class FootnoteDecorator(
    private val dateFormatter: DateFormatter,
    private val isDirectMessage: Boolean,
) : BaseDecorator() {

    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupFootnote(viewHolder.binding.footnote, data)

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupFootnote(viewHolder.binding.footnote, data)

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupFootnote(viewHolder.binding.footnote, data)

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupFootnote(viewHolder.binding.footnote, data)

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        setupFootnote(viewHolder.binding.footnote, data)
    }

    override fun decorateGiphyMessage(viewHolder: GiphyViewHolder, data: MessageListItem.MessageItem) {
        setupFootnote(viewHolder.binding.footnote, data)
        viewHolder.binding.footnote.hideStatusIndicator()
    }

    private fun setupFootnote(footnoteView: FootnoteView, data: MessageListItem.MessageItem) {
        val isSimpleFootnoteMode = data.message.replyCount == 0 || data.message.isInThread()
        if (isSimpleFootnoteMode) {
            setupSimpleFootnote(footnoteView, data)
        } else {
            footnoteView.showThreadRepliesFootnote(data.isMine, data.message.replyCount)
        }
        footnoteView.applyGravity(data.isMine)
    }

    private fun setupSimpleFootnote(footnoteView: FootnoteView, data: MessageListItem.MessageItem) {
        footnoteView.showSimpleFootnote()
        setupMessageFooterLabel(footnoteView.footerTextLabel, data)
        setupMessageFooterTime(footnoteView, data)
        setupDeliveryStateIndicator(footnoteView, data)
    }

    private fun setupMessageFooterLabel(textView: TextView, data: MessageListItem.MessageItem) {
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

    private fun setupMessageFooterTime(footnoteView: FootnoteView, data: MessageListItem.MessageItem) {
        val createdAt = data.message.getCreatedAtOrNull()
        val updatedAt = data.message.getUpdatedAtOrNull()

        when {
            data.isNotBottomPosition() || createdAt == null -> footnoteView.hideTimeLabel()
            data.message.isGiphyNotEphemeral() && updatedAt != null -> footnoteView.showTime(
                dateFormatter.formatTime(updatedAt)
            )
            else -> footnoteView.showTime(dateFormatter.formatTime(createdAt))
        }
    }

    private fun setupDeliveryStateIndicator(footnoteView: FootnoteView, data: MessageListItem.MessageItem) {
        val status = data.message.syncStatus
        when {
            data.isNotBottomPosition() -> footnoteView.hideStatusIndicator()
            data.isTheirs -> footnoteView.hideStatusIndicator()
            data.message.isEphemeral() -> footnoteView.hideStatusIndicator()
            else -> when (status) {
                SyncStatus.FAILED_PERMANENTLY -> footnoteView.hideStatusIndicator()
                SyncStatus.IN_PROGRESS, SyncStatus.SYNC_NEEDED -> footnoteView.showInProgressStatusIndicator()
                SyncStatus.COMPLETED -> {
                    if (data.messageReadBy.isNotEmpty()) footnoteView.showReadStatusIndicator()
                    else footnoteView.showSentStatusIndicator()
                }
            }.exhaustive
        }
    }
}
