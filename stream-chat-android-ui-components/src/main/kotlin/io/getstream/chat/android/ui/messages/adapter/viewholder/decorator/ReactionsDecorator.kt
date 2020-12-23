package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import io.getstream.chat.android.ui.messages.adapter.viewholder.GiphyViewHolder
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
            setupReactionsView(root, reactionsView, messageText, reactionsOffsetSpace, data)
        }
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        with(viewHolder.binding) {
            setupReactionsView(root, reactionsView, mediaAttachmentsGroupView, reactionsOffsetSpace, data)
        }
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        with(viewHolder.binding) {
            setupReactionsView(root, reactionsView, mediaAttachmentsGroupView, reactionsOffsetSpace, data)
        }
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        with(viewHolder.binding) {
            setupReactionsView(root, reactionsView, fileAttachmentsView, reactionsOffsetSpace, data)
        }
    }

    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        with(viewHolder.binding) {
            setupReactionsView(root, reactionsView, fileAttachmentsView, reactionsOffsetSpace, data)
        }
    }

    override fun decorateDeletedMessage(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) = Unit

    override fun decorateGiphyMessage(viewHolder: GiphyViewHolder, data: MessageListItem.MessageItem) = Unit

    private fun setupReactionsView(
        rootConstraintLayout: ConstraintLayout,
        reactionsView: ViewReactionsView,
        achorView: View,
        reactionsOffsetSpace: View,
        data: MessageListItem.MessageItem
    ) {
        if (data.message.latestReactions.isNotEmpty()) {
            reactionsView.isVisible = true
            reactionsView.setMessage(data.message, data.isMine)

            rootConstraintLayout.updateConstraints {
                clear(reactionsView.id, ConstraintSet.START)
                clear(reactionsView.id, ConstraintSet.END)
                clear(reactionsOffsetSpace.id, ConstraintSet.START)
                clear(reactionsOffsetSpace.id, ConstraintSet.END)
            }
            reactionsOffsetSpace.updateLayoutParams<ConstraintLayout.LayoutParams> {
                val offset = if (data.message.isSingleReaction()) {
                    SINGLE_REACTION_OFFSET
                } else {
                    MULTIPLE_REACTIONS_OFFSET
                }
                if (data.isTheirs) {
                    endToEnd = achorView.id
                    marginEnd = offset
                } else {
                    startToStart = achorView.id
                    marginStart = offset
                }
            }
            reactionsView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                if (data.isTheirs) {
                    startToEnd = reactionsOffsetSpace.id
                } else {
                    endToStart = reactionsOffsetSpace.id
                }
            }
            achorView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = REACTIONS_SPACING
            }
        } else {
            reactionsView.isVisible = false
            achorView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = 0
            }
        }
    }

    private companion object {
        private val REACTIONS_SPACING = 16.dpToPx()
        private val SINGLE_REACTION_OFFSET = 6.dpToPx()
        private val MULTIPLE_REACTIONS_OFFSET = 24.dpToPx()
    }
}
