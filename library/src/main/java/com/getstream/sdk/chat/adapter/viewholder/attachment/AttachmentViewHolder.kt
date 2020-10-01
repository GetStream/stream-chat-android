package com.getstream.sdk.chat.adapter.viewholder.attachment

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.getstream.sdk.chat.Chat.Companion.getInstance
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.AttachmentListItem
import com.getstream.sdk.chat.adapter.FileAttachmentSelectedAdapter
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.inflater
import com.getstream.sdk.chat.databinding.StreamItemAttachMediaBinding
import com.getstream.sdk.chat.databinding.StreamItemAttachmentBinding
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import com.getstream.sdk.chat.view.MessageListView.AttachmentClickListener
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message

class AttachmentViewHolder(
    parent: ViewGroup,
    private val binding: StreamItemAttachmentBinding =
        StreamItemAttachmentBinding.inflate(parent.inflater, parent, false)
) : BaseAttachmentViewHolder(binding.root) {

    private val mediaBinding: StreamItemAttachMediaBinding = binding.clAttachmentMedia

    private lateinit var context: Context
    private lateinit var messageListItem: MessageItem
    private lateinit var message: Message
    private lateinit var style: MessageListViewStyle
    private lateinit var bubbleHelper: BubbleHelper

    private var clickListener: AttachmentClickListener? = null
    private var longClickListener: MessageLongClickListener? = null

    private lateinit var attachment: Attachment

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
        this.context = context
        this.messageListItem = messageListItem
        this.message = message
        this.style = style
        this.bubbleHelper = bubbleHelper

        this.clickListener = clickListener
        this.longClickListener = longClickListener

        attachment = attachmentListItem.attachment

        applyStyle()
        configAttachment()
    }

    private fun applyStyle() {
        if (messageListItem.isMine) {
            style.attachmentTitleTextMine.apply(mediaBinding.tvMediaTitle)
            style.attachmentDescriptionTextMine.apply(mediaBinding.tvMediaDes)
        } else {
            style.attachmentTitleTextTheirs.apply(mediaBinding.tvMediaTitle)
            style.attachmentDescriptionTextTheirs.apply(mediaBinding.tvMediaDes)
        }
    }

    private fun configAttachment() {
        var hasFile = false
        var hasMedia = false
        for (attachment in message.attachments) {
            if (attachment.type != null) {
                if (attachment.type == ModelType.attach_file) {
                    hasFile = true
                } else {
                    hasMedia = true
                }
            }
        }

        if (hasMedia) {
            mediaBinding.root.isVisible = true
            configMediaAttach()
        } else {
            mediaBinding.root.isVisible = false
        }

        if (hasFile) {
            binding.lvAttachmentFile.isVisible = true
            configFileAttach()
        } else {
            binding.lvAttachmentFile.isVisible = false
        }

        if (!hasMedia && !hasFile) {
            mediaBinding.ivMediaThumb.isVisible = false

            if (!attachment.title.isNullOrEmpty()) {
                mediaBinding.root.isVisible = true
                mediaBinding.tvMediaTitle.isVisible = true
                mediaBinding.tvMediaTitle.text = attachment.title
            } else {
                mediaBinding.tvMediaTitle.isVisible = false
            }

            if (!attachment.text.isNullOrEmpty()) {
                mediaBinding.root.isVisible = true
                mediaBinding.tvMediaDes.isVisible = true
                mediaBinding.tvMediaDes.text = attachment.text
            } else {
                mediaBinding.tvMediaDes.isVisible = false
            }
        }
    }

    private fun configMediaAttach() {
        val firstAttachment = message.attachments.first { it.type != ModelType.attach_file }
        val type = firstAttachment.type
        var attachUrl = firstAttachment.imageUrl
        if (type != null) {
            if (ModelType.attach_image == type) {
                attachUrl = firstAttachment.imageUrl
            } else if (ModelType.attach_giphy == type) {
                attachUrl = firstAttachment.thumbUrl
            } else if (ModelType.attach_video == type) {
                attachUrl = firstAttachment.thumbUrl
            } else {
                if (attachUrl == null) attachUrl = firstAttachment.image
            }
        }

        if (attachUrl.isNullOrEmpty()) {
            mediaBinding.root.isVisible = false
            return
        }
        mediaBinding.root.isVisible = true

        configAttachViewBackground(mediaBinding.root)
        configImageThumbBackground()
        configClickListeners()

        if (!attachUrl.contains("https:")) {
            attachUrl = "https:$attachUrl"
        }
        Glide.with(context)
            .load(getInstance().urlSigner().signImageUrl(attachUrl))
            .into(mediaBinding.ivMediaThumb)

        if (message.type != ModelType.message_ephemeral) {
            mediaBinding.tvMediaTitle.text = firstAttachment.title
        }
        mediaBinding.tvMediaDes.text = firstAttachment.text

        if (firstAttachment.text.isNullOrEmpty()) {
            mediaBinding.tvMediaDes.isVisible = false
        } else {
            mediaBinding.tvMediaDes.isVisible = true
        }

        if (firstAttachment.title.isNullOrEmpty()) {
            mediaBinding.tvMediaTitle.isVisible = false
        } else {
            mediaBinding.tvMediaTitle.isVisible = true
        }

        if (type == ModelType.attach_video) {
            mediaBinding.tvMediaPlay.isVisible = true
        } else {
            mediaBinding.tvMediaPlay.isVisible = false
        }
    }

    private fun configAttachViewBackground(view: View) {
        val messageBubbleDrawableRes = style.getMessageBubbleDrawable(messageListItem.isMine)
        if (messageBubbleDrawableRes != -1) {
            view.background = context.getDrawable(messageBubbleDrawableRes)
        }
    }

    private fun configImageThumbBackground() {
        var background = bubbleHelper.getDrawableForAttachment(
            message,
            messageListItem.isMine,
            messageListItem.positions,
            attachment
        )
        mediaBinding.ivMediaThumb.setShape(context, background)

        if (mediaBinding.tvMediaDes.isVisible || mediaBinding.tvMediaTitle.isVisible) {
            background = bubbleHelper.getDrawableForAttachmentDescription(
                messageListItem.message,
                messageListItem.isMine,
                messageListItem.positions
            )
            mediaBinding.clDes.background = background
        }
    }

    private fun configClickListeners() {
        mediaBinding.root.setOnClickListener {
            clickListener?.onAttachmentClick(message, attachment)
        }
        mediaBinding.root.setOnLongClickListener {
            longClickListener?.onMessageLongClick(message)
            true
        }
    }

    private fun configFileAttach() {
        configAttachViewBackground(binding.lvAttachmentFile)

        val attachments = message.attachments.filter { it.type == ModelType.attach_file }
        binding.lvAttachmentFile.adapter = FileAttachmentSelectedAdapter(
            LlcMigrationUtils.getMetaAttachments(attachments),
            false
        )
        binding.lvAttachmentFile.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            clickListener?.onAttachmentClick(message, attachment)
        }
        binding.lvAttachmentFile.onItemLongClickListener = OnItemLongClickListener { _, _, _, _ ->
            longClickListener?.onMessageLongClick(message)
            true
        }

        binding.lvAttachmentFile.updateLayoutParams<ConstraintLayout.LayoutParams> {
            val fileHeight = context.resources.getDimension(R.dimen.stream_attach_file_height)
            height = fileHeight.toInt() * attachments.size
        }
    }
}
