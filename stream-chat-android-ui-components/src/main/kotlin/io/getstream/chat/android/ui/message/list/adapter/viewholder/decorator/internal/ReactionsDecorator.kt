package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import android.graphics.Rect
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import io.getstream.chat.android.ui.common.extensions.hasReactions
import io.getstream.chat.android.ui.common.extensions.hasSingleReaction
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.ImageAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.TextAndAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.reactions.view.internal.ViewReactionsView

internal class ReactionsDecorator(private val style: MessageListItemStyle) : BaseDecorator() {

    override fun decorateTextAndAttachmentsMessage(
        viewHolder: TextAndAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = with(viewHolder.binding) {
        setupReactionsView(root, messageContainer, reactionsSpace, reactionsView, data)
    }

    /**
     * Decorates the reactions section of the Giphy attachment.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateGiphyAttachmentMessage(
        viewHolder: GiphyAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) = with(viewHolder.binding) {
        setupReactionsView(root, messageContainer, reactionsSpace, reactionsView, data)
    }

    /**
     * Decorates the reactions section of the image attachment message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateImageAttachmentMessage(
        viewHolder: ImageAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) = with(viewHolder.binding) {
        setupReactionsView(root, messageContainer, reactionsSpace, reactionsView, data)
    }

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        with(viewHolder.binding) {
            setupReactionsView(root, messageContainer, reactionsSpace, reactionsView, data)
        }
    }

    override fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    private fun setupReactionsView(
        rootConstraintLayout: ConstraintLayout,
        contentView: View,
        reactionsSpace: View,
        reactionsView: ViewReactionsView,
        data: MessageListItem.MessageItem,
    ) {
        if (data.message.hasReactions()) {
            reactionsView.isVisible = true
            reactionsSpace.isVisible = true

            reactionsView.applyStyle(style.reactionsViewStyle)

            reactionsView.setMessage(data.message, data.isMine) {
                rootConstraintLayout.updateConstraints {
                    clear(reactionsView.id, ConstraintSet.START)
                    clear(reactionsView.id, ConstraintSet.END)
                    clear(reactionsSpace.id, ConstraintSet.START)
                    clear(reactionsSpace.id, ConstraintSet.END)
                }

                reactionsSpace.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    if (data.isTheirs) {
                        endToEnd = contentView.id
                        marginEnd = 0
                    } else {
                        startToStart = contentView.id
                        marginStart = 0
                    }
                }

                reactionsView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    if (data.isTheirs) {
                        startToEnd = reactionsSpace.id
                    } else {
                        endToStart = reactionsSpace.id
                    }
                }

                reactionsSpace.doOnPreDraw {
                    val dynamicOffset = calculateDynamicOffset(
                        rootConstraintLayout,
                        reactionsSpace,
                        reactionsView,
                        data
                    )

                    updateOffset(contentView, reactionsSpace, data, dynamicOffset)
                }
            }
        } else {
            reactionsView.isVisible = false
            reactionsSpace.isVisible = false
        }
    }

    private fun calculateDynamicOffset(
        rootConstraintLayout: ConstraintLayout,
        reactionsSpace: View,
        reactionsView: ViewReactionsView,
        data: MessageListItem.MessageItem,
    ): Int {
        val offsetViewBounds = Rect()
        reactionsSpace.getDrawingRect(offsetViewBounds)
        rootConstraintLayout.offsetDescendantRectToMyCoords(reactionsSpace, offsetViewBounds)
        val relativeXToParent = offsetViewBounds.left
        val rootWidth =
            rootConstraintLayout.measuredWidth - (rootConstraintLayout.paddingStart + rootConstraintLayout.paddingEnd)

        val offsetFromParent =
            if (data.isTheirs) relativeXToParent else rootConstraintLayout.measuredWidth - relativeXToParent

        val expectedReactionsAndOffsetWidth = offsetFromParent + reactionsView.measuredWidth

        return when {
            expectedReactionsAndOffsetWidth > rootConstraintLayout.measuredWidth -> expectedReactionsAndOffsetWidth - rootWidth
            data.message.hasSingleReaction() -> SINGLE_REACTION_OFFSET
            else -> MULTIPLE_REACTIONS_OFFSET
        }
    }

    private fun updateOffset(
        contentView: View,
        reactionsSpace: View,
        data: MessageListItem.MessageItem,
        dynamicOffset: Int,
    ) {
        reactionsSpace.updateLayoutParams<ConstraintLayout.LayoutParams> {
            if (data.isTheirs) {
                endToEnd = contentView.id
                marginEnd = dynamicOffset
            } else {
                startToStart = contentView.id
                marginStart = dynamicOffset
            }
        }
    }

    private companion object {
        private val SINGLE_REACTION_OFFSET = 8.dpToPx()
        private val MULTIPLE_REACTIONS_OFFSET = 26.dpToPx()
    }
}
