package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.common.extensions.internal.hasLink
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.LongClickFriendlyLinkMovementMethod
import io.getstream.chat.android.ui.databinding.StreamUiItemLinkAttachmentBinding
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator

/**
 * ViewHolder used for holding [io.getstream.chat.android.ui.message.list.adapter.view.internal.LinkAttachmentView]
 */
internal class LinkAttachmentsViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    listeners: MessageListListenerContainer?,
    internal val binding: StreamUiItemLinkAttachmentBinding = StreamUiItemLinkAttachmentBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false
    ),
    private val style: MessageListItemStyle,
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    init {
        binding.run {
            listeners?.let { container ->
                root.setOnClickListener {
                    container.messageClickListener.onMessageClick(data.message)
                }
                reactionsView.setReactionClickListener {
                    container.reactionViewClickListener.onReactionViewClick(data.message)
                }
                footnote.setOnThreadClickListener {
                    container.threadClickListener.onThreadClick(data.message)
                }
                root.setOnLongClickListener {
                    container.messageLongClickListener.onMessageLongClick(data.message)
                    true
                }
                avatarView.setOnClickListener {
                    container.userClickListener.onUserClick(data.message.user)
                }
                LongClickFriendlyLinkMovementMethod.set(
                    textView = messageText,
                    longClickTarget = root,
                    onLinkClicked = container.linkClickListener::onLinkClick
                )
            }
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        // TODO - check with the team regarding diffing

        val linkAttachment = data.message.attachments.firstOrNull { attachment -> attachment.hasLink() }

        updateHorizontalBias(data)

        linkAttachment?.let { attachment ->
            binding.linkAttachmentView.showLinkAttachment(attachment, style)
            binding.messageText.text = data.message.text
        }
    }

    private fun updateHorizontalBias(data: MessageListItem.MessageItem) {
        binding.messageContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
            this.horizontalBias = if (data.isMine) 1f else 0f
        }
    }
}
