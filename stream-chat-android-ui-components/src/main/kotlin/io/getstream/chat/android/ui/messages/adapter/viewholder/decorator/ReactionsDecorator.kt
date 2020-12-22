package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.reactions.ViewReactionsView
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.isSingleReaction

internal class ReactionsDecorator : BaseDecorator() {

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem
    ) {
        with(viewHolder.binding) {
            setupReactionsView(root, reactionsView, messageText, data)
        }
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        with(viewHolder.binding) {
            setupReactionsView(root, reactionsView, mediaAttachmentsGroupView, data)
        }
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        with(viewHolder.binding) {
            setupReactionsView(root, reactionsView, mediaAttachmentsGroupView, data)
        }
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        with(viewHolder.binding) {
            setupReactionsView(root, reactionsView, fileAttachmentsView, data)
        }
    }

    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        with(viewHolder.binding) {
            setupReactionsView(root, reactionsView, fileAttachmentsView, data)
        }
    }

    override fun decorateDeletedMessage(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) = Unit

    private fun setupReactionsView(
        rootConstraintLayout: ConstraintLayout,
        reactionsView: ViewReactionsView,
        anchor: View,
        data: MessageListItem.MessageItem
    ) {
        val hasReactions = data.message.latestReactions.isNotEmpty()
        reactionsView.isVisible = hasReactions

        if (!hasReactions) {
            anchor.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = 0
            }
            return
        }

        rootConstraintLayout.updateConstraints {
            clear(reactionsView.id, ConstraintSet.START)
            clear(reactionsView.id, ConstraintSet.END)
        }
        reactionsView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            val offset = if (data.message.isSingleReaction()) {
                SINGLE_REACTION_OFFSET
            } else {
                MULTIPLE_REACTIONS_OFFSET
            }
            if (data.isTheirs) {
                startToEnd = anchor.id
                marginStart = -offset
            } else {
                endToStart = anchor.id
                marginEnd = -offset
            }
        }
        anchor.updateLayoutParams<ConstraintLayout.LayoutParams> {
            topMargin = if (hasReactions) REACTIONS_SPACING else 0
        }
        reactionsView.setMessage(data.message, data.isMine)
    }

    private companion object {
        private val REACTIONS_SPACING = 16.dpToPx()
        private val SINGLE_REACTION_OFFSET = 6.dpToPx()
        private val MULTIPLE_REACTIONS_OFFSET = 24.dpToPx()
    }
}
