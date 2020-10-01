package com.getstream.sdk.chat.adapter.viewholder.attachment

import android.content.Context
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.AttachmentListItem
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.inflater
import com.getstream.sdk.chat.databinding.StreamItemAttachmentFileBinding
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import com.getstream.sdk.chat.view.MessageListView.AttachmentClickListener
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message

class AttachmentViewHolderFile(
    parent: ViewGroup,
    private val binding: StreamItemAttachmentFileBinding =
        StreamItemAttachmentFileBinding.inflate(parent.inflater, parent, false)
) : BaseAttachmentViewHolder(binding.root) {

    private lateinit var messageListItem: MessageItem
    private lateinit var message: Message
    private lateinit var attachment: Attachment
    private lateinit var style: MessageListViewStyle
    private lateinit var bubbleHelper: BubbleHelper

    private var clickListener: AttachmentClickListener? = null
    private var longClickListener: MessageLongClickListener? = null

    override fun bind(
        context: Context,
        messageListItem: MessageItem,
        message: Message,
        attachmentListItem: AttachmentListItem,
        style: MessageListViewStyle,
        bubbleHelper: BubbleHelper,
        clickListener: AttachmentClickListener?,
        longClickListener: MessageLongClickListener?
    ) {
        this.messageListItem = messageListItem
        this.message = message
        this.style = style
        this.bubbleHelper = bubbleHelper

        this.clickListener = clickListener
        this.longClickListener = longClickListener

        attachment = attachmentListItem.attachment

        applyStyle()
        configAttachment()
        configClickListeners()
    }

    private fun applyStyle() {
        if (messageListItem.isMine) {
            style.attachmentTitleTextMine.apply(binding.tvFileTitle)
            style.attachmentFileSizeTextMine.apply(binding.tvFileSize)
        } else {
            style.attachmentTitleTextTheirs.apply(binding.tvFileTitle)
            style.attachmentFileSizeTextTheirs.apply(binding.tvFileSize)
        }
    }

    private fun configAttachment() {
        binding.tvFileSize.text = LlcMigrationUtils.getFileSizeHumanized(attachment)
        binding.ivFileThumb.setImageResource(LlcMigrationUtils.getIcon(attachment))
        binding.tvFileTitle.text = attachment.title

        val background = bubbleHelper.getDrawableForAttachment(
            messageListItem.message,
            messageListItem.isMine,
            messageListItem.positions,
            attachment
        )
        binding.attachmentview.background = background
    }

    private fun configClickListeners() {
        binding.attachmentview.setOnClickListener {
            clickListener?.onAttachmentClick(
                message,
                attachment
            )
        }
        binding.attachmentview.setOnLongClickListener {
            longClickListener?.onMessageLongClick(message)
            true
        }
    }
}
