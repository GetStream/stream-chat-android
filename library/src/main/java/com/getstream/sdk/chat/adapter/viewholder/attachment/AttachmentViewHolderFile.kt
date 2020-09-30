package com.getstream.sdk.chat.adapter.viewholder.attachment

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.AttachmentListItem
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import com.getstream.sdk.chat.view.MessageListView.AttachmentClickListener
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message

class AttachmentViewHolderFile(resId: Int, parent: ViewGroup) :
    BaseAttachmentViewHolder(resId, parent) {

    private val cl_attachment: ConstraintLayout = itemView.findViewById(R.id.attachmentview)
    private val iv_file_thumb: ImageView = itemView.findViewById(R.id.iv_file_thumb)
    private val tv_file_size: TextView = itemView.findViewById(R.id.tv_file_size)
    private val tv_file_title: TextView = itemView.findViewById(R.id.tv_file_title)

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
            style.attachmentTitleTextMine.apply(tv_file_title)
            style.attachmentFileSizeTextMine.apply(tv_file_size)
        } else {
            style.attachmentTitleTextTheirs.apply(tv_file_title)
            style.attachmentFileSizeTextTheirs.apply(tv_file_size)
        }
    }

    private fun configAttachment() {
        tv_file_size.text = LlcMigrationUtils.getFileSizeHumanized(attachment)
        iv_file_thumb.setImageResource(LlcMigrationUtils.getIcon(attachment))
        tv_file_title.text = attachment.title

        val background = bubbleHelper.getDrawableForAttachment(
            messageListItem.message,
            messageListItem.isMine,
            messageListItem.positions,
            attachment
        )
        cl_attachment.background = background
    }

    private fun configClickListeners() {
        cl_attachment.setOnClickListener {
            clickListener?.onAttachmentClick(
                message,
                attachment
            )
        }
        cl_attachment.setOnLongClickListener {
            longClickListener?.onMessageLongClick(message)
            true
        }
    }
}
