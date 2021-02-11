package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.extensions.isBottomPosition
import com.getstream.sdk.chat.utils.extensions.isNotBottomPosition
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import com.getstream.sdk.chat.utils.formatTime
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.getCreatedAtOrNull
import io.getstream.chat.android.ui.common.extensions.getUpdatedAtOrNull
import io.getstream.chat.android.ui.common.extensions.internal.leftDrawable
import io.getstream.chat.android.ui.common.extensions.isDeleted
import io.getstream.chat.android.ui.common.extensions.isEphemeral
import io.getstream.chat.android.ui.common.extensions.isGiphyNotEphemeral
import io.getstream.chat.android.ui.message.list.adapter.view.internal.FootnoteView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.PlainTextWithMediaAttachmentsViewHolder

internal class FootnoteDecorator(
    private val dateFormatter: DateFormatter,
    private val isDirectMessage: Boolean,
) : BaseDecorator() {

    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupFootnote(
        viewHolder.binding.footnote,
        viewHolder.binding.root,
        viewHolder.binding.threadGuideline,
        viewHolder.binding.messageContainer,
        data,
    )

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupFootnote(
        viewHolder.binding.footnote,
        viewHolder.binding.root,
        viewHolder.binding.threadGuideline,
        viewHolder.binding.fileAttachmentsView,
        data,
    )

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupFootnote(
        viewHolder.binding.footnote,
        viewHolder.binding.root,
        viewHolder.binding.threadGuideline,
        viewHolder.binding.messageContainer,
        data,
    )

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupFootnote(
        viewHolder.binding.footnote,
        viewHolder.binding.root,
        viewHolder.binding.threadGuideline,
        viewHolder.binding.mediaAttachmentsGroupView,
        data,
    )

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupFootnote(
        viewHolder.binding.footnote,
        viewHolder.binding.root,
        viewHolder.binding.threadGuideline,
        viewHolder.binding.messageContainer,
        data,
    )

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupSimpleFootnoteWithRootConstraints(
            viewHolder.binding.footnote,
            viewHolder.binding.root,
            viewHolder.binding.cardView,
            data
        )
        with(viewHolder.binding.footnote) {
            applyGravity(data.isMine)
            hideStatusIndicator()
        }
    }

    override fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupSimpleFootnote(viewHolder.binding.footnote, data)
    }

    private fun setupFootnote(
        footnoteView: FootnoteView,
        root: ConstraintLayout,
        threadGuideline: View,
        anchorView: View,
        data: MessageListItem.MessageItem,
    ) {
        val isSimpleFootnoteMode = data.message.replyCount == 0 || data.isThreadMode
        if (isSimpleFootnoteMode) {
            setupSimpleFootnoteWithRootConstraints(footnoteView, root, anchorView, data)
        } else {
            setupThreadFootnote(footnoteView, root, threadGuideline, data)
        }
        footnoteView.applyGravity(data.isMine)
    }

    private fun setupSimpleFootnoteWithRootConstraints(
        footnoteView: FootnoteView,
        root: ConstraintLayout,
        anchorView: View,
        data: MessageListItem.MessageItem,
    ) {
        root.updateConstraints {
            clear(footnoteView.id, ConstraintSet.TOP)
            connect(footnoteView.id, ConstraintSet.TOP, anchorView.id, ConstraintSet.BOTTOM)
        }
        setupSimpleFootnote(footnoteView, data)
    }

    private fun setupSimpleFootnote(footnoteView: FootnoteView, data: MessageListItem.MessageItem) {
        footnoteView.showSimpleFootnote()
        setupMessageFooterLabel(footnoteView.footerTextLabel, data)
        setupMessageFooterTime(footnoteView, data)
        setupDeliveryStateIndicator(footnoteView, data)
    }

    private fun setupThreadFootnote(
        footnoteView: FootnoteView,
        root: ConstraintLayout,
        threadGuideline: View,
        data: MessageListItem.MessageItem,
    ) {
        root.updateConstraints {
            clear(footnoteView.id, ConstraintSet.TOP)
            connect(footnoteView.id, ConstraintSet.TOP, threadGuideline.id, ConstraintSet.BOTTOM)
        }
        footnoteView.showThreadRepliesFootnote(data.isMine, data.message.replyCount, data.message.threadParticipants)
    }

    private fun setupMessageFooterLabel(textView: TextView, data: MessageListItem.MessageItem) {
        when {
            data.isBottomPosition() && !isDirectMessage && data.isTheirs -> {
                textView.text = data.message.user.name
                textView.isVisible = true
            }
            data.isNotBottomPosition() -> textView.isVisible = false
            !data.message.isEphemeral() && !data.message.isDeleted() -> textView.isVisible = false
            else -> textView.apply {
                isVisible = true
                text = context.getString(R.string.stream_ui_ephemeral_msg_footer)
                leftDrawable(R.drawable.stream_ui_ic_icon_eye_off)
                compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.stream_ui_spacing_small)
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
            data.message.isDeleted() -> footnoteView.hideStatusIndicator()
            else -> when (status) {
                SyncStatus.FAILED_PERMANENTLY -> footnoteView.hideStatusIndicator()
                SyncStatus.IN_PROGRESS, SyncStatus.SYNC_NEEDED -> footnoteView.showInProgressStatusIndicator()
                SyncStatus.COMPLETED -> {
                    if (data.isMessageRead) footnoteView.showReadStatusIndicator()
                    else footnoteView.showSentStatusIndicator()
                }
            }.exhaustive
        }
    }
}
