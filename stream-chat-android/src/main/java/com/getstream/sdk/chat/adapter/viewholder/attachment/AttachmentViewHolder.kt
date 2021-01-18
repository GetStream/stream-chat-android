package com.getstream.sdk.chat.adapter.viewholder.attachment

import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.ChatUI
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.AttachmentListItem
import com.getstream.sdk.chat.adapter.FileAttachmentSelectedAdapter
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.databinding.StreamItemAttachMediaBinding
import com.getstream.sdk.chat.databinding.StreamItemAttachmentBinding
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.inflater
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.view.MessageListView.AttachmentClickListener
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Attachment

internal class AttachmentViewHolder(
    parent: ViewGroup,
    private val style: MessageListViewStyle,
    private val bubbleHelper: BubbleHelper,
    private val messageItem: MessageItem,
    private val clickListener: AttachmentClickListener,
    private val longClickListener: MessageLongClickListener? = null,
    private val longClickListenerView: MessageListView.MessageLongClickListenerView? = null,
    private val binding: StreamItemAttachmentBinding =
        StreamItemAttachmentBinding.inflate(parent.inflater, parent, false),
) : BaseAttachmentViewHolder(binding.root) {

    private val mediaBinding: StreamItemAttachMediaBinding = binding.clAttachmentMedia

    private lateinit var attachment: Attachment

    init {
        mediaBinding.tvMediaDes.maxLines = style.attachmentPreviewMaxLines
    }

    override fun bind(attachmentListItem: AttachmentListItem) {
        attachment = attachmentListItem.attachment

        applyStyle()
        configAttachment()
    }

    private fun applyStyle() {
        if (messageItem.isMine) {
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
        for (attachment in messageItem.message.attachments) {
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
        val firstAttachment = messageItem.message.attachments.first { it.type != ModelType.attach_file }
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

        mediaBinding.ivMediaThumb.load(
            data = ChatUI.instance().urlSigner.signImageUrl(attachUrl)
        )

        if (messageItem.message.type != ModelType.message_ephemeral) {
            mediaBinding.tvMediaTitle.text = firstAttachment.title
        }
        mediaBinding.tvMediaDes.text = firstAttachment.text

        mediaBinding.tvMediaDes.isVisible = !firstAttachment.text.isNullOrEmpty()
        mediaBinding.tvMediaTitle.isVisible = !firstAttachment.title.isNullOrEmpty()
        mediaBinding.tvMediaPlay.isVisible = type == ModelType.attach_video
    }

    private fun configAttachViewBackground(view: View) {
        val messageBubbleDrawableRes = style.getMessageBubbleDrawable(messageItem.isMine)
        if (messageBubbleDrawableRes != -1) {
            view.background = context.getDrawable(messageBubbleDrawableRes)
        }
    }

    private fun configImageThumbBackground() {
        var background = bubbleHelper.getDrawableForAttachment(
            messageItem.message,
            messageItem.isMine,
            messageItem.positions,
            attachment
        )
        mediaBinding.ivMediaThumb.setShape(context, background)

        if (mediaBinding.tvMediaDes.isVisible || mediaBinding.tvMediaTitle.isVisible) {
            background = bubbleHelper.getDrawableForAttachmentDescription(
                messageItem.message,
                messageItem.isMine,
                messageItem.positions
            )
            mediaBinding.clDes.background = background
        }
    }

    private fun configClickListeners() {
        mediaBinding.root.setOnClickListener {
            clickListener.onAttachmentClick(messageItem.message, attachment)
        }
        mediaBinding.root.setOnLongClickListener {
            if (longClickListenerView != null) {
                longClickListenerView.onMessageLongClick2(messageItem.message, itemView)
            } else {
                longClickListener?.onMessageLongClick(messageItem.message)
            }
            true
        }
    }

    private fun configFileAttach() {
        configAttachViewBackground(binding.lvAttachmentFile)

        val attachments = messageItem.message.attachments.filter { it.type == ModelType.attach_file }
        binding.lvAttachmentFile.adapter = FileAttachmentSelectedAdapter(
            attachments.map { AttachmentMetaData(it) },
            false
        )
        binding.lvAttachmentFile.onItemClickListener = OnItemClickListener { _, _, _, _ ->
            clickListener.onAttachmentClick(messageItem.message, attachment)
        }
        binding.lvAttachmentFile.onItemLongClickListener = OnItemLongClickListener { _, _, _, _ ->
            if (longClickListenerView != null) {
                longClickListenerView.onMessageLongClick2(messageItem.message, itemView)
            } else {
                longClickListener?.onMessageLongClick(messageItem.message)
            }

            true
        }

        binding.lvAttachmentFile.updateLayoutParams<ConstraintLayout.LayoutParams> {
            val fileHeight = context.resources.getDimension(R.dimen.stream_attach_file_height)
            height = fileHeight.toInt() * attachments.size
        }
    }
}
