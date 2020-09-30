package com.getstream.sdk.chat.adapter.viewholder.attachment

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.AttachmentListItem
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.inflater
import com.getstream.sdk.chat.databinding.StreamItemAttachMediaBinding
import com.getstream.sdk.chat.enums.GiphyAction
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.view.MessageListView.AttachmentClickListener
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import com.getstream.sdk.chat.view.MessageListView.GiphySendListener
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import top.defaults.drawabletoolbox.DrawableBuilder

class AttachmentViewHolderMedia(
    parent: ViewGroup,
    private val giphySendListener: GiphySendListener,
    private val binding: StreamItemAttachMediaBinding =
        StreamItemAttachMediaBinding.inflate(parent.inflater, parent, false)
) : BaseAttachmentViewHolder(binding.root) {

    private lateinit var message: Message
    private lateinit var messageListItem: MessageItem
    private lateinit var style: MessageListViewStyle
    private lateinit var bubbleHelper: BubbleHelper

    private var clickListener: AttachmentClickListener? = null
    private var longClickListener: MessageLongClickListener? = null

    private lateinit var attachment: Attachment

    override fun bind(
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
        configMediaAttach()
        configActions()
        configClickListeners()
    }

    private fun applyStyle() {
        if (messageListItem.isMine) {
            style.attachmentTitleTextMine.apply(binding.tvMediaTitle)
            style.attachmentDescriptionTextMine.apply(binding.tvMediaDes)
        } else {
            style.attachmentTitleTextTheirs.apply(binding.tvMediaTitle)
            style.attachmentDescriptionTextTheirs.apply(binding.tvMediaDes)
        }
    }

    private fun configMediaAttach() {
        val type = attachment.type
        configImageThumbBackground()

        val thumbUrl = attachment.thumbUrl
        val imageUrl = if (thumbUrl.isNullOrEmpty()) attachment.imageUrl else thumbUrl

        Glide.with(context)
            .asDrawable()
            .load(imageUrl)
            .placeholder(R.drawable.stream_placeholder)
            .into(binding.ivMediaThumb)

        if (message.type != ModelType.message_ephemeral) {
            binding.tvMediaTitle.text = attachment.title
        }

        binding.tvMediaDes.text = attachment.text
        binding.tvMediaTitle.isVisible = !attachment.title.isNullOrEmpty()
        binding.tvMediaDes.isVisible = !attachment.text.isNullOrEmpty()
        binding.tvMediaPlay.isVisible = type == ModelType.attach_video
        binding.ivCommandLogo.isVisible = type == ModelType.attach_giphy

        if (binding.tvMediaDes.isVisible || binding.tvMediaTitle.isVisible) {
            val background = bubbleHelper.getDrawableForAttachmentDescription(
                messageListItem.message, messageListItem.isMine, messageListItem.positions
            )
            binding.clDes.background = background
        }
    }

    private fun configImageThumbBackground() {
        val background = bubbleHelper.getDrawableForAttachment(
            message,
            messageListItem.isMine,
            messageListItem.positions,
            attachment
        )
        binding.ivMediaThumb.setShape(context, background)
    }

    private fun configActions() {
        if (message.type == ModelType.message_ephemeral && message.command == ModelType.attach_giphy) {
            configGiphyAction()
        } else {
            binding.clAction.visibility = View.GONE
        }
    }

    private fun configGiphyAction() {
        binding.clAction.visibility = View.VISIBLE
        binding.tvActionSend.background = DrawableBuilder()
            .rectangle()
            .rounded()
            .strokeColor(Color.WHITE)
            .strokeWidth(Utils.dpToPx(2))
            .solidColor(ContextCompat.getColor(context, R.color.stream_input_message_send_button))
            .solidColorPressed(Color.LTGRAY)
            .build()
        binding.tvActionShuffle.background = DrawableBuilder()
            .rectangle()
            .rounded()
            .strokeColor(ContextCompat.getColor(context, R.color.stream_message_stroke))
            .strokeWidth(Utils.dpToPx(2))
            .solidColor(Color.WHITE)
            .solidColorPressed(Color.LTGRAY)
            .build()
        binding.tvActionCancel.background = DrawableBuilder()
            .rectangle()
            .rounded()
            .strokeColor(ContextCompat.getColor(context, R.color.stream_message_stroke))
            .strokeWidth(Utils.dpToPx(2))
            .solidColor(Color.WHITE)
            .solidColorPressed(Color.LTGRAY)
            .build()
        binding.tvActionSend.setOnClickListener {
            enableSendGiphyButtons(false)
            giphySendListener.onGiphySend(message, GiphyAction.SEND)
        }
        binding.tvActionShuffle.setOnClickListener {
            enableSendGiphyButtons(false)
            giphySendListener.onGiphySend(message, GiphyAction.SHUFFLE)
        }
        binding.tvActionCancel.setOnClickListener {
            giphySendListener.onGiphySend(
                message,
                GiphyAction.CANCEL
            )
        }
    }

    private fun enableSendGiphyButtons(isEnable: Boolean) {
        binding.progressBar.isVisible = !isEnable
        binding.tvActionSend.isEnabled = isEnable
        binding.tvActionShuffle.isEnabled = isEnable
        binding.tvActionCancel.isEnabled = isEnable
    }

    private fun configClickListeners() {
        binding.ivMediaThumb.setOnClickListener {
            clickListener?.onAttachmentClick(
                message,
                attachment
            )
        }
        binding.ivMediaThumb.setOnLongClickListener {
            longClickListener?.onMessageLongClick(message)
            true
        }
    }
}
