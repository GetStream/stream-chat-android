package com.getstream.sdk.chat.adapter.viewholder.attachment

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.AttachmentListItem
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.databinding.StreamItemAttachmentFileBinding
import com.getstream.sdk.chat.utils.UiUtils
import com.getstream.sdk.chat.utils.extensions.inflater
import com.getstream.sdk.chat.view.MessageListView.AttachmentClickListener
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListenerView
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Attachment

internal class AttachmentViewHolderFile(
    parent: ViewGroup,
    private val style: MessageListViewStyle,
    private val bubbleHelper: BubbleHelper,
    private val messageItem: MessageItem,
    private val clickListener: AttachmentClickListener,
    private val longClickListener: MessageLongClickListener?,
    private val longClickListenerView: MessageLongClickListenerView? = null,
    private val binding: StreamItemAttachmentFileBinding =
        StreamItemAttachmentFileBinding.inflate(parent.inflater, parent, false)
) : BaseAttachmentViewHolder(binding.root) {

    private lateinit var attachment: Attachment

    override fun bind(attachmentListItem: AttachmentListItem) {
        attachment = attachmentListItem.attachment

        applyStyle()
        configAttachment()
        configClickListeners()
    }

    private fun applyStyle() {
        if (messageItem.isMine) {
            style.attachmentTitleTextMine.apply(binding.tvFileTitle)
            style.attachmentFileSizeTextMine.apply(binding.tvFileSize)
        } else {
            style.attachmentTitleTextTheirs.apply(binding.tvFileTitle)
            style.attachmentFileSizeTextTheirs.apply(binding.tvFileSize)
        }
    }

    private fun configAttachment() {
        binding.tvFileSize.text = UiUtils.getFileSizeHumanized(attachment.fileSize)
        binding.ivFileThumb.setImageResource(UiUtils.getIcon(attachment.mimeType))
        binding.tvFileTitle.text = attachment.title

        val background = bubbleHelper.getDrawableForAttachment(
            messageItem.message,
            messageItem.isMine,
            messageItem.positions,
            attachment
        )
        binding.attachmentview.background = background
    }

    private fun configClickListeners() {
        binding.attachmentview.setOnClickListener {
            clickListener.onAttachmentClick(
                messageItem.message,
                attachment
            )
        }
        binding.attachmentview.setOnLongClickListener {
            if (longClickListenerView != null) {
                longClickListenerView.onMessageLongClick2(messageItem.message, itemView)
            } else {
                longClickListener?.onMessageLongClick(messageItem.message)
            }

            true
        }
    }
}
