package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.isBottomPosition
import io.getstream.chat.android.ui.ChatUI.markdown
import io.getstream.chat.android.ui.common.extensions.hasText
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.extensions.isReply
import io.getstream.chat.android.ui.databinding.StreamUiItemGiphyAttachmentBinding
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.view.internal.MediaAttachmentView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.BackgroundDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator

internal class GiphyAttachmentViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    internal val binding: StreamUiItemGiphyAttachmentBinding = StreamUiItemGiphyAttachmentBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false
    ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        binding.messageText.isVisible = data.message.text.isNotEmpty()
        markdown.setText(binding.messageText, data.message.text)

        binding.messageContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
            horizontalBias = if (data.isTheirs) 0f else 1f
        }

        imageCorners(binding.mediaAttachmentView, data)

        binding.mediaAttachmentView.showGiphy(
            data.message.attachments.first(),
            containerView = binding.messageContainer
        )
    }

    private fun imageCorners(mediaAttachmentView: MediaAttachmentView, data: MessageListItem.MessageItem) {
        val topLeftCorner = if (data.message.isReply()) 0f else BackgroundDecorator.DEFAULT_CORNER_RADIUS
        val topRightCorner = if (data.message.isReply()) 0f else BackgroundDecorator.DEFAULT_CORNER_RADIUS
        val bottomRightCorner = if (data.message.hasText() || (data.isMine && data.isBottomPosition())) {
                0f
            }
            else {
                BackgroundDecorator.DEFAULT_CORNER_RADIUS
            }
        val bottomLeftCorner = if (data.message.hasText() || (data.isTheirs && data.isBottomPosition())) {
                0f
            } else {
                BackgroundDecorator.DEFAULT_CORNER_RADIUS
            }

        mediaAttachmentView.setImageShapeByCorners(topLeftCorner, topRightCorner, bottomRightCorner, bottomLeftCorner)
    }
}
