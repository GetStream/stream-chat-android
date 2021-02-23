package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.ChatMarkdown
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.hasLink
import io.getstream.chat.android.ui.common.internal.LongClickFriendlyLinkMovementMethod
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePlainTextBinding
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.view.internal.LinkAttachmentView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator

internal class MessagePlainTextViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listeners: MessageListListenerContainer,
    private val markdown: ChatMarkdown,
    private val attachmentViewFactory: AttachmentViewFactory,
    internal val binding: StreamUiItemMessagePlainTextBinding =
        StreamUiItemMessagePlainTextBinding.inflate(
            parent.inflater,
            parent,
            false
        ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    init {
        binding.run {
            root.setOnClickListener {
                listeners.messageClickListener.onMessageClick(data.message)
            }
            reactionsView.setReactionClickListener {
                listeners.reactionViewClickListener.onReactionViewClick(data.message)
            }
            footnote.setOnThreadClickListener {
                listeners.threadClickListener.onThreadClick(data.message)
            }
            root.setOnLongClickListener {
                listeners.messageLongClickListener.onMessageLongClick(data.message)
                true
            }
            LongClickFriendlyLinkMovementMethod.set(
                textView = messageText,
                longClickTarget = root,
                onLinkClicked = listeners.linkClickListener::onLinkClick
            )
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        with(binding) {
            markdown.setText(messageText, data.message.text)
            messageContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                horizontalBias = if (data.isTheirs) 0f else 1f
            }
        }

        setupAttachments(data)
        applyMaxPossibleWidth(binding.root, binding.messageContainer, data.message)
    }

    private fun setupAttachments(data: MessageListItem.MessageItem) {
        binding.attachmentsContainer.removeAllViews()

        val linkAttachment = data.message.attachments.firstOrNull(Attachment::hasLink)

        if (linkAttachment != null) {
            val linkAttachmentView = attachmentViewFactory.createLinkAttachmentView(linkAttachment, context)
            binding.attachmentsContainer.addView(linkAttachmentView)
            (linkAttachmentView as? LinkAttachmentView)?.run {
                setPadding(LINK_VIEW_PADDING)
                setLinkPreviewClickListener { url ->
                    listeners.linkClickListener.onLinkClick(url)
                }
                setLongClickTarget(binding.root)
                showLinkAttachment(linkAttachment)
            }
        }
    }

    private fun applyMaxPossibleWidth(root: ConstraintLayout, messageContainer: View, message: Message) {
        val hasLink = message.attachments.any { it.hasLink() }
        val layoutWidth = messageContainer.layoutParams.width
        if (hasLink && layoutWidth == ConstraintSet.WRAP_CONTENT) {
            root.updateConstraints {
                constrainWidth(messageContainer.id, ConstraintSet.MATCH_CONSTRAINT)
                constrainDefaultWidth(messageContainer.id, ConstraintSet.MATCH_CONSTRAINT_SPREAD)
            }
        } else if (!hasLink && layoutWidth == ConstraintSet.MATCH_CONSTRAINT) {
            root.updateConstraints {
                constrainWidth(messageContainer.id, ConstraintSet.WRAP_CONTENT)
                constrainDefaultWidth(messageContainer.id, ConstraintSet.MATCH_CONSTRAINT_WRAP)
            }
        }
    }

    private companion object {
        private val LINK_VIEW_PADDING = 8.dpToPx()
    }
}
